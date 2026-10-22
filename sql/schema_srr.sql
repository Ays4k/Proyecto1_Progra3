-- =====================================================================
-- Sistema de Reserva de Recursos (SRR) - Base de datos completa
-- Motor: MySQL 8.0.16+ (los CHECK solo se aplican desde esa version)
--
-- Como usarlo (una sola vez, con un usuario administrador como root):
--   mysql -u root -p < schema_srr.sql
-- o abrirlo en MySQL Workbench y ejecutar todo el script.
--
-- Crea la base "srr", sus tablas, el usuario de MySQL que usa la aplicacion
-- (admin / admin) y carga los datos que antes estaban en los archivos JSON.
-- =====================================================================

-- Descomenta la siguiente linea SOLO si quieres empezar de cero (borra todo):
-- DROP DATABASE IF EXISTS srr;

CREATE DATABASE IF NOT EXISTS srr
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE srr;

-- ---------------------------------------------------------------------
-- Tablas
-- ---------------------------------------------------------------------
CREATE TABLE rol (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(15)  NOT NULL,          -- ADMINISTRADOR / FUNCIONARIO
    descripcion VARCHAR(50),
    CONSTRAINT pk_rol PRIMARY KEY (id),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)
);

CREATE TABLE usuario (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    identificacion VARCHAR(20)  NOT NULL,       -- cedula: es lo que se escribe en el Login
    clave          VARCHAR(100),                -- NULL = la clave inicial es la identificacion
    nombre         VARCHAR(50)  NOT NULL,
    telefono       VARCHAR(15),
    id_rol         BIGINT       NOT NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uq_usuario_identificacion UNIQUE (identificacion),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id)
);

CREATE TABLE categoria (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    descripcion VARCHAR(50) NOT NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (id),
    CONSTRAINT uq_categoria_descripcion UNIQUE (descripcion)
);

-- RESTRICT: no se puede borrar una categoria que tenga recursos
CREATE TABLE recurso (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    descripcion  VARCHAR(100) NOT NULL,
    id_categoria BIGINT       NOT NULL,
    CONSTRAINT pk_recurso PRIMARY KEY (id),
    CONSTRAINT fk_recurso_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria (id) ON DELETE RESTRICT
);

CREATE TABLE reserva (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    descripcion VARCHAR(100) NOT NULL,          -- "actividad" en el DTO
    fecha       DATE         NOT NULL,
    hora_inicio TIME         NOT NULL,
    hora_fin    TIME         NOT NULL,
    id_usuario  BIGINT       NOT NULL,
    estado      VARCHAR(10)  NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT pk_reserva PRIMARY KEY (id),
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id),
    CONSTRAINT ck_reserva_estado CHECK (estado IN ('ACTIVA', 'CANCELADA')),
    CONSTRAINT ck_reserva_horas  CHECK (hora_fin > hora_inicio)
);

CREATE INDEX ix_reserva_fecha   ON reserva (fecha);
CREATE INDEX ix_reserva_usuario ON reserva (id_usuario);

-- CASCADE: al borrar una reserva se borran sus filas de detalle
-- RESTRICT: no se puede borrar un recurso que este en una reserva
-- UNIQUE: un recurso no se repite dentro de la misma reserva
CREATE TABLE reserva_recurso (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    id_recurso BIGINT NOT NULL,
    id_reserva BIGINT NOT NULL,
    CONSTRAINT pk_reserva_recurso PRIMARY KEY (id),
    CONSTRAINT uq_reserva_recurso UNIQUE (id_reserva, id_recurso),
    CONSTRAINT fk_rr_reserva FOREIGN KEY (id_reserva)
        REFERENCES reserva (id) ON DELETE CASCADE,
    CONSTRAINT fk_rr_recurso FOREIGN KEY (id_recurso)
        REFERENCES recurso (id) ON DELETE RESTRICT
);

CREATE INDEX ix_rr_recurso ON reserva_recurso (id_recurso);

-- ---------------------------------------------------------------------
-- Usuario de MySQL para la aplicacion (solo permisos sobre los datos)
-- Debe coincidir con ConexionBD.java (admin / admin).
-- ---------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'admin'@'localhost' IDENTIFIED BY 'admin';
GRANT SELECT, INSERT, UPDATE, DELETE ON srr.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;

-- ---------------------------------------------------------------------
-- Datos iniciales (el orden importa por las llaves foraneas):
-- rol -> categoria -> usuario -> recurso -> reserva
-- ---------------------------------------------------------------------
INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMINISTRADOR', 'Gestiona funcionarios, categorias y recursos'),
    ('FUNCIONARIO',   'Crea y cancela reservas');

INSERT INTO categoria (descripcion) VALUES
    ('Sala de reuniones'),
    ('Laptop'),
    ('Proyector'),
    ('Vehículo'),
    ('Cámara'),
    ('Tablet');

-- Las claves estan en texto plano igual que en usuarios.json (solo para pruebas).
INSERT INTO usuario (identificacion, clave, nombre, telefono, id_rol) VALUES
    ('admin', 'admin', 'Admin', '0000-0000', (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR')),
    ('user', 'user', 'User', '0000-0000', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO')),
    ('10101010', 'maria123', 'Maria', '8888-1111', (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR')),
    ('20202020', 'carlos123', 'Carlos', '8888-2222', (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR')),
    ('25252525', 'gabriela123', 'Gabriela', '8888-2525', (SELECT id FROM rol WHERE nombre = 'ADMINISTRADOR')),
    ('30303030', 'ana123', 'Ana', '8888-3333', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO')),
    ('40404040', 'luis123', 'Luis', '8888-4444', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO')),
    ('50505050', 'sofia123', 'Sofia', '8888-5555', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO')),
    ('60606060', 'diego123', 'Diego', '8888-6666', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO')),
    ('70707070', 'valeria123', 'Valeria', '8888-7777', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO')),
    ('80808080', 'pablo123', 'Pablo', '8888-8888', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO')),
    ('90909090', 'andres123', 'Andres', '8888-9090', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO')),
    ('11111122', 'camila123', 'Camila', '8888-1122', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO')),
    ('22222233', 'jose123', 'Jose', '8888-2233', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO')),
    ('33333344', 'natalia123', 'Natalia', '8888-3344', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO')),
    ('44444455', 'esteban123', 'Esteban', '8888-4455', (SELECT id FROM rol WHERE nombre = 'FUNCIONARIO'));

INSERT INTO recurso (descripcion, id_categoria) VALUES
    ('Sala de reuniones - Piso 1', (SELECT id FROM categoria WHERE descripcion = 'Sala de reuniones')),
    ('Sala de reuniones - Piso 2', (SELECT id FROM categoria WHERE descripcion = 'Sala de reuniones')),
    ('Sala de reuniones - Piso 3', (SELECT id FROM categoria WHERE descripcion = 'Sala de reuniones')),
    ('Laptop Dell Latitude #1', (SELECT id FROM categoria WHERE descripcion = 'Laptop')),
    ('Laptop Dell Latitude #2', (SELECT id FROM categoria WHERE descripcion = 'Laptop')),
    ('Laptop HP Elitebook #3', (SELECT id FROM categoria WHERE descripcion = 'Laptop')),
    ('Laptop HP Elitebook #4', (SELECT id FROM categoria WHERE descripcion = 'Laptop')),
    ('Laptop Lenovo Thinkpad #5', (SELECT id FROM categoria WHERE descripcion = 'Laptop')),
    ('Proyector Epson #1', (SELECT id FROM categoria WHERE descripcion = 'Proyector')),
    ('Proyector BenQ #2', (SELECT id FROM categoria WHERE descripcion = 'Proyector')),
    ('Proyector Epson #3', (SELECT id FROM categoria WHERE descripcion = 'Proyector')),
    ('Vehículo Toyota Hilux', (SELECT id FROM categoria WHERE descripcion = 'Vehículo')),
    ('Vehículo Toyota Corolla', (SELECT id FROM categoria WHERE descripcion = 'Vehículo')),
    ('Cámara Canon EOS #1', (SELECT id FROM categoria WHERE descripcion = 'Cámara')),
    ('Cámara Sony Alpha #2', (SELECT id FROM categoria WHERE descripcion = 'Cámara')),
    ('Tablet Samsung Tab #1', (SELECT id FROM categoria WHERE descripcion = 'Tablet')),
    ('Tablet Samsung Tab #2', (SELECT id FROM categoria WHERE descripcion = 'Tablet')),
    ('Tablet iPad #3', (SELECT id FROM categoria WHERE descripcion = 'Tablet'));

-- Reservas de ejemplo. Cada una usa LAST_INSERT_ID() para guardar sus recursos.
-- Los recursos se buscan por descripcion (son unicas en los datos iniciales).
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
