# Junta de Vecinos Población Zaror: Arriendos y Transparencia Financiera

Aplicación móvil Android para gestionar el arriendo de las dependencias de la sede de la Junta de Vecinos Población Zaror y mostrar con transparencia el uso de sus fondos.

Proyecto de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles**

> Todos los datos de la app (usuarios, montos, tarifas y arriendos) son **ficticios**. La app no se conecta a bancos ni a servicios de pago.

## Integrantes

| Integrante | GitHub | Área a cargo |
|---|---|---|
| Isabel Cantaro | [@ISXOOX2](https://github.com/ISXOOX2) | Acceso, roles y arriendos |
| Andeison Belandria | [@Kratos19-11](https://github.com/Kratos19-11) | Finanzas y transparencia |

## Funcionalidades

### Acceso y roles
- Inicio de sesión y creación de cuenta, con validación en cada campo.
- Tres roles: **Vecino**, **Directiva** y **Tesorería**.
- Quien crea una cuenta queda siempre como **Vecino**.
- El menú principal cambia según el rol.

### Arriendos
- **Solicitar arriendo:** dependencia, fecha, hora de inicio, hora de término y motivo. Calcula el monto estimado según la tarifa por hora.
- **Disponibilidad:** lista de arriendos con filtros por estado (pendiente, aprobado, rechazado).
- **Detalle:** la directiva aprueba o rechaza las solicitudes pendientes, con confirmación.
- No permite reservar una dependencia en un horario ya ocupado.

| Dependencia | Tarifa por hora (ficticia) |
|---|---|
| Salón multiuso | $12.000 |
| Sala de reuniones | $6.000 |
| Cancha techada | $9.000 |

### Finanzas y transparencia
- **Registrar pago** y **registrar gasto**, con foto del comprobante tomada con la cámara o elegida de la galería.
- **Historial** de movimientos con filtros por tipo y eliminación con confirmación.
- **Transparencia:** saldo, desglose y uso de los fondos.
- **Reporte** por período, con gráfico de barras animado y opción de compartir el resumen.

## Validaciones
Las reglas están en los **ViewModels**, separadas de la interfaz. Los campos muestran el error con ícono y mensaje animado, y el teléfono vibra al guardar o al fallar. Ejemplos:
- Correo con formato válido y sin repetir; clave de al menos 6 caracteres con letras y números.
- Fecha de arriendo no pasada y dentro de los próximos 6 meses.
- Horario entre las 08:00 y las 22:00, con duración de 1 a 8 horas.
- Motivo de 10 a 200 caracteres.

## Recursos nativos del dispositivo
1. **Cámara y galería**, con permisos solicitados en tiempo de ejecución, para los comprobantes.
2. **Vibración**, como respuesta a formularios guardados o con errores.
3. **Compartir** (Intent del sistema), para enviar el resumen del reporte.

## Tecnologías
- Kotlin y Jetpack Compose con Material 3
- Arquitectura **MVVM** (Model, ViewModel, Repository) con `StateFlow`
- **Room** (SQLite) para la persistencia local
- Navigation Compose
- Diseño adaptable a pantallas compactas, medianas y expandidas
- Requisitos: Android 8.0 (API 26) o superior

## Estructura

Dentro de `app/src/main/java/com/example/juntavecinosapp/`:

| Carpeta | Contenido |
|---|---|
| `model/` | Entidades y modelos: Usuario, Arriendo, Movimiento, Dependencia |
| `data/local/` | Base de datos Room, DAOs y conversores |
| `data/repository/` | Repositorios sobre los DAOs |
| `viewmodel/` | Lógica de cada pantalla y validaciones |
| `ui/screens/` | Pantallas |
| `ui/components/` | Componentes reutilizables |
| `ui/theme/`, `ui/utils/` | Tema, formatos, vibración y tamaño de pantalla |
| `ui/AppNavigation.kt` | Rutas de navegación |

## Cómo ejecutar

1. Clona el repositorio: `https://github.com/ISXOOX2/JuntaVecinosApp`
2. Ábrelo con **Android Studio** y confía en el proyecto si lo pregunta.
3. Espera a que termine el **Gradle Sync**.
4. Elige un emulador (o un celular con depuración USB) con Android 8.0 o superior.
5. Presiona **Run**.

Si ya habías instalado una versión anterior de la app, **desinstálala o limpia sus datos** (Device Manager → Wipe Data) para que se creen los usuarios de prueba.

## Usuarios de prueba

| Rol | Correo | Clave |
|---|---|---|
| Vecino | vecino@junta.cl | vecino123 |
| Directiva | directiva@junta.cl | directiva123 |
| Tesorería | tesoreria@junta.cl | tesoreria123 |

También puedes crear más vecinos con **Crear cuenta**.

## Qué puede hacer cada rol

| Función | Vecino | Directiva | Tesorería |
|---|---|---|---|
| Solicitar arriendo | Sí | Sí | No |
| Ver disponibilidad | Sí | Sí | Sí |
| Aprobar o rechazar arriendos | No | Sí | No |
| Ver transparencia | Sí | Sí | Sí |
| Registrar pagos y gastos | No | No | Sí |
| Historial y reporte | No | Sí | Sí |

## Flujo de ejemplo para probar
1. Entra como **vecino** y solicita un arriendo.
2. Entra como **directiva** y apruébalo en *Disponibilidad → Detalle*.
3. Entra como **tesorería** y registra un pago con foto de comprobante.
4. Revisa *Transparencia* y *Reporte*.

## Organización del equipo
- Ramas: `main` (versión de entrega), `isa` y `Andy` (trabajo de cada integrante).
- Planificación en Trello: [Tablero Junta Vecinos App](https://trello.com/invite/b/6acadb02afb1f21bc08ceb2f/ATTI12efb442b61da3ec6a8ff5d8e5e83de56DF8F2F9/junta-vecinos-app)

## Limitaciones conocidas
- Las claves se guardan sin cifrar y la sesión no se mantiene al cerrar la app.
- No hay pagos reales ni integración bancaria, tal como indica el caso.