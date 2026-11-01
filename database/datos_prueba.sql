-- Datos ficticios para demostración y pruebas manuales. NO contiene datos personales reales.
-- Cargar UNA sola vez sobre la base vacía creada con esquema.sql:
--     mysql -u taller_app -p taller_automotriz < database/datos_prueba.sql
--
-- Usuarios de ejemplo (contraseñas ficticias, guardadas como hash BCrypt):
--     admin / Admin2026          (ADMINISTRADOR)
--     recepcion / Recep2026      (RECEPCIONISTA)
--     tecnico1, tecnico2 / Tecnico2026   (TECNICO)
--     inventario / Inventario2026        (ENCARGADO_INVENTARIO)
--
-- Los datos respetan las reglas del negocio: el stock de cada repuesto es lo comprado menos lo
-- consumido, las órdenes entregadas tienen saldo cero y toda reparación tiene presupuesto aprobado.
USE taller_automotriz;

INSERT INTO marca (id, nombre) VALUES
    (1, 'Toyota'), (2, 'Nissan'), (3, 'Chevrolet'), (4, 'Hyundai');

INSERT INTO modelo (id, id_marca, nombre) VALUES
    (1, 1, 'Corolla'), (2, 1, 'Hilux'), (3, 2, 'Sentra'),
    (4, 2, 'Versa'), (5, 3, 'Spark'), (6, 4, 'Elantra');

INSERT INTO cliente (id, documento, nombre, telefono, correo, direccion) VALUES
    (1, '0000-0001', 'Carlos Mendoza', '5550-0101', 'carlos.mendoza@ejemplo.test', 'Colonia Las Flores, casa 12'),
    (2, '0000-0002', 'Ana Paredes', '5550-0102', 'ana.paredes@ejemplo.test', 'Barrio El Centro, calle 3'),
    (3, '0000-0003', 'Roberto Salinas', '5550-0103', NULL, 'Residencial Los Pinos, bloque B'),
    (4, '0000-0004', 'Lucía Fernández', '5550-0104', 'lucia.fernandez@ejemplo.test', NULL),
    (5, '0000-0005', 'Distribuidora Ejemplo S.A.', '5550-0105', 'contacto@distribuidora.test', 'Zona Industrial, bodega 7');

INSERT INTO vehiculo (id, placa, id_cliente, id_modelo, anio, color, kilometraje) VALUES
    (1, 'AAA-1001', 1, 1, 2018, 'Blanco', 84500),
    (2, 'AAA-1002', 2, 3, 2020, 'Rojo', 45200),
    (3, 'AAA-1003', 3, 2, 2016, 'Gris', 132000),
    (4, 'AAA-1004', 4, 6, 2021, 'Azul', 28900),
    (5, 'AAA-1005', 5, 5, 2019, 'Negro', 61000),
    (6, 'AAA-1006', 5, 4, 2022, 'Plata', 15300);

INSERT INTO tecnico (id, nombre, especialidad, activo) VALUES
    (1, 'Luis Ramírez', 'Mecánica general', TRUE),
    (2, 'Marta Flores', 'Electricidad', TRUE),
    (3, 'Jorge Castillo', 'Frenos y suspensión', TRUE);

INSERT INTO usuario (id, nombre_usuario, contrasena_hash, rol, activo, id_tecnico) VALUES
    (1, 'admin', '$2a$10$B0Eg95RW2kcdy.TEsUsr9espxJI1qs.HlVH89RM/698kwYz7sjJPm', 'ADMINISTRADOR', TRUE, NULL),
    (2, 'recepcion', '$2a$10$kbhHkbnoiWixP.FjWNo45.wQqHz6ps/QZB5EjrMnNilZXyQ6Yi0EO', 'RECEPCIONISTA', TRUE, NULL),
    (3, 'tecnico1', '$2a$10$9MHAo/ibeVFYX2Jg4vOfhu/6MejhpPDMomzcaI4rnlilH7SKGEJWW', 'TECNICO', TRUE, 1),
    (4, 'tecnico2', '$2a$10$9MHAo/ibeVFYX2Jg4vOfhu/6MejhpPDMomzcaI4rnlilH7SKGEJWW', 'TECNICO', TRUE, 2),
    (5, 'inventario', '$2a$10$QJe8j.dxfcUVwRC.n2kRCe4fJGd2QbJ1rFesVxD7BmUAqvw/DnUiK', 'ENCARGADO_INVENTARIO', TRUE, NULL);

INSERT INTO tipo_atencion (id, nombre) VALUES
    (1, 'Mantenimiento preventivo'), (2, 'Reparación de frenos'),
    (3, 'Diagnóstico eléctrico'), (4, 'Alineación y balanceo');

INSERT INTO servicio (id, nombre, precio_base) VALUES
    (1, 'Cambio de aceite y filtro', 25.00),
    (2, 'Revisión de frenos', 40.00),
    (3, 'Diagnóstico eléctrico', 35.00),
    (4, 'Alineación y balanceo', 30.00),
    (5, 'Cambio de pastillas y discos', 45.00),
    (6, 'Mano de obra general', 20.00);

-- Stock actual = compras - consumos (ver más abajo).
INSERT INTO repuesto (id, codigo, nombre, stock, stock_minimo, precio_costo, precio_venta) VALUES
    (1, 'FIL-ACE-01', 'Filtro de aceite', 29, 5, 4.00, 7.50),
    (2, 'ACE-5W30', 'Aceite 5W-30 (litro)', 76, 10, 6.00, 10.00),
    (3, 'PAS-FRE-01', 'Pastillas de freno delanteras', 11, 4, 22.00, 38.00),
    (4, 'BUJ-STD-01', 'Bujía estándar', 40, 8, 3.00, 6.00),
    (5, 'BAT-12V-01', 'Batería 12V', 3, 2, 70.00, 110.00),
    (6, 'DIS-FRE-01', 'Disco de freno', 5, 2, 35.00, 60.00);

INSERT INTO proveedor (id, nombre, telefono) VALUES
    (1, 'Repuestos del Norte', '5551-0001'),
    (2, 'Lubricantes y Filtros Ejemplo', '5551-0002');

INSERT INTO compra (id, id_proveedor, id_usuario, fecha, total) VALUES
    (1, 1, 5, DATE_SUB(NOW(), INTERVAL 40 DAY), 1314.00),
    (2, 2, 5, DATE_SUB(NOW(), INTERVAL 30 DAY), 160.00);

INSERT INTO detalle_compra (id_compra, id_repuesto, cantidad, costo_unitario) VALUES
    (1, 1, 20, 4.00), (1, 2, 60, 6.00), (1, 3, 12, 22.00),
    (1, 4, 40, 3.00), (1, 5, 4, 70.00), (1, 6, 6, 35.00),
    (2, 2, 20, 6.00), (2, 1, 10, 4.00);

-- Órdenes: 1 y 2 entregadas, 3 finalizada con saldo, 4 en reparación, 5 esperando aprobación,
-- 6 en diagnóstico, 7 recibida, 8 cancelada, 9 con presupuesto rechazado.
INSERT INTO orden_servicio (id, id_vehiculo, id_tipo_atencion, prioridad, estado, fecha_ingreso, fecha_entrega,
                            id_usuario_registro, id_usuario_autoriza_duplicada, id_usuario_autoriza_credito) VALUES
    (1, 1, 1, 'MEDIA', 'ENTREGADA', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), 2, NULL, NULL),
    (2, 2, 2, 'ALTA', 'ENTREGADA', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), 2, NULL, NULL),
    (3, 3, 3, 'MEDIA', 'FINALIZADA', DATE_SUB(NOW(), INTERVAL 8 DAY), NULL, 2, NULL, NULL),
    (4, 4, 2, 'URGENTE', 'EN_REPARACION', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, 2, NULL, NULL),
    (5, 5, 1, 'BAJA', 'ESPERANDO_APROBACION', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 2, NULL, NULL),
    (6, 6, 4, 'MEDIA', 'EN_DIAGNOSTICO', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, 2, NULL, NULL),
    (7, 1, 4, 'BAJA', 'RECIBIDA', DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, 2, NULL, NULL),
    (8, 2, 1, 'MEDIA', 'CANCELADA', DATE_SUB(NOW(), INTERVAL 10 DAY), NULL, 2, NULL, NULL),
    (9, 3, 2, 'ALTA', 'ESPERANDO_APROBACION', DATE_SUB(NOW(), INTERVAL 6 DAY), NULL, 2, NULL, NULL);

INSERT INTO diagnostico (id_orden, id_tecnico, descripcion, fecha) VALUES
    (1, 1, 'Mantenimiento de 5000 km: aceite y filtro vencidos.', DATE_SUB(NOW(), INTERVAL 20 DAY)),
    (2, 3, 'Pastillas gastadas al límite; vibración al frenar.', DATE_SUB(NOW(), INTERVAL 15 DAY)),
    (3, 2, 'Batería sin carga; alternador en buen estado.', DATE_SUB(NOW(), INTERVAL 8 DAY)),
    (4, 3, 'Discos deformados y pastillas gastadas.', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (5, 1, 'Cambio de aceite y filtro según kilometraje.', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (6, 1, 'Desgaste irregular en llantas delanteras; revisar alineación.', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (9, 3, 'Ruido al frenar; se sospecha de las zapatas.', DATE_SUB(NOW(), INTERVAL 6 DAY));

INSERT INTO presupuesto (id, id_orden, estado, fecha, total, id_usuario_aprobador) VALUES
    (1, 1, 'APROBADO', DATE_SUB(NOW(), INTERVAL 20 DAY), 72.50, 2),
    (2, 2, 'APROBADO', DATE_SUB(NOW(), INTERVAL 15 DAY), 128.00, 2),
    (3, 3, 'APROBADO', DATE_SUB(NOW(), INTERVAL 8 DAY), 162.50, 2),
    (4, 4, 'APROBADO', DATE_SUB(NOW(), INTERVAL 3 DAY), 205.00, 2),
    (5, 5, 'PENDIENTE', DATE_SUB(NOW(), INTERVAL 2 DAY), 72.50, NULL),
    (6, 9, 'RECHAZADO', DATE_SUB(NOW(), INTERVAL 6 DAY), 40.00, 2);

INSERT INTO detalle_servicio (id_presupuesto, id_servicio, cantidad_horas, precio_unitario) VALUES
    (1, 1, 1.00, 25.00),
    (2, 5, 2.00, 45.00),
    (3, 3, 1.50, 35.00),
    (4, 2, 1.00, 40.00), (4, 5, 1.00, 45.00),
    (5, 1, 1.00, 25.00),
    (6, 2, 1.00, 40.00);

INSERT INTO detalle_presupuesto_repuesto (id_presupuesto, id_repuesto, cantidad, precio_unitario) VALUES
    (1, 1, 1, 7.50), (1, 2, 4, 10.00),
    (2, 3, 1, 38.00),
    (3, 5, 1, 110.00),
    (4, 6, 2, 60.00),
    (5, 1, 1, 7.50), (5, 2, 4, 10.00);

INSERT INTO asignacion (id_orden, id_tecnico, fecha_asignacion, estado_tarea, porcentaje_avance) VALUES
    (1, 1, DATE_SUB(NOW(), INTERVAL 19 DAY), 'TERMINADA', 100),
    (2, 3, DATE_SUB(NOW(), INTERVAL 14 DAY), 'TERMINADA', 100),
    (3, 2, DATE_SUB(NOW(), INTERVAL 7 DAY), 'TERMINADA', 100),
    (4, 3, DATE_SUB(NOW(), INTERVAL 2 DAY), 'EN_CURSO', 40),
    (6, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 'PENDIENTE', 0);

INSERT INTO observacion_orden (id_orden, id_tecnico, texto, fecha) VALUES
    (3, 2, 'Se reemplazó la batería y se verificó el sistema de carga.', DATE_SUB(NOW(), INTERVAL 7 DAY)),
    (4, 3, 'Discos delanteros desmontados; falta instalar el segundo disco.', DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Consumos reales (los que descuentan stock).
INSERT INTO consumo_repuesto (id_orden, id_repuesto, id_usuario, cantidad, precio_unitario, fecha) VALUES
    (1, 1, 3, 1, 7.50, DATE_SUB(NOW(), INTERVAL 19 DAY)),
    (1, 2, 3, 4, 10.00, DATE_SUB(NOW(), INTERVAL 19 DAY)),
    (2, 3, 3, 1, 38.00, DATE_SUB(NOW(), INTERVAL 14 DAY)),
    (3, 5, 4, 1, 110.00, DATE_SUB(NOW(), INTERVAL 7 DAY)),
    (4, 6, 3, 1, 60.00, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO pago (id_orden, id_usuario, monto, metodo, fecha) VALUES
    (1, 2, 72.50, 'EFECTIVO', DATE_SUB(NOW(), INTERVAL 17 DAY)),
    (2, 2, 80.00, 'TARJETA', DATE_SUB(NOW(), INTERVAL 12 DAY)),
    (2, 2, 48.00, 'EFECTIVO', DATE_SUB(NOW(), INTERVAL 12 DAY)),
    (3, 2, 60.00, 'TRANSFERENCIA', DATE_SUB(NOW(), INTERVAL 6 DAY)),
    (4, 2, 100.00, 'EFECTIVO', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Historial de estados (regla 5): cada orden termina en su estado actual.
INSERT INTO historial_estado_orden (id_orden, estado_anterior, estado_nuevo, fecha, id_usuario) VALUES
    (1, NULL, 'RECIBIDA', DATE_SUB(NOW(), INTERVAL 20 DAY), 2),
    (1, 'RECIBIDA', 'EN_DIAGNOSTICO', DATE_SUB(NOW(), INTERVAL 20 DAY), 2),
    (1, 'EN_DIAGNOSTICO', 'ESPERANDO_APROBACION', DATE_SUB(NOW(), INTERVAL 20 DAY), 2),
    (1, 'ESPERANDO_APROBACION', 'EN_REPARACION', DATE_SUB(NOW(), INTERVAL 19 DAY), 2),
    (1, 'EN_REPARACION', 'FINALIZADA', DATE_SUB(NOW(), INTERVAL 18 DAY), 3),
    (1, 'FINALIZADA', 'ENTREGADA', DATE_SUB(NOW(), INTERVAL 17 DAY), 2),
    (2, NULL, 'RECIBIDA', DATE_SUB(NOW(), INTERVAL 15 DAY), 2),
    (2, 'RECIBIDA', 'EN_DIAGNOSTICO', DATE_SUB(NOW(), INTERVAL 15 DAY), 2),
    (2, 'EN_DIAGNOSTICO', 'ESPERANDO_APROBACION', DATE_SUB(NOW(), INTERVAL 15 DAY), 2),
    (2, 'ESPERANDO_APROBACION', 'EN_REPARACION', DATE_SUB(NOW(), INTERVAL 14 DAY), 2),
    (2, 'EN_REPARACION', 'FINALIZADA', DATE_SUB(NOW(), INTERVAL 13 DAY), 3),
    (2, 'FINALIZADA', 'ENTREGADA', DATE_SUB(NOW(), INTERVAL 12 DAY), 2),
    (3, NULL, 'RECIBIDA', DATE_SUB(NOW(), INTERVAL 8 DAY), 2),
    (3, 'RECIBIDA', 'EN_DIAGNOSTICO', DATE_SUB(NOW(), INTERVAL 8 DAY), 2),
    (3, 'EN_DIAGNOSTICO', 'ESPERANDO_APROBACION', DATE_SUB(NOW(), INTERVAL 8 DAY), 2),
    (3, 'ESPERANDO_APROBACION', 'EN_REPARACION', DATE_SUB(NOW(), INTERVAL 7 DAY), 2),
    (3, 'EN_REPARACION', 'FINALIZADA', DATE_SUB(NOW(), INTERVAL 6 DAY), 4),
    (4, NULL, 'RECIBIDA', DATE_SUB(NOW(), INTERVAL 3 DAY), 2),
    (4, 'RECIBIDA', 'EN_DIAGNOSTICO', DATE_SUB(NOW(), INTERVAL 3 DAY), 2),
    (4, 'EN_DIAGNOSTICO', 'ESPERANDO_APROBACION', DATE_SUB(NOW(), INTERVAL 3 DAY), 2),
    (4, 'ESPERANDO_APROBACION', 'EN_REPARACION', DATE_SUB(NOW(), INTERVAL 2 DAY), 2),
    (5, NULL, 'RECIBIDA', DATE_SUB(NOW(), INTERVAL 2 DAY), 2),
    (5, 'RECIBIDA', 'EN_DIAGNOSTICO', DATE_SUB(NOW(), INTERVAL 2 DAY), 2),
    (5, 'EN_DIAGNOSTICO', 'ESPERANDO_APROBACION', DATE_SUB(NOW(), INTERVAL 2 DAY), 2),
    (6, NULL, 'RECIBIDA', DATE_SUB(NOW(), INTERVAL 1 DAY), 2),
    (6, 'RECIBIDA', 'EN_DIAGNOSTICO', DATE_SUB(NOW(), INTERVAL 1 DAY), 2),
    (7, NULL, 'RECIBIDA', DATE_SUB(NOW(), INTERVAL 2 HOUR), 2),
    (8, NULL, 'RECIBIDA', DATE_SUB(NOW(), INTERVAL 10 DAY), 2),
    (8, 'RECIBIDA', 'CANCELADA', DATE_SUB(NOW(), INTERVAL 9 DAY), 2),
    (9, NULL, 'RECIBIDA', DATE_SUB(NOW(), INTERVAL 6 DAY), 2),
    (9, 'RECIBIDA', 'EN_DIAGNOSTICO', DATE_SUB(NOW(), INTERVAL 6 DAY), 2),
    (9, 'EN_DIAGNOSTICO', 'ESPERANDO_APROBACION', DATE_SUB(NOW(), INTERVAL 6 DAY), 2);

INSERT INTO auditoria (id_usuario, accion, tabla_afectada, id_registro, fecha) VALUES
    (1, 'LOGIN', 'usuario', 1, DATE_SUB(NOW(), INTERVAL 40 DAY)),
    (5, 'REGISTRAR_COMPRA', 'compra', 1, DATE_SUB(NOW(), INTERVAL 40 DAY)),
    (2, 'CREAR_ORDEN', 'orden_servicio', 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
    (2, 'APROBAR_PRESUPUESTO', 'presupuesto', 1, DATE_SUB(NOW(), INTERVAL 20 DAY)),
    (2, 'REGISTRAR_PAGO', 'pago', 1, DATE_SUB(NOW(), INTERVAL 17 DAY)),
    (2, 'RECHAZAR_PRESUPUESTO', 'presupuesto', 6, DATE_SUB(NOW(), INTERVAL 5 DAY));
