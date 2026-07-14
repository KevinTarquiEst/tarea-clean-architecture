# Data Model: Filtrado de Usuarios por Dominio de Correo

**Feature**: `001-filter-users-by-email` | **Date**: 2026-07-13

No se introducen entidades nuevas ni cambios de esquema de persistencia. Esta
feature reutiliza el modelo de dominio `User` ya existente y añade una
operación de filtrado sobre una colección de instancias ya cargadas.

## Entidad: User (existente, sin cambios de forma)

| Campo | Tipo | Notas |
|-------|------|-------|
| `firstName` | `String` | Sin cambios |
| `lastName` | `String` | Sin cambios |
| `email` | `String` | Atributo usado por el nuevo filtro; ya existía en `domain.model.User` |
| `phone` | `String` | Sin cambios |

**Reglas de validación relevantes para esta feature**:
- Un `User` cuyo `email` es `null` NUNCA coincide con ningún filtro de
  dominio (se excluye silenciosamente del resultado, sin lanzar error).
- La coincidencia del filtro es una operación de solo lectura; no modifica
  ningún `User`.

## Nueva operación de dominio: filtrado por dominio de correo

Ubicación: `domain.service.UserService` (mismo patrón que
`findByLastName`).

```text
findByEmailDomain(List<User> users, String domainFilter) -> List<User>
```

**Contrato funcional** (deriva de FR-002, FR-003, FR-004, FR-005, FR-006,
FR-008 del spec):

- Precondición: `domainFilter` no debe ser `null` ni blanco; esta función
  asume que la validación de "requerido" ya ocurrió en una capa superior
  (caso de uso / controlador) — la función en sí NO lanza excepción por
  entrada vacía, para mantenerla como una función pura de filtrado (la
  decisión de rechazar la solicitud es responsabilidad del caso de uso,
  ver contracts/).
- Normaliza `domainFilter`: recorta espacios, quita un `@` inicial si
  existe, convierte a minúsculas.
- Para cada `user` en `users`: se incluye en el resultado si
  `user.getEmail() != null` y
  `user.getEmail().toLowerCase().contains(domainFilterNormalizado)`.
- Devuelve una lista vacía (nunca `null`) si no hay coincidencias o si
  `users` es `null`/vacía.
- No modifica la lista de entrada ni los objetos `User`.

## Sin cambios en persistencia

- `infraestructure.adapters.output.UserEntity` y
  `UserEntityRepository`: sin cambios. La operación reutiliza
  `UserPort.findAll()` (ya existente, usado también por
  `findByLastName`), por lo que no se requiere una nueva query JPA.

## DTO de transporte afectado

| DTO | Cambio | Razón |
|-----|--------|-------|
| `infraestructure.adapters.input.dto.UserResponse` | Añadir campo `email` | Ver `research.md` Decisión 4 — sin este campo el cliente no puede confirmar qué correo produjo la coincidencia |
| `infraestructure.adapters.input.mapper.UserResponseMapper` | Sin cambios de código | MapStruct mapea `email` automáticamente por convención de nombre una vez añadido a `UserResponse` |
