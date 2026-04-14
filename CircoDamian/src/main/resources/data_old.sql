-- ============================================================
-- Datos iniciales: Admin del sistema
-- Credenciales fijas: admin / admin
-- INSERT IGNORE evita error si ya existe en ejecuciones posteriores
-- ============================================================

INSERT IGNORE INTO persona (nombre, email, nacionalidad)
VALUES ('Administrador General', 'admin@circo.local', 'ES');

INSERT IGNORE INTO credenciales (nombre_usuario, password, perfil, id_persona)
VALUES ('admin', 'admin', 'ADMIN',
    (SELECT id FROM persona WHERE email = 'admin@circo.local'));
