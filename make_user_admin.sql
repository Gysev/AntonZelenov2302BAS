
-- Проверка существования таблицы
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_name IN ('app_users', 'app_user_roles');


-- Создание админа
INSERT INTO public.app_user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM public.app_users
WHERE username = ''
AND NOT EXISTS (
    SELECT 1 FROM public.app_user_roles 
    WHERE user_id = app_users.id AND role = 'ADMIN'
);

-- Проверка
SELECT u.id, u.username, array_agg(ur.role) as roles
FROM public.app_users u
LEFT JOIN public.app_user_roles ur ON u.id = ur.user_id
WHERE u.username = ''
GROUP BY u.id, u.username;
