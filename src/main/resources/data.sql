-- 1) Crea l'utente admin SOLO se non esiste già
INSERT INTO users (email, password, role, must_change_password)
SELECT 'admin@system.com',
       '$2a$10$4Fh7YJZKMWH8U0845NGZIecJIfdmDGZCcuaDkuKQzwC70mugkxzWy',
       'ADMIN',
       FALSE
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@system.com'
);

-- 2) Crea l'admin SOLO se non esiste già
INSERT INTO admin (user_id)
SELECT id FROM users WHERE email = 'admin@system.com'
                       AND NOT EXISTS (
        SELECT 1 FROM admin WHERE user_id = (
            SELECT id FROM users WHERE email = 'admin@system.com'
        )
    );