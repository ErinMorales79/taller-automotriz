# Reporte de la sesión autónoma (módulo `nucleo`)

**Resultado:** las 4 etapas pedidas están terminadas y validadas. `./gradlew build` pasa sin
advertencias y las **110 pruebas** de `nucleo` pasan. La base de datos real quedó sin filas de prueba.

## Etapas y commits

| Etapa | Qué se hizo | Commit |
| ----- | ----------- | ------ |
| 0 | Se commiteó el patrón State y `OrdenServicio` que estaban pendientes en el índice | `7c97cfc` |
| 1 | Revisión de conexión/excepciones/entidades; 12 entidades nuevas y 3 enums, `Transaccion`, validadores de montos | `b6b7a2b` |
| 2 | 20 DAO (interfaz + implementación JDBC cada uno); pruebas CRUD, búsquedas y rollback | `6b80e70`, `196c7f1` |
| 3 | Servicios con las 5 reglas de negocio; prueba válida e inválida por regla | `a8393c1` |
| 4 | Permisos por rol, login BCrypt, auditoría, historial, asignación de tareas | `710d3e9` |

## Qué quedó construido

- **Entidades nuevas:** `Servicio`, `Repuesto`, `Proveedor`, `Presupuesto` (compone detalles de
  servicio y de repuesto), `Diagnostico`, `Asignacion`, `ObservacionOrden`, `HistorialEstadoOrden`,
  `ConsumoRepuesto`, `Compra` (con detalles), `Pago`, `Auditoria` y 3 enums.
- **DAO:** `nucleo/dao` (interfaces) y `nucleo/dao/jdbc` (implementaciones). Todo con
  `PreparedStatement`; los errores de MySQL (duplicado, referencia, CHECK) se traducen a
  `PersistenciaException` con mensaje en español. Cada DAO recibe la `Connection`, así el servicio
  decide dónde empieza y termina la transacción.
- **Transacciones:** `util/Transaccion` hace commit si todo sale bien y rollback ante cualquier
  error. Se usa en presupuestos y compras (encabezado-detalle) y en consumo/compra de repuestos
  (movimientos de inventario). El descuento de stock es atómico en SQL (`stock + ? >= 0`).
- **Reglas (en `nucleo/servicio`):**
  1. `OrdenServicioServicio.crear`: segunda orden activa del mismo tipo exige un administrador.
  2. `iniciarReparacion`: exige presupuesto aprobado.
  3. `InventarioServicio.registrarConsumo`: único punto que descuenta stock; presupuestar no lo toca.
  4. `entregar`: orden finalizada (lo impone el State) y saldo cero o crédito autorizado por administrador.
  5. Todo cambio de estado (incluida la creación) escribe en el historial con fecha y responsable,
     en la misma transacción que el cambio.
- **Seguridad:** `Permisos` (matriz rol → acciones) se exige en cada servicio, no en la vista.
  Login con BCrypt, mismo mensaje para usuario inexistente y clave incorrecta, y los intentos
  (buenos y malos) quedan auditados. Contraseña: mínimo 8 caracteres y máximo 72 bytes (límite de BCrypt).
- **Auditoría:** cada operación relevante registra usuario, acción, tabla e id dentro de su transacción.
  Una operación rechazada no deja registro; el login fallido sí, a propósito.

## Cómo se probó

- Las pruebas con MySQL (`BaseBD`) corren sobre una sola conexión real dentro de una transacción que
  siempre se revierte al final. Así no ensucian la base y además se puede comprobar que el rollback
  deshace de verdad lo escrito a medias.
- Cobertura por tipo: entidades (validaciones), DAO (CRUD, filtros, restricciones), transacciones
  (rollback ante detalle inválido), reglas (válido e inválido), seguridad y auditoría.
- Para correr: `JAVA_HOME` apuntando a JDK 21 y `./gradlew :nucleo:test`. Requiere MySQL levantado y
  `config/conexion.properties` correcto.

## Cosas a tener presente

- Las decisiones que tomé y que necesitan tu visto bueno están en `docs/pendientes.md`
  (sobre todo la matriz de permisos y la interpretación de las reglas 2, 3 y 4).
- `PresupuestoServicio.crear` ahora recibe también el usuario (`crear(presupuesto, usuario)`).
- `Asignacion` ya no valida técnico activo en el constructor (rompía al reconstruir desde la base);
  esa validación vive en `TareaServicio.asignar`.

## Fuera del alcance de esta sesión (siguiente paso)

- Servicios CRUD para clientes, vehículos, marcas, repuestos, etc. (hoy se usan los DAO directo).
- Los 5 reportes obligatorios y su exportación a PDF/Excel.
- `database/datos_prueba.sql`.
- Módulos `escritorio` (Swing + FlatLaf) y `api` (Spring Boot + JWT).
