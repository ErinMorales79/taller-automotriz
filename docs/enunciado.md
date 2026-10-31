# Enunciado del proyecto: Taller Automotriz y Órdenes de Servicio

## Lineamientos generales

### Requisitos comunes obligatorios

- Java como lenguaje principal; interfaz de escritorio con Swing (opción propuesta) y una aplicación móvil Android desarrollada en Java.
- Mínimo 10 entidades persistentes relacionadas con PK y FK; diagrama entidad-relación y script de creación y datos de prueba.
- POO demostrable: encapsulamiento, herencia, composición o agregación, clase abstracta, polimorfismo y excepciones propias.
- En la segunda entrega, operar con ArrayList u otras listas de objetos sin persistencia en base de datos; posteriormente migrar a JDBC y DAO.
- CRUD, búsqueda con filtros y al menos un reporte útil en cada módulo principal; los reportes globales se detallan por proyecto.
- Autenticación y roles, contraseñas con hash seguro, consultas preparadas, validación de entradas y registro básico de auditoría.
- Patrones MVC y DAO, más un patrón adicional justificado (por ejemplo Singleton, Observer o Facade).
- La app móvil consumirá una API HTTP autenticada; no se conectará directamente a la base de datos desde el dispositivo.
- Manejo de transacciones en operaciones con encabezado-detalle o movimientos de inventario, con rollback ante errores.
- Código original, documentación y demostración con datos ficticios; prohibido utilizar credenciales reales en repositorios.

### Entregables

- **Entrega 1 - Análisis y diseño:** alcance, actores, requerimientos, reglas de negocio, casos de uso y diagrama UML de clases.
- **Entrega 2 - Java y POO:** navegación, formularios y operaciones funcionales en memoria con listas de objetos; evidencia de herencia, polimorfismo y excepciones.
- **Entrega 3 - Persistencia:** DER con 10 o más entidades, scripts SQL, JDBC, DAO, CRUD, reportes, validaciones y pruebas.
- **Entrega 4 - Integración:** app móvil conectada a API y base de datos del sistema, manuales, código, backup, pruebas y defensa.

## Instrucciones del proyecto

### 1. Planteamiento del problema

Un taller recibe vehículos, diagnostica averías, asigna técnicos y consume repuestos. Sin seguimiento centralizado, se pierden aprobaciones de presupuestos y se cobran servicios no ejecutados.

### 2. Objetivo del proyecto

Analizar, diseñar e implementar una solución integral que digitalice los procesos descritos, garantice consistencia de datos, permita consultas y reportes, y habilite una operación móvil específica.

### 3. Módulos y requerimientos funcionales

- Registro de propietarios y vehículos.
- Ingreso de órdenes con diagnóstico, prioridad y estado.
- Presupuestos de mano de obra y repuestos, sujetos a aprobación.
- Asignación de técnicos y control de tareas.
- Inventario de repuestos, compras, pagos y entrega de vehículos.

### 4. Reglas del negocio obligatorias

- Un vehículo no puede tener dos órdenes activas del mismo tipo de atención sin autorización.
- No iniciar reparaciones facturables sin aprobación del presupuesto.
- Los repuestos se descuentan al registrar su consumo real en una orden.
- Una orden solo pasa a ENTREGADA si está finalizada y tiene saldo cero o crédito autorizado.
- Registrar historial de cambios de estado con fecha y responsable.

### 5. Modelo de datos mínimo sugerido

Entidades candidatas (14): Cliente, Vehículo, Marca, Modelo, OrdenServicio, Diagnóstico, Servicio, DetalleServicio, Técnico, Asignación, Repuesto, ConsumoRepuesto, Presupuesto, Pago. El equipo definirá atributos, PK, FK, cardinalidades y restricciones; podrá ampliar o normalizar el modelo según su análisis.

### 6. Reportes obligatorios

- Órdenes por estado y antigüedad
- Productividad por técnico
- Repuestos de mayor consumo
- Ingresos por servicio
- Historial de mantenimiento por vehículo

### 7. Componente móvil obligatorio

Aplicación móvil para técnicos: ver órdenes asignadas, actualizar avance y adjuntar observaciones mediante API.

### 8. Aplicación de conceptos (obligatorio)

Identificar una jerarquía de clases coherente con el dominio; incluir una clase abstracta y sobrescritura de métodos; modelar composición o agregación en operaciones maestro-detalle; manejar errores con excepciones; implementar listas de objetos en la fase inicial y después DAO/JDBC; aplicar MVC, autenticación, validaciones y un patrón adicional documentado.

### 9. Casos de prueba y demostración

- Demostrar un flujo completo de inicio a fin con al menos tres registros relacionados.
- Probar una operación válida, una inválida por regla de negocio y una falla controlada de persistencia.
- Demostrar filtrado/búsqueda, reporte exportable o visualizable y control de permisos.
- Ejecutar desde el teléfono o emulador la funcionalidad móvil descrita y verificar sincronización con el sistema principal.

### 10. Aceptación

El proyecto se considera funcional cuando sus módulos principales ejecutan operaciones reales, las reglas de negocio se validan en el servidor o servicio, los datos persisten sin inconsistencias, los reportes reflejan los registros almacenados y la aplicación móvil completa su flujo asignado.
