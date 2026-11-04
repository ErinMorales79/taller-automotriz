# App móvil para técnicos

App Android (Java 17) que consume la API del taller (ver `docs/api.md`). El técnico inicia sesión, ve sus órdenes
asignadas, actualiza su avance y agrega observaciones. Nunca se conecta a la base de datos.

## Abrirla y compilarla

- Con Android Studio: abre la carpeta `movil/` (es un proyecto independiente, no forma parte del Gradle de la raíz).
- Por consola, desde `movil/` (necesita el SDK de Android y JDK 21 en `JAVA_HOME`):
  - `./gradlew :app:assembleDebug` genera `app/build/outputs/apk/debug/app-debug.apk`.
  - `./gradlew :app:testDebugUnitTest` corre las pruebas de la capa de red (en la JVM, sin emulador).
- `movil/local.properties` indica dónde está el SDK (`sdk.dir=...`). Android Studio lo crea solo; Git lo ignora.

## Guion de demostración

1. Base de datos con datos de prueba: carga `database/datos_prueba.sql` en la base real (una sola vez).
2. Configura y arranca la API: copia `config/api.properties.example` como `config/api.properties`, cambia el secreto y
   ejecuta `./gradlew :api:bootRun`.
3. Arranca la app de escritorio (`./gradlew :escritorio:run`) con `admin` / `Admin2026` para mostrar el lado del taller.
4. En el emulador (Android Studio, Device Manager) instala y abre la app. En la pantalla de acceso:
   - Servidor: `10.0.2.2:8080` (en el emulador es el computador). En un teléfono real, la IP de tu computador en la red
     Wi-Fi (por ejemplo `192.168.1.20:8080`), con la API y el teléfono en la misma red y el puerto 8080 abierto en el firewall.
   - Usuario `tecnico1`, contraseña `Tecnico2026`.
5. Abre una orden, mueve el avance y pulsa **Guardar avance**; escribe una observación y pulsa **Agregar observación**.
6. En el escritorio, abre esa orden (Órdenes, doble clic) y revisa la pestaña **Técnicos**: ahí aparecen el avance y la
   observación hechos desde el móvil. También quedan en la auditoría.

## Cómo está armada

| Carpeta | Qué contiene |
|---|---|
| `red/` | Interfaz Retrofit (`ApiTaller`), modelos JSON (`Modelos`), cliente con el token (`ClienteApi`) y `Resultado`, que convierte respuestas y fallos en mensajes en español |
| `datos/` | `AlmacenSesion`: token y servidor guardados cifrados |
| `ui/` | `LoginActivity`, `OrdenesActivity`, `DetalleOrdenActivity` y el adaptador de la lista |
| `util/` | Validación de la dirección del servidor y formato de textos y fechas |

Las reglas de negocio las valida el servidor; la app solo muestra el mensaje que la API devuelve. Si la API responde 401
la app borra la sesión y vuelve al acceso.

## Limitaciones conocidas

- La API usa HTTP sin cifrar, así que la app permite tráfico HTTP (`res/xml/configuracion_red.xml`). Es para la demostración
  en red local; en una instalación real se debe servir la API por HTTPS y quitar esa excepción.
- Solo hay pruebas unitarias de la capa de red. Las pantallas no tienen pruebas automáticas: se revisan en el emulador.
- `security-crypto` está marcada como obsoleta por Google desde la 1.1.0; sigue funcionando (ver comentario en `AlmacenSesion`).
