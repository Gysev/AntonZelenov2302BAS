# Задание 5 - JWT Authentication с Refresh Tokens

## Суть задания

Задание 5 реализует **JWT (JSON Web Token) аутентификацию** с поддержкой **refresh токенов**. Это современный подход к аутентификации в REST API, который использует токен-базированную систему вместо сессионной аутентификации.

### Основные концепции:

1. **Access Token** - короткоживущий токен (15 минут), используется для доступа к защищенным ресурсам
2. **Refresh Token** - долгоживущий токен (30 дней), используется для получения новой пары токенов
3. **UserSession** - запись в БД, которая отслеживает активные сессии пользователей
4. **SessionStatus** - статус сессии (ACTIVE, EXPIRED, REVOKED)

### Преимущества JWT:

- **Stateless** - не требует хранения сессий на сервере (кроме refresh токенов для отзыва)
- **Безопасность** - токены подписываются секретным ключом, невозможно подделать
- **Масштабируемость** - можно использовать на нескольких серверах без синхронизации сессий
- **Мобильные приложения** - идеально подходит для мобильных и SPA приложений
- **Нет CSRF** - токены передаются в заголовках, не в cookies, поэтому CSRF атаки не применимы

---

## Структура файлов и их назначение

### 1. Модели данных (Entities)

#### `src/main/java/ru/mtuci/rbpo2025/model/SessionStatus.java`
**Назначение:** Enum для статусов сессии пользователя

**Значения:**
- `ACTIVE` - активная сессия, токен можно использовать
- `EXPIRED` - сессия истекла по времени
- `REVOKED` - сессия отозвана (например, после refresh)

**Использование:** Хранится в таблице `user_sessions` для отслеживания состояния refresh токенов

---

#### `src/main/java/ru/mtuci/rbpo2025/model/UserSession.java`
**Назначение:** Entity для хранения сессий пользователей в БД

**Поля:**
- `id` - уникальный идентификатор сессии
- `user` - связь с пользователем (AppUser)
- `refreshToken` - сам refresh токен (хранится в БД)
- `status` - статус сессии (ACTIVE/EXPIRED/REVOKED)
- `createdAt` - время создания сессии
- `expiresAt` - время истечения сессии

**Таблица в БД:** `user_sessions`

**Зачем нужна:** 
- Отслеживание активных сессий
- Предотвращение повторного использования старых refresh токенов
- Возможность отзыва всех сессий пользователя

---

### 2. Репозитории

#### `src/main/java/ru/mtuci/rbpo2025/repository/UserSessionRepository.java`
**Назначение:** JPA репозиторий для работы с сессиями пользователей

**Методы:**
- `findByRefreshToken(String refreshToken)` - найти сессию по refresh токену
- `findByRefreshTokenAndStatus(String refreshToken, SessionStatus status)` - найти активную сессию по токену

**Использование:** Используется в `TokenService` для проверки и обновления токенов

---

### 3. Security компоненты

#### `src/main/java/ru/mtuci/rbpo2025/security/JwtTokenProvider.java`
**Назначение:** Генерация и валидация JWT токенов

**Основные методы:**

1. **`generateAccessToken(username, roles, userId)`**
   - Генерирует access токен
   - Срок жизни: 15 минут (настраивается в `application-local.properties`)
   - Содержит: username, userId, roles, type="access"

2. **`generateRefreshToken(username, userId, sessionId)`**
   - Генерирует refresh токен
   - Срок жизни: 30 дней (настраивается в `application-local.properties`)
   - Содержит: username, userId, sessionId, type="refresh"

3. **`validateToken(token)`**
   - Проверяет валидность токена (подпись, срок действия)

4. **`getUsernameFromToken(token)`** - извлекает username из токена
5. **`getRolesFromToken(token)`** - извлекает роли из токена
6. **`getTokenType(token)`** - определяет тип токена (access/refresh)

**Конфигурация:**
- Секретный ключ: `jwt.secret` в `application-local.properties`
- Должен быть минимум 256 бит для алгоритма HS512

---

#### `src/main/java/ru/mtuci/rbpo2025/security/JwtAuthenticationFilter.java`
**Назначение:** Фильтр Spring Security, который проверяет JWT токены в запросах

**Как работает:**
1. Перехватывает каждый HTTP запрос
2. Ищет заголовок `Authorization: Bearer <token>`
3. Если токен найден:
   - Валидирует токен через `JwtTokenProvider`
   - Проверяет, что это access токен (не refresh)
   - Извлекает username и roles
   - Устанавливает аутентификацию в Spring Security Context
4. Если токена нет или он невалидный - пропускает запрос дальше (для Basic Auth)

**Важно:** Фильтр НЕ блокирует запросы без JWT токенов, чтобы Basic Auth мог работать для заданий 1-4

---

#### `src/main/java/ru/mtuci/rbpo2025/security/SecurityConfig.java`
**Назначение:** Конфигурация Spring Security с двумя цепочками фильтров

**Важно:** CSRF полностью отключен во всем проекте (`.csrf(csrf -> csrf.disable())` в обеих цепочках)

**Две цепочки фильтров:**

1. **JWT SecurityFilterChain (Order 1)**
   - Обрабатывает все API endpoints
   - Использует `JwtAuthenticationFilter` для проверки JWT токенов
   - Stateless (без сессий)
   - Приоритет: первый (проверяет запросы первым)

2. **Basic Auth SecurityFilterChain (Order 2)**
   - Обрабатывает все API endpoints (fallback)
   - Использует Basic Authentication
   - Stateless (без сессий)
   - Приоритет: второй (обрабатывает запросы без JWT токенов)

**Почему две цепочки:**
- Задания 1-4 используют Basic Auth
- Задание 5 использует JWT
- Обе системы должны работать параллельно
- JWT фильтр проверяет запросы первым (Order 1)
- Если JWT токен не найден/невалиден, запрос обрабатывается Basic Auth (Order 2)

---

### 4. Сервисы

#### `src/main/java/ru/mtuci/rbpo2025/service/TokenService.java`
**Назначение:** Бизнес-логика работы с парами токенов

**Основные методы:**

1. **`generateTokenPair(AppUser user)`**
   - Генерирует пару access + refresh токенов
   - Создает запись `UserSession` в БД со статусом ACTIVE
   - Возвращает оба токена

2. **`refreshTokenPair(String oldRefreshToken)`**
   - Принимает старый refresh токен
   - Проверяет его валидность и статус в БД
   - Если валиден:
     - Помечает старую сессию как REVOKED
     - Создает новую сессию со статусом ACTIVE
     - Генерирует новую пару токенов
   - Если невалиден - выбрасывает исключение

**Логика безопасности:**
- Старый refresh токен нельзя использовать повторно (помечается как REVOKED)
- Каждый refresh создает новую сессию
- Истекшие сессии помечаются как EXPIRED

---

### 5. Контроллеры

#### `src/main/java/ru/mtuci/rbpo2025/controller/auth/AuthController.java`
**Назначение:** REST endpoints для аутентификации

**Endpoints:**

1. **`POST /api/auth/login`**
   - Принимает: `LoginRequest` (username, password)
   - Проверяет credentials
   - Вызывает `TokenService.generateTokenPair()`
   - Возвращает: `{ accessToken, refreshToken }`

2. **`POST /api/auth/refresh`**
   - Принимает: `RefreshRequest` (refreshToken)
   - Вызывает `TokenService.refreshTokenPair()`
   - Возвращает: `{ accessToken, refreshToken }` (новую пару)
   - Если токен невалиден/отозван - возвращает 401

3. **`POST /api/auth/register`** (уже был, не изменился)
   - Регистрация нового пользователя

---

### 6. DTO (Data Transfer Objects)

#### `src/main/java/ru/mtuci/rbpo2025/dto/auth/LoginRequest.java`
**Назначение:** DTO для запроса логина
- `username` - имя пользователя
- `password` - пароль

#### `src/main/java/ru/mtuci/rbpo2025/dto/auth/RefreshRequest.java`
**Назначение:** DTO для запроса обновления токенов
- `refreshToken` - текущий refresh токен

---

### 7. Конфигурация

#### `src/main/resources/application-local.properties`
**Добавленные настройки:**
```properties
jwt.secret=MySecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLongForHS512Algorithm123456789012345678901234567890
jwt.access-token-expiration-minutes=15
jwt.refresh-token-expiration-days=30
```

**Важно:** В production секретный ключ должен храниться в переменных окружения!

---

### 8. Зависимости

#### `build.gradle`
**Добавленные зависимости:**
```gradle
implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
```

**Библиотека:** JJWT (Java JWT) - стандартная библиотека для работы с JWT в Java

---

## Сценарий работы (как в задании)

### Шаг 1: Логин
```
POST /api/auth/login
Body: { "username": "user", "password": "pass" }
Response: { "accessToken": "...", "refreshToken": "..." }
```
- Создается UserSession в БД со статусом ACTIVE
- Оба токена сохраняются в переменные Postman

### Шаг 2: Использование Access Token
```
GET /api/senders
Header: Authorization: Bearer <accessToken>
Response: 200 OK (список отправителей)
```
- JwtAuthenticationFilter проверяет токен
- Извлекает username и roles
- Устанавливает аутентификацию
- Доступ предоставлен

### Шаг 3: Обновление токенов
```
POST /api/auth/refresh
Body: { "refreshToken": "<current_refresh_token>" }
Response: { "accessToken": "...", "refreshToken": "..." }
```
- Старая сессия помечается как REVOKED
- Создается новая сессия со статусом ACTIVE
- Возвращается новая пара токенов

### Шаг 4: Попытка использовать старый Refresh Token
```
POST /api/auth/refresh
Body: { "refreshToken": "<old_refresh_token>" }
Response: 401 Unauthorized
```
- Старая сессия уже имеет статус REVOKED
- TokenService выбрасывает исключение
- Сервер возвращает 401

### Шаг 5: Проверка в БД
Выполните SQL запрос в pgAdmin4:
```sql
SELECT 
    us.id,
    u.username,
    us.status,
    us.created_at,
    us.expires_at
FROM user_sessions us
JOIN app_users u ON us.user_id = u.id
ORDER BY us.created_at DESC;
```

**Ожидаемый результат:**
- Должны быть видны сессии со статусами:
  - **ACTIVE** - текущая активная сессия (последняя после refresh)
  - **REVOKED** - отозванные сессии (после использования refresh токена)
  - **EXPIRED** - истекшие сессии (если есть)

**Это подтверждает, что:**
1. Старые refresh токены помечаются как REVOKED
2. Новые сессии создаются со статусом ACTIVE
3. Система правильно отслеживает жизненный цикл токенов

---

## Безопасность

### Защита от атак:

1. **CSRF атаки** - не применимы, т.к. токены передаются в заголовке `Authorization`, а не в cookies. CSRF полностью отключен в проекте.
2. **Replay атаки** - предотвращаются через статусы сессий (REVOKED). Старый refresh токен нельзя использовать повторно.
3. **Подделка токенов** - невозможна, т.к. токены подписаны секретным ключом (HS512 алгоритм)
4. **Утечка токенов** - access токены короткоживущие (15 минут), refresh токены можно отозвать через изменение статуса в БД

### Best Practices:

1. **Секретный ключ** - должен быть минимум 256 бит, храниться в переменных окружения
2. **HTTPS** - обязательно в production (токены передаются в открытом виде)
3. **Refresh Token Rotation** - каждый refresh создает новую сессию (реализовано)
4. **Token Expiration** - короткие access токены, длинные refresh токены

---

## Вопросы для защиты

### 1. Почему два типа токенов?
**Ответ:** 
- Access токены короткоживущие (15 мин) - минимизируют риск при утечке
- Refresh токены долгоживущие (30 дней) - удобство для пользователя
- При компрометации access токена - ущерб минимален (токен скоро истечет)

### 2. Зачем хранить refresh токены в БД?
**Ответ:**
- Возможность отзыва токенов (logout)
- Предотвращение повторного использования старых токенов
- Отслеживание активных сессий пользователя
- Возможность отозвать все сессии при компрометации

### 3. Почему CSRF отключен?
**Ответ:**
- CSRF атаки работают через cookies (браузер автоматически отправляет cookies)
- JWT токены передаются в заголовке `Authorization: Bearer <token>`
- Заголовки НЕ отправляются автоматически браузером (в отличие от cookies)
- Поэтому CSRF не применим к JWT токенам
- В проекте CSRF полностью отключен для всех endpoints (и JWT, и Basic Auth)

### 4. Что происходит при refresh токена?
**Ответ:**
1. Проверяется валидность старого refresh токена
2. Проверяется статус сессии в БД (должна быть ACTIVE)
3. Старая сессия помечается как REVOKED
4. Создается новая сессия со статусом ACTIVE
5. Генерируется новая пара токенов
6. Старый refresh токен больше нельзя использовать

### 5. Как работает JwtAuthenticationFilter?
**Ответ:**
1. Перехватывает каждый HTTP запрос
2. Ищет заголовок `Authorization: Bearer <token>`
3. Если найден - валидирует через JwtTokenProvider
4. Если валиден - извлекает username и roles, устанавливает аутентификацию
5. Если не найден/невалиден - пропускает дальше (для Basic Auth)

### 6. Почему две SecurityFilterChain?
**Ответ:**
- Задания 1-4 используют Basic Auth (без CSRF)
- Задание 5 использует JWT (без CSRF)
- Обе системы должны работать параллельно
- JWT фильтр имеет приоритет (Order 1) - проверяет токены первым
- Basic Auth - fallback (Order 2) - обрабатывает запросы без JWT токенов
- CSRF отключен в обеих цепочках

---

## Схема работы

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/auth/login
       │ { username, password }
       ▼
┌─────────────────────┐
│  AuthController     │
│  - проверка creds   │
│  - вызов TokenService│
└──────┬──────────────┘
       │
       │ generateTokenPair()
       ▼
┌─────────────────────┐
│   TokenService      │
│  - генерация токенов│
│  - создание сессии  │
└──────┬──────────────┘
       │
       │ { accessToken, refreshToken }
       ▼
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ GET /api/senders
       │ Authorization: Bearer <accessToken>
       ▼
┌─────────────────────┐
│ JwtAuthentication   │
│ Filter              │
│ - проверка токена   │
│ - установка auth    │
└──────┬──────────────┘
       │
       │ Authenticated request
       ▼
┌─────────────────────┐
│ SenderController    │
│ - возврат данных    │
└─────────────────────┘
```

---

## Ключевые моменты для защиты

1. **Понимание JWT** - токены содержат информацию о пользователе, подписаны секретным ключом
2. **Два типа токенов** - access (короткий) и refresh (длинный) для баланса безопасности и удобства
3. **Хранение сессий** - в БД для возможности отзыва и отслеживания
4. **Статусы сессий** - ACTIVE, EXPIRED, REVOKED для управления жизненным циклом
5. **Refresh Token Rotation** - каждый refresh создает новую сессию, старая отзывается
6. **Безопасность** - CSRF полностью отключен в проекте, т.к. токены передаются в заголовках, не в cookies
7. **Две цепочки фильтров** - для поддержки Basic Auth (задания 1-4) и JWT (задание 5), обе без CSRF

---

## Файлы для проверки

### Основные файлы задания 5:
1. `model/SessionStatus.java` - enum статусов
2. `model/UserSession.java` - entity сессии
3. `repository/UserSessionRepository.java` - репозиторий
4. `security/JwtTokenProvider.java` - генерация/валидация токенов
5. `security/JwtAuthenticationFilter.java` - фильтр проверки токенов
6. `security/SecurityConfig.java` - конфигурация (две цепочки)
7. `service/TokenService.java` - бизнес-логика токенов
8. `controller/auth/AuthController.java` - endpoints login/refresh
9. `dto/auth/LoginRequest.java` - DTO для логина
10. `dto/auth/RefreshRequest.java` - DTO для refresh
11. `application-local.properties` - настройки JWT
12. `build.gradle` - зависимости JJWT

### Таблицы в БД:
- `user_sessions` - создается автоматически Hibernate
  - `id` (bigint)
  - `user_id` (bigint, FK to app_users)
  - `refresh_token` (varchar(500))
  - `status` (varchar)
  - `created_at` (timestamp)
  - `expires_at` (timestamp)

---

## Итог

Задание 5 реализует современную токен-базированную аутентификацию, которая:
- Безопаснее сессионной (токены подписаны секретным ключом, CSRF отключен)
- Удобнее для REST API и мобильных приложений
- Масштабируемее (stateless, не требует синхронизации сессий между серверами)
- С поддержкой refresh токенов для удобства пользователей
- Полностью без CSRF токенов (отключен во всем проекте)

