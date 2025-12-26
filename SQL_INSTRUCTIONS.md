SELECT 
    u.id, 
    u.username, 
    (SELECT array_agg(role) 
     FROM public.app_user_roles 
     WHERE user_id = u.id) as roles
FROM public.app_users u
ORDER BY u.id;
```
**Результат:** Список всех пользователей с их ID, именем и ролями.


Сделать пользователя администратором


INSERT INTO public.app_user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM public.app_users
WHERE username = 'testuser'
  AND NOT EXISTS (
    SELECT 1 
    FROM public.app_user_roles 
    WHERE user_id = app_users.id 
      AND role = 'ADMIN'
  );



Удалить роль ADMIN у пользователя

**Пример:**
```sql
DELETE FROM public.app_user_roles
WHERE user_id = (SELECT id FROM public.app_users WHERE username = 'testuser')


Удалить пользователя полностью

```sql
DELETE FROM public.app_users
WHERE username = 'ваш_username';



### Проверить существование таблиц:

```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name IN ('app_users', 'app_user_roles');
```

Посмотреть структуру таблицы:

```sql
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'app_users'
ORDER BY ordinal_position;
```

---

