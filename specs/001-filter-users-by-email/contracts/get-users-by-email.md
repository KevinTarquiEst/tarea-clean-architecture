# Contrato: GET /v1/users/by-email?email={filtro}

**Feature**: `001-filter-users-by-email`

Añade una subruta dedicada a la colección de usuarios existente
(`/v1/users`) para filtrar por dominio/proveedor de correo, independiente
del filtro existente por `lastName` en `/v1/users` (ver `research.md`
Decisión 3 sobre por qué se usa una subruta y no el mismo path).

## Request

```
GET /v1/users/by-email?email={filtro}
```

| Parámetro | Ubicación | Tipo | Requerido | Descripción |
|-----------|-----------|------|-----------|--------------|
| `email` | query | string | Sí | Texto a buscar dentro del correo de cada usuario (coincidencia parcial, insensible a mayúsculas/minúsculas). Puede ser un proveedor sin TLD (`gmail`), un dominio completo (`gmail.com`) o incluir un `@` inicial (`@gmail.com`). |

## Responses

### 200 OK — coincidencias encontradas o no

```json
[
  {
    "firstName": "Ana",
    "lastName": "Gómez",
    "email": "ana@gmail.com"
  },
  {
    "firstName": "Carlos",
    "lastName": "Pérez",
    "email": "carlos@gmail.com"
  }
]
```

- Si ningún usuario coincide, el cuerpo es `[]` (lista vacía), no un error
  (FR-005).
- El orden de la lista no está garantizado por este contrato.

### 400 Bad Request — parámetro `email` ausente o en blanco

```json
{
  "status": 400,
  "message": "El parámetro 'email' es requerido y no puede estar vacío"
}
```

Aplica cuando `email` no se envía (Spring responde `400` automáticamente por
parámetro requerido faltante), o se envía vacío/solo espacios (el caso de
uso lanza `IllegalArgumentException`, capturada por el controlador y
traducida a `400`) — FR-006. El contrato exige únicamente el código de
estado `400` y que no se devuelva una lista.

## Casos de prueba de contrato

| # | `email` enviado | Usuarios existentes | Resultado esperado |
|---|------------------|----------------------|---------------------|
| 1 | `gmail.com` | `ana@gmail.com`, `luis@hotmail.com` | 200, `[ana]` |
| 2 | `gmail` | `ana@gmail.com` | 200, `[ana]` (sin TLD también coincide) |
| 3 | `@gmail.com` | `ana@gmail.com` | 200, `[ana]` (arroba inicial normalizada) |
| 4 | `GMAIL.COM` | `ana@gmail.com` | 200, `[ana]` (insensible a mayúsculas) |
| 5 | `yahoo.com` | `ana@gmail.com` | 200, `[]` |
| 6 | (vacío) | cualquiera | 400 |
| 7 | (ausente) | cualquiera | 400 |
