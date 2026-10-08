-- 仅用于本地演示，账号密码均为 DevFlow123!
USE devflow;

INSERT INTO app_user (username, password_hash, display_name)
VALUES
    ('alice', '$2a$10$CIbGLXVr1m192X4mxubOlefec2dbnKweyUf5UFVP6sU5RDUi6bo6', 'Alice'),
    ('bob', '$2a$10$2gOjoiYQISei88Gba8a2d.mz4OpRQHesC3fUt.e/aK2iLJYgQ7fx', 'Bob');