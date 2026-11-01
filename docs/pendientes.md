# Pendientes que necesitan decisión de Inge

Cosas que quedaron resueltas con una opción por defecto razonable, pero que según `AGENTS.md`
deben consultarse. Cada punto indica qué se hizo y qué cambia si se decide otra cosa.

## 1. Matriz de permisos por rol (propuesta mía, no aprobada)

Archivo: `nucleo/.../seguridad/Permisos.java`.

| Rol                    | Puede                                                                                          |
| ---------------------- | ---------------------------------------------------------------------------------------------- |
| `ADMINISTRADOR`        | Todo (único que autoriza orden duplicada y entrega a crédito)                                  |
| `RECEPCIONISTA`        | Clientes y vehículos, crear órdenes, cambiar estados, presupuestos, asignar técnicos, pagos, reportes |
| `TECNICO`              | Actualizar avance de sus tareas, registrar consumo de repuestos                                |
| `ENCARGADO_INVENTARIO` | Inventario y compras, registrar consumo de repuestos, reportes                                 |

Decidir: ¿es correcta? ¿Quién aprueba el presupuesto (hoy cualquier rol con `GESTIONAR_PRESUPUESTO`)?
¿El técnico debe poder finalizar órdenes (hoy no)?
R// si es correcta y el tecnico si tiene que poder finalizar ordenes

## 2. Interpretaciones de las reglas de negocio

- **Regla 2** ("reparaciones facturables"): el modelo no distingue reparaciones no facturables
  (garantía, cortesía). Hoy **toda** reparación exige presupuesto aprobado.
- **Regla 3**: el consumo de repuestos solo se acepta con la orden `EN_REPARACION`.
- **Regla 4**: el saldo es *total del presupuesto aprobado − pagos* (como indica `AGENTS.md`).
  Los repuestos consumidos que no estaban en el presupuesto **no** aumentan el saldo.
- **Pagos**: sin presupuesto aprobado el saldo es 0, por lo tanto no se puede registrar ningún pago.
  Tampoco se acepta un pago mayor al saldo. Esto evita cobrar lo no aprobado, pero impide anticipos.
- **Flujo de presupuesto**: solo se crea con la orden `EN_DIAGNOSTICO`; para `enviarAAprobacion`
  tiene que existir un presupuesto `PENDIENTE`; se aprueba o rechaza solo con la orden en
  `ESPERANDO_APROBACION`; una orden no puede tener dos presupuestos aprobados.
R// Estan todas bien.

## 3. Modelo de datos (sin cambios, solo observaciones)

- `orden_servicio.credito_autorizado` es redundante con `id_usuario_autoriza_credito`. El código
  escribe ambos de forma coherente y deriva el booleano del usuario. No se tocó el esquema.
- `AGENTS.md` dice 21 tablas; `esquema.sql` crea 23.
- No hay forma de cambiar el rol de un usuario ya creado (la entidad no tiene `setRol`). ¿Se necesita?
//Si es reduntante y puedes trabajar solo con uno, quedate con el escencial. No se especifico pero el poder editar un registro siempre es necesario, manejar un crud completo en cada datos que se ingresan en los formualrios es buena practica.
- 

## 4. Base de datos de pruebas

`taller_app` solo tiene permisos sobre `taller_automotriz`, así que no se pudo crear
`taller_automotriz_test`. Las pruebas usan la base real dentro de una transacción que siempre se
revierte (la base queda intacta; verificado con conteos en cero después de correr todo). Si se
prefiere una base aparte, hay que ejecutar como root:

```sql
CREATE DATABASE taller_automotriz_test;
GRANT ALL ON taller_automotriz_test.* TO 'taller_app'@'localhost';
```
R// Ejecute el sql tal cual lo pintaste, utiliza esos datos para realizar los test.

y aplicar `database/esquema.sql` ahí. Después se cambiaría la URL en las pruebas.

## 5. Otras observaciones

- No hay bloqueo de cuenta por intentos fallidos de login (solo se audita `LOGIN_FALLIDO`).
- El remoto `origin` apunta a `.../taller-aotomotriz.git` (con "aotomotriz"). Si es un error de
  tipeo del repositorio, conviene renombrarlo en GitHub.
- Git avisa de fines de línea (LF/CRLF). Un `.gitattributes` con `* text=auto` lo evitaría.
//Dejalo asi. Si era un error pero ya lo corregi. Esta bien agregalo.
---

## Resolución de las decisiones (2026-11-01)

1. Matriz aceptada. Se agregó la acción `FINALIZAR_ORDEN` (técnico, recepcionista y administrador)
   y `GESTIONAR_CATALOGOS` (solo administrador: técnicos, servicios y tipos de atención).
2. Interpretaciones de las reglas aceptadas sin cambios.
3. Se eliminó la columna redundante `orden_servicio.credito_autorizado` (queda
   `id_usuario_autoriza_credito`). Cambiada en `esquema.sql` y aplicada con `ALTER TABLE` en
   `taller_automotriz`. Se agregó CRUD completo con edición para todos los catálogos
   (`Catalogos`/`CrudServicio`) y cambio de rol de usuarios (`UsuarioServicio.cambiarRol`).
4. Las pruebas corren sobre `taller_automotriz_test` (propiedad `taller.bd.nombre` en
   `nucleo/build.gradle`); `BaseBD` se niega a correr sobre una base que no termine en `_test`.
5. Se agregó `.gitattributes`. El remoto `origin` sigue apuntando al nombre viejo del repositorio
   (el cambio de URL quedó pendiente de tu autorización).

---

## Fase 5: interpretaciones de los reportes (para tu visto bueno)

- **Ingresos por servicio:** solo cuenta servicios de presupuestos `APROBADO` cuya orden está
  `FINALIZADA` o `ENTREGADA` (lo ejecutado), no lo presupuestado. Coherente con "no cobrar servicios
  que no se ejecutaron". Es ingreso facturable, no dinero ya cobrado.
- **Antigüedad de órdenes:** días desde el ingreso hasta la entrega (entregadas), hasta el último
  cambio de estado (canceladas) o hasta hoy (el resto).
- **Rangos de fechas:** se filtran por fecha de ingreso de la orden (reportes 1 y 4), de asignación
  (reporte 2) y de consumo (reporte 3). El reporte 5 no usa rango: lista todo el historial.
- **Quién los ve:** `ADMINISTRADOR`, `RECEPCIONISTA` y `ENCARGADO_INVENTARIO` (los técnicos no).
- **Librerías:** se usaron OpenPDF 2.0.3 y Apache POI 5.3.0 (ya estaban en la lista aprobada; las
  versiones las elegí yo).
- **Datos de prueba:** `database/datos_prueba.sql` se carga una sola vez sobre la base vacía. Las
  pruebas de `DatosPruebaTest` y varias de reportes asumen `taller_automotriz_test` vacía: no cargues
  datos ahí.
