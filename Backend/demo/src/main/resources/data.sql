-- Movimientos básicos del sistema de asistencia
INSERT OR IGNORE INTO movimiento (id_movimiento, descripcion, abre_desc, estado) VALUES (1, 'Entrada', 'ENT', 'ACTIVO');
INSERT OR IGNORE INTO movimiento (id_movimiento, descripcion, abre_desc, estado) VALUES (2, 'Salida', 'SAL', 'ACTIVO');
INSERT OR IGNORE INTO movimiento (id_movimiento, descripcion, abre_desc, estado) VALUES (3, 'Entrada Break', 'E_BRK', 'ACTIVO');
INSERT OR IGNORE INTO movimiento (id_movimiento, descripcion, abre_desc, estado) VALUES (4, 'Fin Break', 'F_BRK', 'ACTIVO');
