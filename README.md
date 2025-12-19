Delivery Service API (Spring Boot)

REST API для управления службой доставки: отправители, получатели, посылки, курьеры и доставки.
Проект разработан в рамках практических работ №1–4 по дисциплине РБПО и включает базовую защиту API с использованием Spring Security (Basic Auth + CSRF).

Возможности

CRUD-операции для основных сущностей:

Senders

Recipients

Parcels

Couriers

Deliveries

Хранение данных в базе (JPA + H2)

Валидация входных данных

Аутентификация через HTTP Basic

Авторизация по ролям USER / ADMIN

CSRF-защита для изменяющих операций

Автоматическое создание администратора при запуске

Технологии

Java 17

Spring Boot

Spring Web

Spring Data JPA

Spring Validation

Spring Security

H2 Database (in-memory)

Gradle

Postman

Запуск
Локально (Gradle)
./gradlew bootRun


или из IDE (IntelliJ IDEA) — запуск класса Rbpo2025Application.

Базовый URL
http://localhost:8080

Аутентификация

Проект использует HTTP Basic Authentication.

Пользователи

Пользователи хранятся в базе данных

Пароли сохраняются только в зашифрованном виде

Поддерживаются роли:

USER

ADMIN

Администратор по умолчанию

При старте приложения:

автоматически создаётся пользователь admin

пароль генерируется случайным образом

пароль выводится в консоль

Пример лога:

ADMIN CREATED
Login: admin
Password: Xy7P@K2sL


 Пароль администратора не хранится в коде и конфигурации.

CSRF-защита

CSRF включён для всех изменяющих операций (POST, PUT, DELETE).

Получение CSRF-токена
GET /api/csrf


Пример ответа:

{
"parameterName": "_csrf",
"token": "FRsKZ1rfda6q...",
"headerName": "X-XSRF-TOKEN"
}

Использование CSRF-токена

Для защищённых запросов необходимо:

передать токен в заголовке:

X-XSRF-TOKEN: <token>


и сохранить cookie XSRF-TOKEN

Примеры запросов
Создание отправителя (POST)
POST /api/senders
Authorization: Basic <credentials>
X-XSRF-TOKEN: <token>
Content-Type: application/json

{
"name": "Test Sender",
"phone": "+79990001122",
"address": "Moscow"
}


Ответ:

{
"id": 1,
"name": "Test Sender",
"phone": "+79990001122",
"address": "Moscow"
}

Авторизация и доступ

Доступ к endpoint’ам ограничен ролями и настраивается в SecurityConfig.

Примеры:

Публичные endpoint’ы:

POST /api/auth/register

GET /api/csrf

Защищённые endpoint’ы:

POST /api/senders

PUT /api/deliveries/{id}

DELETE /api/*

Типовые ответы безопасности
Сценарий	Ответ
Без аутентификации	401 Unauthorized
Неверный логин/пароль	401 Unauthorized
POST без CSRF	403 Forbidden
Недостаточно прав	403 Forbidden
Корректный запрос	200 OK
Тестирование
Postman

Для проверки использовался Postman:

Получить CSRF-токен (GET /api/csrf)

Выполнить запрос с Basic Auth

Добавить CSRF-токен в заголовки

Проверить результат

Все сценарии из задания №4 были успешно проверены.

Безопасность

Пароли хэшируются перед сохранением

CSRF включён и обязателен для изменения данных

Нет пользователей с паролями в коде

Администратор создаётся динамически при старте

Статус задания

Практическая работа №1 — выполнена
 Практическая работа №2 — выполнена
 Практическая работа №3 — выполнена
 Практическая работа №4 — выполнена