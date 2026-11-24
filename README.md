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

**App móvil**
- Kotlin y Jetpack Compose con Material 3
- Arquitectura **MVVM** (Model, ViewModel, Repository) con `StateFlow`
- **Retrofit** + Gson y corrutinas para consumir los microservicios y la API externa
- **Room** (SQLite) como respaldo local cuando no hay conexión con el servidor
- Navigation Compose
- Diseño adaptable a pantallas compactas, medianas y expandidas
- Requisitos: Android 8.0 (API 26) o superior

**Backend** (carpeta `backend/`)
- 3 microservicios en **Spring Boot** (Java 21) con Lombok
- **MySQL** (XAMPP), una base de datos por microservicio
- Estructura Controller → Service → Repository, DTOs y manejo de errores con `GlobalExceptionHandler`

## Arquitectura

```
App Android (Retrofit)
 ├── ms-admin     → localhost:9091 → bd_admin     (usuarios y roles)
 ├── ms-reservas  → localhost:9092 → bd_reservas  (dependencias y reservas)
 ├── ms-finanzas  → localhost:9093 → bd_finanzas  (pagos y gastos)
 └── mindicador.cl (API externa: UF y dólar del día)
```

Desde el emulador, la app llega al computador con la dirección `10.0.2.2`. Si el servidor no responde, la app muestra los datos guardados en Room.

## Estructura

| Carpeta | Contenido |
|---|---|
| `app/.../model/` | Entidades y modelos: Usuario, Arriendo, Movimiento, Dependencia |
| `app/.../data/local/` | Base de datos Room, DAOs y conversores |
| `app/.../data/remote/` | `RetrofitInstance` y los `ApiService` de cada microservicio y de mindicador |
| `app/.../data/repository/` | Repositorios: primero el microservicio y Room como respaldo |
| `app/.../viewmodel/` | Lógica de cada pantalla y validaciones |
| `app/.../ui/screens/` | Pantallas |
| `app/.../ui/components/` | Componentes reutilizables |
| `backend/ms-admin/` | Microservicio de usuarios, login y registro |
| `backend/ms-reservas/` | Microservicio de dependencias y reservas (revisa choques de horario) |
| `backend/ms-finanzas/` | Microservicio de movimientos y resumen financiero |

`app/...` corresponde a `app/src/main/java/com/example/juntavecinosapp/`.

## Endpoints utilizados

### Microservicios propios

**ms-admin** (`http://localhost:9091`)

| Método | Endpoint | Uso |
|---|---|---|
| POST | `/auth/register` | Crear cuenta (siempre como Vecino) |
| POST | `/auth/login` | Iniciar sesión |
| GET | `/usuarios` | Listar usuarios |
| GET | `/usuarios/{id}` | Ver un usuario |
| PUT | `/usuarios/{id}` | Actualizar un usuario |
| DELETE | `/usuarios/{id}` | Eliminar un usuario |

**ms-reservas** (`http://localhost:9092`)

| Método | Endpoint | Uso |
|---|---|---|
| GET | `/dependencias` | Listar dependencias y tarifas |
| GET | `/reservas` | Listar reservas |
| GET | `/reservas/{id}` | Ver una reserva |
| POST | `/reservas` | Solicitar arriendo (calcula el monto y rechaza horarios ocupados con error 400) |
| PUT | `/reservas/{id}` | Actualizar una reserva |
| PATCH | `/reservas/{id}/estado` | Aprobar o rechazar (directiva) |
| DELETE | `/reservas/{id}` | Eliminar un arriendo (directiva) |
| GET | `/reservas/{id}/historial` | Historial de cambios de estado |

**ms-finanzas** (`http://localhost:9093`)

| Método | Endpoint | Uso |
|---|---|---|
| GET | `/movimientos` | Listar pagos y gastos |
| GET | `/movimientos/resumen` | Totales de entradas, salidas y saldo |
| GET | `/movimientos/{id}` | Ver un movimiento |
| POST | `/movimientos` | Registrar pago o gasto (tesorería) |
| PUT | `/movimientos/{id}` | Actualizar un movimiento |
| DELETE | `/movimientos/{id}` | Eliminar un movimiento |

### API externa

| Método | Endpoint | Uso |
|---|---|---|
| GET | `https://mindicador.cl/api` | Valor de la **UF** y del **dólar** del día, mostrados en la pantalla Transparencia |

## Cómo ejecutar

### 1. Base de datos
1. Abre **XAMPP** y presiona **Start** en **MySQL** (puerto 3306, usuario `root`, sin clave).
2. No hace falta crear las bases: cada microservicio crea la suya (`bd_admin`, `bd_reservas`, `bd_finanzas`) al iniciar.

### 2. Backend
1. Abre la carpeta `backend/` en **IntelliJ IDEA** (o cada microservicio por separado).
2. Ejecuta, en este orden, `MsAdminApplication`, `MsReservasApplication` y `MsFinanzasApplication`.
3. Para comprobar que están arriba, abre en el navegador:
   - `http://localhost:9091/usuarios`
   - `http://localhost:9092/dependencias`
   - `http://localhost:9093/movimientos`

Al iniciar, ms-admin crea los usuarios de prueba y ms-reservas las 3 dependencias.

### 3. App
1. Abre la carpeta del proyecto con **Android Studio** y espera el **Gradle Sync**.
2. Elige un emulador con Android 8.0 o superior.
3. Presiona **Run**. Los microservicios deben estar corriendo para trabajar en línea.

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

## APK firmado

- APK: `app/release/app-release.apk`
- Llave: `juntavecinos.jks` (en la raíz del proyecto), alias `juntavecinos`
- La configuración de firma está en `app/build.gradle.kts` (`signingConfigs` → `release`).

Para generarlo de nuevo: **Build → Generate Signed App Bundle or APK → APK**, elegir `juntavecinos.jks` y la variante **release**.

![APK firmado](capturas/apk-firmado.png)

![Llave .jks](capturas/jks.png)

## Organización del equipo
- Ramas: `main` (versión de entrega), `integracion-api` (conexión con los microservicios), `isa` y `Andy` (trabajo de cada integrante).
- Planificación en Trello: [Tablero Junta Vecinos App](https://trello.com/invite/b/6acadb02afb1f21bc08ceb2f/ATTI12efb442b61da3ec6a8ff5d8e5e83de56DF8F2F9/junta-vecinos-app)

## Limitaciones conocidas
- La sesión no se mantiene al cerrar la app.
- El backend corre en el computador (localhost); para usarlo desde un celular físico habría que cambiar la dirección `10.0.2.2` por la IP del computador.
- No hay pagos reales ni integración bancaria, tal como indica el caso.
