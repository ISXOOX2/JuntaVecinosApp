# Backend Junta de Vecinos Población Zaror (DSY1105)

Son tres microservicios Spring Boot 4 (Java 21). Cada uno es un proyecto Maven independiente y tiene su propia base MySQL. Siguen el estilo del ejemplo del profesor: model, dto, repository, service, controller y exception.

```
JuntaVecinosBackend/
├── ms-admin/          puerto 9091  bd_admin     usuarios, roles, login
├── ms-reservas/       puerto 9092  bd_reservas  dependencias, reservas, historial
├── ms-finanzas/       puerto 9093  bd_finanzas  movimientos y resumen
├── configuracion-maven/settings.xml   mirror de Maven (por si falla la descarga)
└── postman_collection.json
```

## Requisitos

- JDK 21
- VS Code con **Extension Pack for Java** y **Spring Boot Extension Pack**
- XAMPP, con MySQL encendido

## Cómo levantar los servicios

1. Abre el **XAMPP Control Panel** y presiona **Start** en **MySQL**. No hace falta crear las bases, porque cada servicio crea la suya al arrancar (`createDatabaseIfNotExist=true`). Se usa el usuario `root` sin clave.
2. En VS Code, ve a **File > Open Folder** y abre la carpeta de **un** servicio, por ejemplo `ms-admin`. Abre una ventana por servicio para que cada uno cargue su propio `pom.xml`.
3. Espera a que la extensión de Java termine de importar el proyecto. Puedes ver el avance en la barra inferior.
4. Abre `src/main/java/com/example/ms_admin/MsAdminApplication.java` y haz clic en **Run** encima del método `main`. También puedes usar el panel **Spring Boot Dashboard**.
5. Repite lo mismo con `ms-reservas` (`MsReservasApplication`) y `ms-finanzas` (`MsFinanzasApplication`).

Otra forma es usar la terminal, dentro de la carpeta de cada servicio:

```
mvnw.cmd spring-boot:run      (Windows)
./mvnw spring-boot:run        (Mac/Linux)
```

| Servicio | URL en el PC | URL desde el emulador Android |
|---|---|---|
| ms-admin | http://localhost:9091 | http://10.0.2.2:9091 |
| ms-reservas | http://localhost:9092 | http://10.0.2.2:9092 |
| ms-finanzas | http://localhost:9093 | http://10.0.2.2:9093 |

Al arrancar por primera vez, cada **DataLoader** carga los datos de prueba (si ya existen, no los duplica):

- ms-admin: los roles VECINO, DIRECTIVA y TESORERIA, y los usuarios `vecino@junta.cl / vecino123`, `directiva@junta.cl / directiva123` y `tesoreria@junta.cl / tesoreria123`.
- ms-reservas: Salón multiuso (12000/h), Sala de reuniones (6000/h) y Cancha techada (9000/h).

## Ver las bases en phpMyAdmin

Con XAMPP encendido, entra a http://localhost/phpmyadmin. A la izquierda aparecen `bd_admin`, `bd_reservas` y `bd_finanzas`. Las tablas (`usuario`, `rol`, `dependencia`, `reserva`, `historial_reserva` y `movimiento`) las crea Hibernate automáticamente (`ddl-auto=update`). En la tabla `usuario` puedes comprobar que la clave está guardada como hash (`password_hash`) y no en texto plano.

## Endpoints

### ms-admin (9091)

| Método | Ruta | Descripción |
|---|---|---|
| POST | /auth/register | Registra un vecino (el rol siempre es VECINO). 400 si el correo ya existe |
| POST | /auth/login | Devuelve el usuario sin hash. 400 si el correo o la clave no coinciden |
| GET | /usuarios | Lista los usuarios |
| GET | /usuarios/{id} | Busca un usuario |
| PUT | /usuarios/{id} | Edita nombre, email, rol y activo |
| DELETE | /usuarios/{id} | Elimina un usuario |

```json
POST /auth/login
{ "email": "vecino@junta.cl", "password": "vecino123" }

200 OK
{ "id": 1, "nombre": "Vecino Prueba", "email": "vecino@junta.cl", "rol": "VECINO", "activo": true }
```

### ms-reservas (9092)

| Método | Ruta | Descripción |
|---|---|---|
| GET | /dependencias | Lista las dependencias y sus tarifas |
| GET | /reservas?usuarioId=1 | Lista las reservas (el filtro `usuarioId` es opcional) |
| GET | /reservas/{id} | Busca una reserva |
| POST | /reservas | Crea una reserva PENDIENTE con el monto calculado. 400 si `horaFin <= horaInicio`; también 400 si choca con otra |
| PUT | /reservas/{id} | Modifica la reserva y recalcula el monto |
| PATCH | /reservas/{id}/estado | Cambia el estado (APROBADO / RECHAZADO / PENDIENTE) y lo guarda en el historial |
| DELETE | /reservas/{id} | Elimina la reserva y su historial |
| GET | /reservas/{id}/historial | Muestra los cambios de estado |

```json
POST /reservas
{ "usuarioId": 1, "dependencia": "Salón multiuso", "fecha": "2026-12-05",
  "horaInicio": "18:00", "horaFin": "20:30", "motivo": "Cumpleaños" }

201 Created
{ "id": 1, "usuarioId": 1, "dependencia": "Salón multiuso", "fecha": "2026-12-05",
  "horaInicio": "18:00", "horaFin": "20:30", "motivo": "Cumpleaños",
  "monto": 30000, "estado": "PENDIENTE" }

PATCH /reservas/1/estado
{ "estado": "APROBADO" }
```

### ms-finanzas (9093)

| Método | Ruta | Descripción |
|---|---|---|
| GET | /movimientos?tipo=GASTO | Lista los movimientos (el filtro `tipo` es opcional) |
| GET | /movimientos/{id} | Busca un movimiento |
| POST | /movimientos | Registra un PAGO, INGRESO o GASTO (`monto` debe ser > 0) |
| PUT | /movimientos/{id} | Edita un movimiento |
| DELETE | /movimientos/{id} | Elimina un movimiento |
| GET | /movimientos/resumen | Devuelve totalIngresos (PAGO + INGRESO), totalGastos y saldo |

```json
POST /movimientos
{ "tipo": "GASTO", "concepto": "Ampolletas sede", "monto": 25000,
  "fecha": 1764950400000, "persona": "Tesorería Prueba", "descripcion": "Cambio de luces" }

GET /movimientos/resumen
{ "totalIngresos": 180000, "totalGastos": 25000, "saldo": 155000 }
```

`fecha` en movimientos va en milisegundos (Long), igual que en la app.

Todos los errores responden con el mismo formato:

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request",
  "message": "La hora de término debe ser posterior a la de inicio", "path": "/reservas" }
```

El contrato completo está en `contrato-api-v2.md`.

## Probar con Postman

En Postman, ve a **Import** y elige `postman_collection.json`. Las variables `admin`, `reservas` y `finanzas` ya apuntan a localhost:9091, 9092 y 9093. La colección incluye casos de error (400 y 404) para mostrarlos en el video.

## Si Maven no logra descargar las dependencias

Esto le pasa a veces a la red de Duoc con Maven Central. El wrapper (`.mvn/wrapper/maven-wrapper.properties`) ya descarga Maven desde el mirror de Google. Si las dependencias igual fallan:

1. Copia `configuracion-maven/settings.xml` a la carpeta `.m2` de tu usuario: `C:\Users\<tu_usuario>\.m2\settings.xml` en Windows o `~/.m2/settings.xml` en Mac/Linux. Si ya tienes un `settings.xml`, agrega solo el bloque `<mirror>`.
2. En VS Code, abre la paleta con Ctrl+Shift+P y ejecuta **Java: Clean Java Language Server Workspace**. Otra opción es compilar desde la terminal con:
   ```
   mvnw.cmd -s ..\configuracion-maven\settings.xml clean package -DskipTests
   ```

## Notas

- La clave se guarda con hash SHA-1, como en el `HashService` del profesor. En producción se usaría BCrypt.
- El test por defecto (`contextLoads`) levanta el contexto completo, así que **necesita MySQL encendido**. Las pruebas unitarias que pide la pauta son de la app Android.
