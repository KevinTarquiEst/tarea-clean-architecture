# Research: Filtrado de Usuarios por Dominio de Correo

**Feature**: `001-filter-users-by-email` | **Date**: 2026-07-13

No quedaron marcadores `NEEDS CLARIFICATION` en el Technical Context: todo el
contexto técnico se deriva directamente del proyecto existente (`build.gradle`,
capas ya implementadas para el feature `User`). Este documento registra las
decisiones de diseño específicas de esta feature que no estaban ya fijadas
por el código existente.

## Decisión 1: Dónde vive la lógica de filtrado

**Decision**: Añadir un método `findByEmailDomain(List<User> users, String
domain)` a `domain.service.UserService`, como método estático puro (mismo
patrón que el ya existente `findByLastName`).

**Rationale**: Principio III (Pureza del Dominio) y Principio V (Cobertura de
pruebas) de la constitución exigen que la lógica de negocio viva en el
dominio y sea comprobable sin contexto Spring. Seguir el patrón de
`findByLastName` mantiene consistencia con el código existente y facilita el
test unitario aislado.

**Alternatives considered**:
- Filtrar directamente en `UserUseCaseImpl`: rechazada porque mezclaría
  orquestación con lógica de negocio y dificultaría probar el filtrado sin
  mocks de puertos.
- Filtrar en el adaptador de salida (`UserAdapter`/JPA, con una query
  `LIKE %domain%`): rechazada porque el spec ya fue clarificado para operar
  sobre la lista completa de usuarios (mismo patrón que `findByLastName`, que
  usa `userPort.findAll()` y filtra en memoria); introducir una query nueva
  sería una capacidad no solicitada y aumentaría el alcance sin necesidad.

## Decisión 2: Semántica de la comparación (contiene, case-insensitive)

**Decision**: `email.toLowerCase().contains(domain.toLowerCase())`, con
`domain` previamente despojado de un posible `@` inicial y de espacios.

**Rationale**: Confirmado en la sesión de clarificación (2026-07-13):
coincidencia parcial, insensible a mayúsculas/minúsculas, aceptando el valor
con o sin `@` inicial y con o sin TLD.

**Alternatives considered**: coincidencia exacta tras el `@` (rechazada por
el usuario en clarificación); expresiones regulares por proveedor conocido
(gmail/hotmail/outlook) codificadas explícitamente (rechazada: violaría
FR-007, que exige soportar cualquier proveedor sin cambios de código).

## Decisión 3: Forma de exponer el endpoint (ruta y parámetro HTTP)

**Decision**: Añadir un método `GET` adicional en el `UserController`
existente en una subruta dedicada `/v1/users/by-email`, con
`@RequestParam String email` requerido.

**Rationale**: Se evaluó primero distinguir por la presencia del parámetro
de consulta en la misma ruta base (`@GetMapping(params = "email")` junto a
`@GetMapping(params = "lastName")`), pero esa alternativa se descartó
durante la implementación: si una solicitud llega a `/v1/users` sin ningún
parámetro, Spring MVC no encuentra ningún método que matchee (ninguno de los
`params` se cumple) y responde `404 Not Found` en lugar de despachar a un
handler que pueda validar y responder `400`. Eso rompe dos cosas a la vez:
(a) el requisito FR-006 de este spec (email ausente ⇒ `400`, no `404`), y
(b) el comportamiento ya existente de `/v1/users` sin `lastName` (que antes
respondía `400` por parámetro requerido faltante, vía el manejo por defecto
de Spring). Una subruta dedicada aísla completamente ambos endpoints: cada
uno controla su propia validación de "requerido" sin interferir con el otro,
y `/v1/users` (filtro por apellido) queda exactamente como estaba antes de
esta feature.

**Alternatives considered**: distinguir por presencia de parámetro en la
misma ruta (`params` attribute) — descartada por la razón anterior, verificada
durante la implementación, no solo en el diseño.

## Decisión 4: Exponer el correo en la respuesta

**Decision**: Añadir el campo `email` a `UserResponse` (actualmente solo
expone `firstName` y `lastName`).

**Rationale**: El propósito completo de la feature es "conocer cuáles son
los usuarios que tienen gmail/hotmail/outlook, etc."; sin el campo `email`
en la respuesta, el cliente no puede verificar qué correo produjo la
coincidencia. MapStruct mapea este campo automáticamente por convención de
nombre una vez añadido a `UserResponse`, sin tocar `UserResponseMapper`
(Principio IV: el mapeo sigue centralizado en el mapper existente).
Este campo adicional es aditivo y no rompe a consumidores existentes del
endpoint `findByLastName`, que simplemente verán un campo más en el JSON.

**Alternatives considered**: crear un DTO de respuesta distinto solo para
este endpoint (p. ej. `UserEmailFilterResponse`) — rechazada por
duplicar estructura idéntica a `UserResponse` + `email` sin beneficio, dado
que ambos endpoints devuelven la misma forma de datos de usuario.

## Decisión 5: Validación del parámetro requerido

**Decision**: Usar `@RequestParam String email` (sin `required = false`) y
además validar explícitamente que no esté en blanco en el caso de uso o
controlador, devolviendo `400 Bad Request` con un mensaje indicando que el
parámetro es requerido.

**Rationale**: Spring ya rechaza con `400` si falta un `@RequestParam`
obligatorio sin valor por defecto, pero no rechaza un string vacío/blanco
(`?email=`) por sí solo; se necesita una validación explícita adicional para
cubrir ese caso, tal como se confirmó en la clarificación (dominio vacío ⇒
error de validación, no lista vacía ni lista completa).

**Alternatives considered**: usar Bean Validation (`@NotBlank`) sobre un
parámetro de query simple — viable pero añade una dependencia de anotaciones
de validación no usada actualmente en el proyecto para query params sueltos;
una validación explícita simple es suficiente para el alcance de esta
feature y no introduce nuevas dependencias.
