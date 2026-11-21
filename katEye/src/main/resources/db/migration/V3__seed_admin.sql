-- V3: roles del sistema y usuario administrador inicial
-- Credenciales iniciales: admin@kateye.cl / Admin123! (cambiar en produccion)

INSERT IGNORE INTO rol (nombre) VALUES
    ('Administrador'),
    ('Vendedor'),
    ('Cliente');

INSERT IGNORE INTO usuario (nombre, correo, password_hash, telefono, region_codigo, comuna_codigo, rol_id, activo)
SELECT 'Administrador KatEye',
       'admin@kateye.cl',
       '$2b$10$XbAo/2edDUlbkpB6bLvYLu4zLo9VX/WsW7Jz23q2nqTIYCT1BCQg6',
       '+56900000000',
       '13',
       '13101',
       r.id,
       TRUE
FROM rol r
WHERE r.nombre = 'Administrador';
