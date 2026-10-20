USE srr;
START TRANSACTION;

-- RES-000001
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Reunion de equipo', '2026-08-05', '09:00', '11:00', (SELECT id FROM usuario WHERE identificacion = 'user'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Sala de reuniones - Piso 1')),
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Laptop Dell Latitude #1'));

-- RES-000002
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Presentacion a cliente', '2026-08-06', '14:00', '16:00', (SELECT id FROM usuario WHERE identificacion = 'user'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Proyector Epson #1'));

-- RES-000003
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Visita a proveedor', '2026-08-10', '08:00', '12:00', (SELECT id FROM usuario WHERE identificacion = 'user'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Vehículo Toyota Hilux'));

-- RES-000004
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Entrevista de trabajo', '2026-07-20', '10:00', '11:00', (SELECT id FROM usuario WHERE identificacion = 'user'), 'CANCELADA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Sala de reuniones - Piso 2'));

-- RES-000005
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Taller de Excel avanzado', '2026-08-12', '13:00', '17:00', (SELECT id FROM usuario WHERE identificacion = '30303030'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Laptop Dell Latitude #2')),
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Laptop HP Elitebook #3'));

-- RES-000006
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Sesion de retroalimentacion', '2026-06-15', '09:00', '10:00', (SELECT id FROM usuario WHERE identificacion = '40404040'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Sala de reuniones - Piso 1'));

-- RES-000007
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Cobertura de evento', '2026-08-14', '08:00', '12:00', (SELECT id FROM usuario WHERE identificacion = 'user'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Cámara Canon EOS #1')),
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Tablet Samsung Tab #1'));

-- RES-000008
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Capacitacion de onboarding', '2026-08-15', '09:00', '12:00', (SELECT id FROM usuario WHERE identificacion = '50505050'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Sala de reuniones - Piso 3'));

-- RES-000009
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Entrega de documentos', '2026-08-18', '07:00', '09:00', (SELECT id FROM usuario WHERE identificacion = '60606060'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Vehículo Toyota Corolla'));

-- RES-000010
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Trabajo remoto supervisado', '2026-05-02', '08:00', '16:00', (SELECT id FROM usuario WHERE identificacion = 'user'), 'CANCELADA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Laptop HP Elitebook #4'));

-- RES-000011
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Demo de producto', '2026-08-19', '10:00', '12:00', (SELECT id FROM usuario WHERE identificacion = '70707070'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Proyector BenQ #2')),
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Sala de reuniones - Piso 2'));

-- RES-000012
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Encuesta de satisfaccion en campo', '2026-08-20', '13:00', '15:00', (SELECT id FROM usuario WHERE identificacion = '80808080'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Tablet Samsung Tab #2')),
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Tablet iPad #3'));

-- RES-000013
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Sesion de fotos institucional', '2026-04-10', '09:00', '11:00', (SELECT id FROM usuario WHERE identificacion = '90909090'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Cámara Sony Alpha #2'));

-- RES-000014
INSERT INTO reserva (descripcion, fecha, hora_inicio, hora_fin, id_usuario, estado) VALUES ('Feria de proyectos', '2026-08-21', '08:00', '17:00', (SELECT id FROM usuario WHERE identificacion = 'user'), 'ACTIVA');
SET @r = LAST_INSERT_ID();
INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Laptop Lenovo Thinkpad #5')),
  (@r, (SELECT id FROM recurso WHERE descripcion = 'Proyector Epson #3'));

COMMIT;