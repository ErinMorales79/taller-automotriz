# API para la app móvil

API HTTP del taller. Solo la usan los **técnicos** desde la app Android. Todo pasa por los servicios de
`nucleo`: la app nunca toca la base de datos.

## Cómo ejecutarla

1. Copia `config/api.properties.example` como `config/api.properties` y cambia `taller.jwt.secreto`
   (mínimo 32 caracteres, aleatorio). Ese archivo no se sube a Git.
2. Necesita también `config/conexion.properties` (el mismo que usa el escritorio).
3. `./gradlew :api:bootRun` (puerto 8080 por defecto, configurable con `server.port`).

Si falta el secreto o es corto, la API no arranca y dice por qué.

## Autenticación

`POST /api/auth/login` devuelve un token JWT. En todas las demás rutas se envía en la cabecera:

```
Authorization: Bearer <token>
```

El token dura `taller.jwt.horas` horas (8 por defecto). En cada llamada la API vuelve a comprobar en la
base de datos que el usuario siga activo y sea técnico, así que desactivar a un usuario corta su acceso
aunque su token no haya vencido.

## Rutas

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| POST | `/api/auth/login` | `{"usuario":"tecnico1","contrasena":"Tecnico2026"}` | 200 `{token, expiraEn, usuario, tecnico}` |
| GET | `/api/ordenes/asignadas` | - | 200 lista de tareas (`TareaDto`) |
| GET | `/api/ordenes/{idOrden}` | - | 200 detalle de la orden con diagnósticos y observaciones |
| PUT | `/api/asignaciones/{idAsignacion}/avance` | `{"porcentaje":60}` | 200 la tarea actualizada |
| POST | `/api/ordenes/{idOrden}/observaciones` | `{"texto":"Se cambió el filtro"}` | 201 la observación creada |

Ejemplo de `TareaDto`:

```json
{
  "idAsignacion": 12, "idOrden": 5, "placa": "AAA-1001", "vehiculo": "Toyota Corolla",
  "tipoAtencion": "Reparación de frenos", "prioridad": "ALTA", "estadoOrden": "EN_REPARACION",
  "estadoTarea": "EN_CURSO", "porcentajeAvance": 40, "fechaAsignacion": "2026-10-29T18:24:00"
}
```

Las fechas van en formato ISO 8601. El técnico solo ve y modifica lo que tiene asignado.

## Errores

Siempre JSON con un mensaje en español, sin detalles internos: `{"mensaje": "..."}`.

| Código | Cuándo |
|---|---|
| 400 | Una regla de negocio no se cumple (por ejemplo avance fuera de 0 a 100, observación vacía u orden cerrada) o el JSON está mal formado |
| 401 | Credenciales incorrectas (mismo mensaje exista o no el usuario), token ausente, falso o vencido, o usuario desactivado |
| 403 | El usuario no es técnico, o la orden o tarea no es suya |
| 500 | Falla inesperada o de base de datos (el detalle queda en el registro del servidor) |
