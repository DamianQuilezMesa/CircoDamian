-- Datos de muestra. INSERT IGNORE para no duplicar si ya existen.

INSERT IGNORE INTO persona (id, nombre, email, nacionalidad) VALUES
(2,  'Laura Mendez',   'laura.mendez@circo.com', 'ES'),
(3,  'Carlos Ruiz',    'carlos.ruiz@circo.com',  'MX'),
(4,  'Marco Rossi',    'marco.rossi@circo.com',  'IT'),
(5,  'Sofia Dupont',   'sofia.dupont@circo.com', 'FR'),
(6,  'Ivan Petrov',    'ivan.petrov@circo.com',  'RU'),
(7,  'Ana Lima',       'ana.lima@circo.com',     'BR'),
(8,  'Kenji Tanaka',   'kenji.tanaka@circo.com', 'JP'),
(9,  'Elena Varga',    'elena.varga@circo.com',  'HU'),
(10, 'Luis Torres',    'luis.torres@circo.com',  'MO');

INSERT IGNORE INTO coordinacion (id_persona, senior, fecha_senior) VALUES
(2, TRUE,  '2020-03-15'),
(3, FALSE, NULL);

INSERT IGNORE INTO artista (id_persona, apodo) VALUES
(4,  'El Gran Marco'),
(5,  'La Pluma'),
(6,  NULL),
(7,  'Aninha'),
(8,  'Kenjiro'),
(9,  NULL),
(10, 'El Mago Torres');

INSERT IGNORE INTO artista_especialidad (id_artista, especialidad) VALUES
(4,  'ACROBACIA'), (4,  'EQUILIBRISMO'),
(5,  'HUMOR'),
(6,  'ACROBACIA'), (6,  'MALABARISMO'),
(7,  'MAGIA'),     (7,  'HUMOR'),
(8,  'EQUILIBRISMO'), (8, 'ACROBACIA'),
(9,  'MALABARISMO'),
(10, 'MAGIA');

INSERT IGNORE INTO credenciales (nombre_usuario, password, perfil, id_persona) VALUES
('laura',  'laura123',  'COORDINACION', 2),
('carlos', 'carlos123', 'COORDINACION', 3),
('marco',  'marco123',  'ARTISTA',      4),
('sofia',  'sofia123',  'ARTISTA',      5),
('ivan',   'ivan123',   'ARTISTA',      6),
('ana',    'ana123',    'ARTISTA',      7),
('kenji',  'kenji123',  'ARTISTA',      8),
('elena',  'elena123',  'ARTISTA',      9),
('luis',   'luis123',   'ARTISTA',     10);

INSERT IGNORE INTO espectaculo (id, nombre, fecha_inicio, fecha_fin, id_coordinador) VALUES
(1, 'Noche de Estrellas', '2025-06-01', '2025-08-31', 2),
(2, 'El Gran Finale',     '2025-09-15', '2025-12-15', 2),
(3, 'Magia y Acrobacia',  '2026-01-10', '2026-06-10', 3);

INSERT IGNORE INTO numero (id, nombre, duracion, orden, id_espectaculo) VALUES
(1,  'Apertura Acrobatica',  10.0, 1, 1),
(2,  'El Mago del Fuego',     8.5, 2, 1),
(3,  'Equilibrio en Altura', 12.0, 3, 1),
(4,  'Cierre con Humor',      6.5, 4, 1),
(5,  'Vuelo Libre',          15.0, 1, 2),
(6,  'Juegos Malabares',      9.0, 2, 2),
(7,  'Fuego y Fantasia',     20.0, 3, 2),
(8,  'Ilusiones',            10.5, 1, 3),
(9,  'Torre Humana',         14.0, 2, 3),
(10, 'Desaparicion Final',    8.0, 3, 3);

INSERT IGNORE INTO numero_artista (id_numero, id_artista) VALUES
(1, 4),  (1, 6),
(2, 10),
(3, 8),  (3, 4),
(4, 5),  (4, 7),
(5, 4),  (5, 8),
(6, 6),  (6, 9),
(7, 4),  (7, 5), (7, 6), (7, 10),
(8, 10), (8, 7),
(9, 4),  (9, 6), (9, 8),
(10,10), (10, 5);
