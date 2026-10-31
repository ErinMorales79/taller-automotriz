# Taller Automotriz y Órdenes de Servicio

## Resumen del proyecto

Sistema para un taller automotriz que recibe vehículos, diagnostica averías,
asigna técnicos y consume repuestos. Resuelve dos problemas: aprobaciones de
presupuestos que se pierden y cobros de servicios que no se ejecutaron.

Proyecto universitario desarrollado por Daniel con Claude. Se apunta directo a
la versión final: persistencia real con MySQL, JDBC y DAO desde el inicio.

El enunciado completo (lineamientos, reglas, reportes, entregables y casos de
prueba) está en `docs/enunciado.md`. Consultarlo ante cualquier duda de alcance.

## Componentes

| Componente    | Tecnología                               | Función                                                                             |
| ------------- | ---------------------------------------- | ----------------------------------------------------------------------------------- |
| Escritorio    | Java 21 + Swing                          | Sistema principal del taller. Se conecta a MySQL con JDBC/DAO                       |
| API           | Java 21 + Spring Boot                    | API HTTP autenticada. Solo la usa la app móvil                                      |
| Móvil         | Android en Java (compatibilidad Java 17) | App para técnicos: ver órdenes asignadas, actualizar avance y agregar observaciones |
| Base de datos | MySQL                                    | Persistencia de todo el sistema                                                     |

La app móvil NUNCA se conecta directo a la base de datos. Todo pasa por la API.

## Módulos funcionales

1. Registro de propietarios y vehículos
2. Órdenes de servicio con diagnóstico, prioridad y estado
3. Presupuestos de mano de obra y repuestos, sujetos a aprobación
4. Asignación de técnicos y control de tareas
5. Inventario de repuestos, compras, pagos y entrega de vehículos

Cada módulo principal debe tener: CRUD, búsqueda con filtros y al menos un reporte.

## Reglas de negocio (nunca deben romperse)

1. Un vehículo no puede tener dos órdenes activas del mismo tipo de atención sin autorización.
2. No se inician reparaciones facturables sin presupuesto aprobado.
3. Los repuestos se descuentan del inventario solo al registrar su consumo real en una orden.
4. Una orden pasa a ENTREGADA solo si está finalizada y tiene saldo cero o crédito autorizado.
5. Todo cambio de estado de una orden se registra en un historial con fecha y responsable.

Las reglas se validan en la capa de servicio del módulo `nucleo`, nunca solo en la
interfaz. Si una regla no se cumple, se lanza una excepción propia con un mensaje
claro en español.

## Arquitectura

Proyecto Gradle con varios módulos:

```
taller-automotriz/
├── nucleo/       → entidades, excepciones, DAO, servicios y reglas de negocio
├── escritorio/   → Swing con MVC (vistas y controladores). Depende de nucleo
├── api/          → Spring Boot (controladores REST y seguridad). Depende de nucleo
├── movil/        → proyecto Android independiente (se abre con Android Studio)
├── database/     → esquema.sql y datos_prueba.sql
└── docs/         → enunciado y documentación
```

Capas obligatorias: Vista → Controlador → Servicio → DAO → Base de datos.

- La vista no contiene lógica de negocio ni SQL.
- Las reglas de negocio viven en los servicios.
- El SQL vive únicamente en los DAO.

Patrones obligatorios:

- MVC en el escritorio.
- DAO para toda la persistencia: una interfaz por entidad y su implementación JDBC.
- Patrón adicional: State (aprobado). Cada estado de la orden es una clase que
  hereda de la clase abstracta `EstadoOrden` y sobrescribe las transiciones que permite.

## Decisiones aprobadas

- Roles: `ADMINISTRADOR`, `RECEPCIONISTA`, `TECNICO`, `ENCARGADO_INVENTARIO`.
  Solo `TECNICO` usa la app móvil y la API. Solo `ADMINISTRADOR` autoriza las
  excepciones de las reglas 1 y 4.
- Estados de la orden: `RECIBIDA` → `EN_DIAGNOSTICO` → `ESPERANDO_APROBACION` →
  `EN_REPARACION` → `FINALIZADA` → `ENTREGADA`. `CANCELADA` se alcanza desde cualquier
  estado anterior a `EN_REPARACION`. Orden activa = distinta de `ENTREGADA` y `CANCELADA`.
- Presupuesto: `PENDIENTE`, `APROBADO`, `RECHAZADO` (estado propio, separado de la orden).
- Presupuesto rechazado: la orden queda en `ESPERANDO_APROBACION` y recepción elige entre
  volver a `EN_DIAGNOSTICO` (para armar un presupuesto nuevo) o pasar a `CANCELADA`.
  Los presupuestos rechazados se conservan con estado `RECHAZADO`.
- Modelo de datos (21 tablas) aprobado y creado en `database/esquema.sql`. El saldo de una
  orden no se guarda: se calcula (total del presupuesto aprobado menos pagos).

## Interfaz de escritorio (Swing)

- Todas las pantallas se escriben en código Java. NO usar el diseñador visual de
  NetBeans ni archivos `.form` generados.
- Una clase por ventana o panel, dentro de `escritorio/.../vista/`
  (ej.: `FormularioCliente`, `ListaOrdenes`).
- Las vistas solo construyen la interfaz y muestran datos. Los eventos (clic en
  botones) se delegan al controlador correspondiente.
- Usar administradores de diseño estándar de Swing (BorderLayout, GridBagLayout,
  BoxLayout). No usar posiciones absolutas (setBounds con layout null).
- Apariencia con FlatLaf.
- Componentes reutilizables (encabezados, botones estándar, tablas con filtro) en
  `escritorio/.../vista/componentes/` para que todas las pantallas se vean iguales.
- Mensajes al usuario en español y claros, usando JOptionPane para confirmaciones
  y errores.

## Persistencia

- JDBC puro con consultas preparadas (`PreparedStatement`) en los DAO.
- PROHIBIDO usar JPA, Hibernate o Spring Data: el curso exige DAO y JDBC propios.
  En la API, Spring Boot se usa solo para la capa web y la seguridad.
- Mínimo 10 entidades con PK y FK. Entidades candidatas: Cliente, Vehículo, Marca,
  Modelo, OrdenServicio, Diagnóstico, Servicio, DetalleServicio, Técnico,
  Asignación, Repuesto, ConsumoRepuesto, Presupuesto, Pago. Se permite ampliar o
  normalizar el modelo.
- Operaciones encabezado-detalle y movimientos de inventario usan transacciones con
  `commit` y `rollback` ante cualquier error.
- Scripts en `database/`: `esquema.sql` (creación) y `datos_prueba.sql` (datos ficticios).

## Programación orientada a objetos (obligatorio y demostrable)

- Encapsulamiento en todas las entidades.
- Jerarquía de clases coherente con el dominio, con al menos una clase abstracta y
  métodos sobrescritos (polimorfismo).
- Composición o agregación en operaciones maestro-detalle.
- Excepciones propias para errores de negocio y de persistencia.

## Seguridad

- Autenticación con usuario y contraseña, y control de permisos por rol.
- Roles: `ADMINISTRADOR`, `RECEPCIONISTA`, `TECNICO` y `ENCARGADO_INVENTARIO`
  (ver "Decisiones aprobadas"). Cualquier cambio de roles se consulta a Daniel.
- Contraseñas guardadas con hash BCrypt. Nunca en texto plano.
- La API se autentica con tokens JWT.
- Validar todas las entradas del usuario antes de procesarlas.
- Registro básico de auditoría: quién hizo qué y cuándo.
- Credenciales de base de datos en archivos de configuración excluidos de Git. Se
  versiona solo un archivo de ejemplo (`*.example`) con valores ficticios.
- PROHIBIDO subir credenciales reales al repositorio. Solo datos ficticios.

## Librerías aprobadas

| Librería          | Uso                                |
| ----------------- | ---------------------------------- |
| MySQL Connector/J | Conexión a MySQL                   |
| jBCrypt           | Hash de contraseñas                |
| Gson              | Conversión de JSON (API y Android) |
| java-jwt (Auth0)  | Tokens de autenticación de la API  |
| Retrofit + OkHttp | Llamadas HTTP desde Android        |
| OpenPDF           | Exportar reportes a PDF            |
| Apache POI        | Exportar reportes a Excel          |
| FlatLaf           | Apariencia moderna de Swing        |
| JUnit 5           | Pruebas internas                   |

No agregar ninguna librería fuera de esta lista sin aprobación de Daniel.

## Reportes obligatorios

1. Órdenes por estado y antigüedad
2. Productividad por técnico
3. Repuestos de mayor consumo
4. Ingresos por servicio
5. Historial de mantenimiento por vehículo

Los reportes leen datos reales de la base de datos y deben poder verse en pantalla
y exportarse.

## Convenciones de código

- Todo en español: clases, métodos, variables, tablas, columnas y comentarios.
- Seguir las convenciones oficiales de Java: clases en PascalCase
  (`OrdenServicio`), métodos y variables en camelCase (`calcularSaldo`),
  constantes en MAYÚSCULAS (`IVA_GENERAL`).
- Tablas y columnas de MySQL en snake_case (`orden_servicio`, `fecha_ingreso`).
- Nombres descriptivos. Nada de abreviaturas confusas (`os`, `tmp`, `x`).
- Métodos cortos con una sola responsabilidad.
- Comentarios solo donde el código no se explica por sí mismo.

## Pruebas

- Escribir pruebas con JUnit 5 para servicios, reglas de negocio y DAO.
- Las pruebas son para control interno del desarrollo y NO forman parte del
  entregable. Viven en `src/test/` de cada módulo y se excluyen al preparar la entrega.
- Toda regla de negocio debe tener al menos: una prueba válida y una prueba que
  demuestre que la regla bloquea la operación inválida.

## Comandos

Requieren JDK 21 (`JAVA_HOME` apuntando a él). Se usa el wrapper `./gradlew`.

- Compilar: `./gradlew build`
- Ejecutar escritorio: `./gradlew :escritorio:run`
- Ejecutar API: `./gradlew :api:bootRun`
- Ejecutar pruebas: `./gradlew test`

## Definición de terminado

Una tarea está terminada cuando:

1. Compila sin errores ni advertencias nuevas.
2. Sus pruebas pasan.
3. Las reglas de negocio afectadas se validan en el servicio.
4. Se explicó a Daniel qué se hizo y por qué.

## Forma de trabajar con Daniel

- Responder siempre en español, con palabras simples. Usar términos técnicos solo
  cuando sean necesarios.
- Antes de implementar algo grande, proponer el plan y esperar aprobación.
- Después de cada cambio importante, explicar en el chat qué se hizo y por qué.
  Daniel defenderá el proyecto y necesita entender cada parte.
- Las explicaciones van en el chat, no dentro de los archivos del proyecto.
- No generar diagramas a menos que Daniel los pida.

## Cuándo preguntar antes de actuar

- Cualquier cambio en el modelo de datos (tablas, columnas, relaciones).
- Cualquier cambio o interpretación de una regla de negocio.
- Cambios a los roles, los estados de la orden o el patrón adicional ya aprobados
  (ver "Decisiones aprobadas").
- Agregar librerías o cambiar versiones.
- Cuando el enunciado sea ambiguo: preguntar, no suponer.

## Prohibiciones

- No usar JPA, Hibernate ni Spring Data.
- No conectar la app móvil directo a la base de datos.
- No poner SQL fuera de los DAO ni lógica de negocio en las vistas.
- No subir credenciales reales ni datos personales reales.
- No borrar ni modificar scripts de base de datos ya aplicados sin consultar.
- No cambiar la versión de Java ni de las librerías sin aprobación.
