-- Esquema de la base de datos del taller automotriz (MySQL 8.0.16 o superior por los CHECK).
CREATE DATABASE IF NOT EXISTS taller_automotriz
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE taller_automotriz;

CREATE TABLE marca (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE modelo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_marca INT NOT NULL,
    nombre VARCHAR(60) NOT NULL,
    UNIQUE (id_marca, nombre),
    FOREIGN KEY (id_marca) REFERENCES marca (id)
) ENGINE=InnoDB;

CREATE TABLE cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    documento VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    correo VARCHAR(100),
    direccion VARCHAR(150)
) ENGINE=InnoDB;

CREATE TABLE vehiculo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    placa VARCHAR(15) NOT NULL UNIQUE,
    id_cliente INT NOT NULL,
    id_modelo INT NOT NULL,
    anio SMALLINT NOT NULL CHECK (anio >= 1950),
    color VARCHAR(30),
    kilometraje INT NOT NULL DEFAULT 0 CHECK (kilometraje >= 0),
    FOREIGN KEY (id_cliente) REFERENCES cliente (id),
    FOREIGN KEY (id_modelo) REFERENCES modelo (id)
) ENGINE=InnoDB;

CREATE TABLE tecnico (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    especialidad VARCHAR(60),
    activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena_hash VARCHAR(100) NOT NULL,
    rol VARCHAR(25) NOT NULL
        CHECK (rol IN ('ADMINISTRADOR', 'RECEPCIONISTA', 'TECNICO', 'ENCARGADO_INVENTARIO')),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    id_tecnico INT UNIQUE,
    FOREIGN KEY (id_tecnico) REFERENCES tecnico (id),
    CHECK (rol = 'TECNICO' OR id_tecnico IS NULL)
) ENGINE=InnoDB;

CREATE TABLE tipo_atencion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE orden_servicio (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_vehiculo INT NOT NULL,
    id_tipo_atencion INT NOT NULL,
    prioridad VARCHAR(10) NOT NULL CHECK (prioridad IN ('BAJA', 'MEDIA', 'ALTA', 'URGENTE')),
    estado VARCHAR(25) NOT NULL DEFAULT 'RECIBIDA'
        CHECK (estado IN ('RECIBIDA', 'EN_DIAGNOSTICO', 'ESPERANDO_APROBACION',
                          'EN_REPARACION', 'FINALIZADA', 'ENTREGADA', 'CANCELADA')),
    fecha_ingreso DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega DATETIME,
    credito_autorizado BOOLEAN NOT NULL DEFAULT FALSE,
    id_usuario_registro INT NOT NULL,
    id_usuario_autoriza_duplicada INT,
    id_usuario_autoriza_credito INT,
    FOREIGN KEY (id_vehiculo) REFERENCES vehiculo (id),
    FOREIGN KEY (id_tipo_atencion) REFERENCES tipo_atencion (id),
    FOREIGN KEY (id_usuario_registro) REFERENCES usuario (id),
    FOREIGN KEY (id_usuario_autoriza_duplicada) REFERENCES usuario (id),
    FOREIGN KEY (id_usuario_autoriza_credito) REFERENCES usuario (id)
) ENGINE=InnoDB;

CREATE TABLE diagnostico (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_orden INT NOT NULL,
    id_tecnico INT NOT NULL,
    descripcion TEXT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_orden) REFERENCES orden_servicio (id),
    FOREIGN KEY (id_tecnico) REFERENCES tecnico (id)
) ENGINE=InnoDB;

CREATE TABLE asignacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_orden INT NOT NULL,
    id_tecnico INT NOT NULL,
    fecha_asignacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_tarea VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado_tarea IN ('PENDIENTE', 'EN_CURSO', 'TERMINADA')),
    porcentaje_avance TINYINT NOT NULL DEFAULT 0 CHECK (porcentaje_avance BETWEEN 0 AND 100),
    FOREIGN KEY (id_orden) REFERENCES orden_servicio (id),
    FOREIGN KEY (id_tecnico) REFERENCES tecnico (id)
) ENGINE=InnoDB;

CREATE TABLE observacion_orden (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_orden INT NOT NULL,
    id_tecnico INT NOT NULL,
    texto TEXT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_orden) REFERENCES orden_servicio (id),
    FOREIGN KEY (id_tecnico) REFERENCES tecnico (id)
) ENGINE=InnoDB;

CREATE TABLE historial_estado_orden (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_orden INT NOT NULL,
    estado_anterior VARCHAR(25),
    estado_nuevo VARCHAR(25) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario INT NOT NULL,
    FOREIGN KEY (id_orden) REFERENCES orden_servicio (id),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id)
) ENGINE=InnoDB;

CREATE TABLE servicio (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    precio_base DECIMAL(12,2) NOT NULL CHECK (precio_base >= 0)
) ENGINE=InnoDB;

CREATE TABLE repuesto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    stock_minimo INT NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    precio_costo DECIMAL(12,2) NOT NULL CHECK (precio_costo >= 0),
    precio_venta DECIMAL(12,2) NOT NULL CHECK (precio_venta >= 0)
) ENGINE=InnoDB;

CREATE TABLE presupuesto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_orden INT NOT NULL,
    estado VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    id_usuario_aprobador INT,
    FOREIGN KEY (id_orden) REFERENCES orden_servicio (id),
    FOREIGN KEY (id_usuario_aprobador) REFERENCES usuario (id)
) ENGINE=InnoDB;

CREATE TABLE detalle_servicio (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_presupuesto INT NOT NULL,
    id_servicio INT NOT NULL,
    cantidad_horas DECIMAL(6,2) NOT NULL CHECK (cantidad_horas > 0),
    precio_unitario DECIMAL(12,2) NOT NULL CHECK (precio_unitario >= 0),
    FOREIGN KEY (id_presupuesto) REFERENCES presupuesto (id),
    FOREIGN KEY (id_servicio) REFERENCES servicio (id)
) ENGINE=InnoDB;

CREATE TABLE detalle_presupuesto_repuesto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_presupuesto INT NOT NULL,
    id_repuesto INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(12,2) NOT NULL CHECK (precio_unitario >= 0),
    FOREIGN KEY (id_presupuesto) REFERENCES presupuesto (id),
    FOREIGN KEY (id_repuesto) REFERENCES repuesto (id)
) ENGINE=InnoDB;

CREATE TABLE consumo_repuesto (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_orden INT NOT NULL,
    id_repuesto INT NOT NULL,
    id_usuario INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(12,2) NOT NULL CHECK (precio_unitario >= 0),
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_orden) REFERENCES orden_servicio (id),
    FOREIGN KEY (id_repuesto) REFERENCES repuesto (id),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id)
) ENGINE=InnoDB;

CREATE TABLE proveedor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20)
) ENGINE=InnoDB;

CREATE TABLE compra (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_proveedor INT NOT NULL,
    id_usuario INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    FOREIGN KEY (id_proveedor) REFERENCES proveedor (id),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id)
) ENGINE=InnoDB;

CREATE TABLE detalle_compra (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_compra INT NOT NULL,
    id_repuesto INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    costo_unitario DECIMAL(12,2) NOT NULL CHECK (costo_unitario >= 0),
    FOREIGN KEY (id_compra) REFERENCES compra (id),
    FOREIGN KEY (id_repuesto) REFERENCES repuesto (id)
) ENGINE=InnoDB;

CREATE TABLE pago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_orden INT NOT NULL,
    id_usuario INT NOT NULL,
    monto DECIMAL(12,2) NOT NULL CHECK (monto > 0),
    metodo VARCHAR(20) NOT NULL CHECK (metodo IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_orden) REFERENCES orden_servicio (id),
    FOREIGN KEY (id_usuario) REFERENCES usuario (id)
) ENGINE=InnoDB;

CREATE TABLE auditoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    accion VARCHAR(50) NOT NULL,
    tabla_afectada VARCHAR(50) NOT NULL,
    id_registro INT,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuario (id)
) ENGINE=InnoDB;
