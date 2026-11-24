# Contrato API v2 — Junta de Vecinos Población Zaror

Tres microservicios Spring Boot independientes, cada uno con su base MySQL (XAMPP).

| Servicio | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| ms-admin | 9091 | bd_admin | Usuarios, roles, registro y login |
| ms-reservas | 9092 | bd_reservas | Dependencias, reservas (arriendos), historial de estados |
| ms-finanzas | 9093 | bd_finanzas | Movimientos (PAGO / INGRESO / GASTO) y resumen |

URL base desde el emulador de Android: `http://10.0.2.2:<puerto>`. Desde el PC o Postman: `http://localhost:<puerto>`.

## Convenciones

- JSON en UTF-8. Campos en camelCase.
- Reservas: `fecha` en formato `"yyyy-MM-dd"` y horas `"HH:mm"`. En el historial, `fechaCambio` va como `"yyyy-MM-dd HH:mm:ss"`.
- Movimientos: `fecha` es un `Long` en milisegundos (epoch), igual que `Movimiento.fecha` en la app.
- Estados de una reserva: `PENDIENTE`, `APROBADO`, `RECHAZADO`.
- Tipos de movimiento: `PAGO`, `INGRESO`, `GASTO`.
- Roles: `VECINO`, `DIRECTIVA`, `TESORERIA`.
- Respuestas: 200 OK, 201 Created (POST que crea algo), 204 No Content (DELETE).
- Todos los errores usan el mismo formato:

```json
{
  "timestamp": "2026-11-18T15:30:00.123-03:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Ese horario ya está ocupado en Salón multiuso",
  "path": "/reservas"
}
```

| Código | Cuándo |
|---|---|
| 400 | Faltan datos, formato inválido, `horaFin <= horaInicio`, `monto <= 0`, tipo, estado o rol inválido, dependencia inexistente |
| 404 | El id no existe o la ruta no existe |
| 400 | También: correo ya registrado, login fallido o horario que choca con otra reserva |
| 500 | Error inesperado |

---

## ms-admin (9091)

Usuario que devuelve la API (nunca incluye la clave ni su hash):

```json
{ "id": 1, "nombre": "Vecino Prueba", "email": "vecino@junta.cl", "rol": "VECINO", "activo": true }
```

> En la app el campo se llama `correo`; en la API se llama `email`. Hay que mapearlo en el DTO de Retrofit.

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| POST | /auth/register | `{"nombre","email","password"}` | 201 Usuario (rol siempre VECINO) · 400 |
| POST | /auth/login | `{"email","password"}` | 200 Usuario · 400 si falla · 400 si faltan campos |
| GET | /usuarios | — | 200 `[Usuario]` |
| GET | /usuarios/{id} | — | 200 Usuario · 404 |
| PUT | /usuarios/{id} | `{"nombre","email","rol","activo"}` (todos opcionales) | 200 Usuario · 400 rol inválido · 404 |
| DELETE | /usuarios/{id} | — | 204 · 404 |

La clave se guarda como hash SHA-1, igual que en el ejemplo del profesor. En producción se usaría BCrypt. El email se guarda en minúsculas y sin espacios.

Usuarios de prueba (los crea DataLoader solo si no existen):

| Email | Clave | Rol |
|---|---|---|
| vecino@junta.cl | vecino123 | VECINO |
| directiva@junta.cl | directiva123 | DIRECTIVA |
| tesoreria@junta.cl | tesoreria123 | TESORERIA |

Ejemplo de registro:

```json
POST /auth/register
{ "nombre": "Juan Pérez", "email": "juan@junta.cl", "password": "juan123" }
→ 201 { "id": 4, "nombre": "Juan Pérez", "email": "juan@junta.cl", "rol": "VECINO", "activo": true }
```

---

## ms-reservas (9092)

Dependencia (DataLoader crea las 3 de la app):

```json
{ "id": 1, "nombre": "Salón multiuso", "descripcion": "Espacio amplio para eventos y reuniones grandes", "tarifaPorHora": 12000, "activa": true }
```

| Nombre | Tarifa por hora |
|---|---|
| Salón multiuso | 12000 |
| Sala de reuniones | 6000 |
| Cancha techada | 9000 |

Reserva (en la app se llama `Arriendo`):

```json
{
  "id": 1, "usuarioId": 1, "dependencia": "Salón multiuso",
  "fecha": "2026-12-05", "horaInicio": "18:00", "horaFin": "20:30",
  "motivo": "Cumpleaños", "monto": 30000, "estado": "PENDIENTE"
}
```

- `dependencia` es el **nombre** de la dependencia, igual que en `Arriendo.dependencia` de la app.
- El servidor calcula `monto` con la misma fórmula que la app: `tarifaPorHora * minutos / 60`. Si el cliente manda un monto, se ignora.
- `estado` al crear siempre es `PENDIENTE`. Solo se cambia con PATCH.
- Hay choque si otra reserva **no rechazada** tiene la misma dependencia, la misma fecha y `horaInicio < finNueva AND horaFin > inicioNueva`. Es la misma regla que `ArriendoDao.contarChoques`.

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | /dependencias | — | 200 `[Dependencia]` |
| GET | /reservas?usuarioId={id} | — (`usuarioId` es opcional) | 200 `[Reserva]` ordenadas por fecha descendente |
| GET | /reservas/{id} | — | 200 Reserva · 404 |
| POST | /reservas | `{"usuarioId","dependencia","fecha","horaInicio","horaFin","motivo"}` | 201 Reserva · 400 |
| PUT | /reservas/{id} | igual que POST | 200 Reserva (con el monto recalculado) · 400 · 404 |
| PATCH | /reservas/{id}/estado | `{"estado":"APROBADO"}` | 200 Reserva · 400 · 404 |
| DELETE | /reservas/{id} | — | 204 (también borra su historial) · 404 |
| GET | /reservas/{id}/historial | — | 200 `[Historial]` · 404 |

Historial: se registra al crear la reserva (`estadoAnterior` = null) y en cada cambio de estado.

```json
[
  { "id": 1, "reservaId": 1, "estadoAnterior": null, "estadoNuevo": "PENDIENTE", "fechaCambio": "2026-11-18 15:30:00" },
  { "id": 2, "reservaId": 1, "estadoAnterior": "PENDIENTE", "estadoNuevo": "APROBADO", "fechaCambio": "2026-11-18 16:02:11" }
]
```

---

## ms-finanzas (9093)

Movimiento:

```json
{
  "id": 1, "tipo": "PAGO", "concepto": "Arriendo Salón multiuso", "monto": 30000,
  "fecha": 1764950400000, "persona": "Vecino Prueba", "descripcion": "Pago reserva 1",
  "reservaId": 1, "uriComprobante": null
}
```

- `tipo`, `concepto` y `monto > 0` son obligatorios. Si falta `fecha`, se usa la hora actual del servidor.
- `reservaId` y `uriComprobante` son opcionales. `uriComprobante` es solo texto: el backend no recibe archivos.

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | /movimientos?tipo={tipo} | — (`tipo` es opcional) | 200 `[Movimiento]` ordenados por fecha descendente · 400 tipo inválido |
| GET | /movimientos/{id} | — | 200 · 404 |
| POST | /movimientos | Movimiento sin id | 201 · 400 |
| PUT | /movimientos/{id} | Movimiento | 200 · 400 · 404 |
| DELETE | /movimientos/{id} | — | 204 · 404 |
| GET | /movimientos/resumen | — | 200 `{"totalIngresos","totalGastos","saldo"}` |

Resumen: `totalIngresos` = suma de PAGO + INGRESO; `saldo` = `totalIngresos - totalGastos`.

```json
{ "totalIngresos": 180000, "totalGastos": 25000, "saldo": 155000 }
```

---

## Fuera de alcance (posibles mejoras para el EFT)

- Categorías de movimientos (hoy el campo `concepto` hace ese papel).
- JWT o API Gateway. El ejemplo del profesor los tiene, pero aquí se dejaron fuera para mantener el backend simple.
- Comunicación entre servicios: ms-reservas no comprueba que `usuarioId` exista en ms-admin, y ms-finanzas no comprueba `reservaId`.
- Subir la foto del comprobante al servidor.
