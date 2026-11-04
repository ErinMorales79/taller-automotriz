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
R//tiene mi visto bueno
---

## Fase 6: escritorio (para tu visto bueno)

- **Librería nueva aprobada por ti:** `flatlaf-extras` 3.5.4 (iconos SVG). Los iconos están en
  `escritorio/src/main/resources/iconos/` y los dibujé yo (estilo de trazo simple).
- **Cambios en `nucleo` (sin tocar el modelo de datos):** `ConsultaServicio` (consultas de solo lectura
  para que la interfaz no use los DAO) y `DiagnosticoServicio`. Este último es una **regla nueva que
  interpreté yo**: un diagnóstico solo se registra mientras la orden está `EN_DIAGNOSTICO` y lo puede
  registrar quien tenga `GESTIONAR_ORDEN` (admin y recepción). Antes no había forma de crear diagnósticos.
- **Autorización de excepciones (reglas 1 y 4):** si quien trabaja no es administrador, el sistema pide
  usuario y contraseña de un administrador en una ventana; si ya es administrador, solo confirma.
- **Menú por rol:** cada rol ve solo sus módulos. Marcas y modelos van dentro de "Vehículos" (permiso de
  recepción); técnicos, servicios y tipos de atención en "Catálogos" (solo admin); proveedores y compras
  dentro de "Inventario".
- **Cambiar contraseña propia:** botón con candado en el menú lateral (cualquier rol).
- **Riesgo conocido (ya existía en `nucleo`):** `RepuestoDaoJdbc.actualizar` escribe también el stock. La
  pantalla de repuestos no deja editarlo (se muestra de solo lectura), pero si dos personas editan el mismo
  repuesto a la vez, una edición podría pisar un cambio de stock reciente. Se arregla quitando `stock` del
  `UPDATE` en una próxima fase; no lo cambié sin consultarte.
- **Avisos del compilador:** en `escritorio` se apagaron solo `-Xlint:-serial` y `-Xlint:-this-escape`
  porque todo componente Swing es `Serializable` y genera avisos que no aplican.
- **Pruebas de pantallas:** `PantallasTest` carga `datos_prueba.sql` dentro de la transacción de la prueba
  (se revierte), igual que `DatosPruebaTest`: la base de pruebas debe estar vacía. Con
  `./gradlew :escritorio:test -Dtaller.capturas=<carpeta>` guarda una imagen de cada pantalla
- R//tiene mi visto bueno

---

## Fase 7: API (para tu visto bueno)

- **Librerías aprobadas por ti:** `java-jwt` 4.4.0 y `spring-boot-starter-test` (versión que gestiona Spring
  Boot 3.4.1). Seguridad con un filtro propio, sin Spring Security. JSON con Jackson (viene en Spring Boot).
- **Cambios en `nucleo` (sin tocar el modelo de datos):** `TareaServicio.asignacionesDelTecnico`,
  `ordenAsignada` y `agregarObservacion`; `AutenticacionServicio.usuarioActivo`.
  **Regla nueva que interpreté yo:** una observación solo se agrega si el técnico está asignado a la orden
  y la orden sigue activa. Va auditada (`AGREGAR_OBSERVACION`) y tiene pruebas.
- **Solo técnicos:** el login con otro rol responde 403 y no entrega token. Ojo: el intento correcto de un
  no técnico sí queda en la auditoría como `LOGIN`, porque la autenticación ya ocurrió.
- **Token:** dura 8 horas y no lleva el rol. En cada petición se consulta la base para confirmar que el
  usuario esté activo y sea técnico, así que un cambio de rol o una desactivación surten efecto de inmediato.
  A cambio hay una consulta por petición.
- **Sin límite de intentos de login:** la API no bloquea tras varios intentos fallidos (fuera de alcance).
  Todos los intentos quedan auditados. Si lo quieres, es una decisión para una fase posterior.
- **Sin HTTPS:** la API responde HTTP simple. Para la defensa en local sirve; en una instalación real debe ir
  detrás de HTTPS, porque el token viaja en una cabecera.
- **Secreto:** va en `config/api.properties` (ignorado por Git); se versiona solo el `.example`.
- **Prueba no hecha:** no pude arrancar la API real con `curl` (el sistema bloqueó el comando). Lo cubren las
  13 pruebas de `ApiTecnicosTest` con `MockMvc`; conviene que la arranques una vez con tu `api.properties`.
- Documentación de rutas para Android en `docs/api.md`.

---

## Fase 8: app móvil (para tu visto bueno)

- **SDK de Android instalado por mí** (a tu pedido) en `C:\Users\erinm\AppData\Local\Android\Sdk`: herramientas de
  línea de comandos 11076708, plataforma 35, build-tools 35.0.0 y platform-tools. **Acepté las licencias del SDK** al
  instalarlo. No instalé el emulador ni imágenes de sistema (pesan varios GB); Android Studio los baja desde
  *Device Manager*. `movil/local.properties` (ignorado por Git) apunta a esa ruta.
- **Versiones usadas** (las que aprobaste): Retrofit 2.11.0, OkHttp 4.12.0, Gson 2.11.0, plugin de Android Gradle 8.7.3,
  `compileSdk` 35, `minSdk` 26, Java 17. Las que yo elegí dentro de lo aprobado: AppCompat 1.7.0, Material 1.12.0,
  RecyclerView 1.3.2, SwipeRefreshLayout 1.1.0, `security-crypto` 1.1.0 y JUnit 5.11.4 (igual que el resto del proyecto).
  Android lint avisa que existen versiones más nuevas de AppCompat, Material, etc.; no las cambié sin tu aprobación.
- **Tráfico HTTP permitido** en toda la app (`configuracion_red.xml`) porque la API local no usa HTTPS. Solo para la
  demostración; ver `docs/movil.md`.
- **`security-crypto` obsoleta:** Google la marcó así en la 1.1.0. Funciona, pero conviene migrar al Keystore directo si
  la app va a mantenerse.
- **Lo que NO pude verificar:** no hay emulador en esta máquina, así que no ejecuté la app. Compila, genera el APK, pasa
  lint sin errores y sus 10 pruebas de red pasan, pero el aspecto de las pantallas y el flujo completo contra la API real
  solo se ven al correrla. Pruébalo con el guion de `docs/movil.md`.
- **Sin pruebas de pantallas:** solo hay pruebas de la capa de red (servidor simulado). La interfaz se revisa a mano.
