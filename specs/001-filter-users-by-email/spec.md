# Feature Specification: Filtrado de Usuarios por Dominio de Correo

**Feature Branch**: `001-filter-users-by-email`

**Created**: 2026-07-13

**Status**: Draft

**Input**: User description: "Como desarrollador quiero crear un endpoint que me permita obtener todos los usuarios filtrados por correo para poder conocer cuales son los usuario que tienen gmail,hotmail,outlook, etc, el endpoint solo me debe retornar una lista con los usuarios que tienen el correo que el endpoint me permite filtrar."

## Clarifications

### Session 2026-07-13

- Q: ¿El filtro de dominio debe usar coincidencia exacta (el dominio después del `@` debe ser igual al valor filtrado) o coincidencia parcial (el valor filtrado puede aparecer en cualquier parte del correo)? → A: Coincidencia parcial ("contiene") - el valor filtrado puede aparecer en cualquier parte del correo electrónico.
- Q: ¿Qué debe ocurrir cuando el parámetro de dominio viene vacío o no se envía? → A: Rechazar la solicitud con un error de validación (dominio requerido).
- Q: ¿El valor de dominio debe aceptar solo el proveedor sin TLD (ej. `gmail`) además del dominio completo (ej. `gmail.com`)? → A: Aceptar cualquier texto tal cual; ambos formatos funcionan igual por ser coincidencia parcial.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar usuarios por proveedor de correo (Priority: P1)

Como desarrollador que consume la API, quiero solicitar la lista de usuarios
cuyo correo pertenece a un proveedor específico (por ejemplo `gmail.com`,
`hotmail.com`, `outlook.com`), para poder conocer cuántos y cuáles usuarios
usan cada proveedor de correo.

**Why this priority**: Es la única capacidad solicitada por el negocio; sin
ella la funcionalidad no existe. Es el MVP completo de esta feature.

**Independent Test**: Se puede probar de forma independiente creando usuarios
con distintos dominios de correo (`@gmail.com`, `@hotmail.com`,
`@outlook.com`, `@empresa.com`) y verificando que al solicitar un dominio
específico solo se reciben los usuarios cuyo correo contiene ese valor.

**Acceptance Scenarios**:

1. **Given** existen usuarios con correos `ana@gmail.com`, `luis@hotmail.com`
   y `carlos@gmail.com`, **When** se solicita el filtro por dominio
   `gmail.com`, **Then** la respuesta contiene únicamente a `ana` y `carlos`.
2. **Given** existen usuarios registrados, **When** se solicita un dominio
   que ningún usuario tiene (por ejemplo `yahoo.com`), **Then** la respuesta
   es una lista vacía (no un error).
3. **Given** un usuario tiene el correo `Maria@GMAIL.com`, **When** se filtra
   por `gmail.com`, **Then** ese usuario es incluido en el resultado
   (la comparación ignora mayúsculas/minúsculas).
4. **Given** no existe ningún usuario registrado, **When** se solicita
   cualquier dominio, **Then** la respuesta es una lista vacía.
5. **Given** un usuario tiene el correo `ana@gmail.com`, **When** se filtra
   usando solo el valor `gmail` (sin `.com`), **Then** ese usuario es
   incluido en el resultado, ya que la coincidencia es parcial.

### Edge Cases

- Un correo mal formado (sin `@`) puede seguir coincidiendo si el valor
  filtrado aparece como subcadena en cualquier parte del texto del correo,
  ya que la coincidencia es parcial ("contiene"), no estructural.
- Un correo como `usuario@notgmail.com` COINCIDE con el filtro `gmail.com`,
  porque la subcadena `gmail.com` está contenida en el correo, aunque el
  proveedor real no sea Gmail. Este es el comportamiento esperado de la
  coincidencia parcial.
- Si el valor de dominio recibido incluye un `@` inicial (por ejemplo
  `@gmail.com`), el sistema lo normaliza y lo trata igual que `gmail.com`.
- Si el parámetro de dominio viene vacío o en blanco, el sistema responde con
  un error de validación indicando que el dominio es requerido, en lugar de
  devolver todos los usuarios o una lista vacía silenciosa.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE exponer una forma de solicitar la lista de
  usuarios filtrada por un dominio de correo (por ejemplo `gmail.com`,
  `hotmail.com`, `outlook.com`).
- **FR-002**: El sistema DEBE incluir en el resultado únicamente a los
  usuarios cuyo correo contiene el valor de dominio solicitado como
  subcadena (coincidencia parcial, no exige que sea exactamente la parte
  después del `@`).
- **FR-003**: La comparación del dominio DEBE ser insensible a mayúsculas y
  minúsculas.
- **FR-004**: El sistema DEBE aceptar el valor del dominio con o sin un `@`
  inicial, tratándolos de forma equivalente.
- **FR-005**: Cuando ningún usuario coincide con el dominio solicitado, el
  sistema DEBE devolver una lista vacía en lugar de un error.
- **FR-006**: Cuando el valor del dominio no se proporciona o está en blanco,
  el sistema DEBE rechazar la solicitud indicando que el dominio es
  requerido.
- **FR-007**: El sistema NO DEBE requerir cambios de código para soportar un
  nuevo proveedor de correo (gmail, hotmail, outlook u otro): el dominio es
  un valor de entrada, no una lista fija codificada.
- **FR-008**: El sistema DEBE aceptar cualquier texto no vacío como valor de
  filtro, incluyendo un nombre de proveedor sin TLD (por ejemplo `gmail`) o
  un dominio completo (por ejemplo `gmail.com`), sin exigir un formato
  específico más allá de no estar en blanco.

### Key Entities

- **User**: representa a un usuario del sistema con nombre, apellido,
  correo electrónico y teléfono. El correo electrónico es el atributo usado
  para determinar el proveedor/dominio en esta feature.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un desarrollador puede obtener, en una sola solicitud, la lista
  completa de usuarios que pertenecen a un dominio de correo dado.
- **SC-002**: El resultado filtrado contiene el 100% de los usuarios cuyo
  correo pertenece al dominio solicitado y 0% de usuarios de otros dominios.
- **SC-003**: Solicitar un dominio sin usuarios asociados produce una
  respuesta vacía y exitosa (sin errores) en el 100% de los casos.
- **SC-004**: Filtrar por cualquier proveedor de correo (gmail, hotmail,
  outlook u otro no listado previamente) funciona sin necesidad de cambios
  adicionales, ya que el dominio se recibe como parámetro de la solicitud.

## Assumptions

- El filtro usa coincidencia parcial ("contiene") sobre el correo completo,
  confirmado en la sesión de clarificación del 2026-07-13; no valida que el
  valor coincida exactamente con la parte del dominio después del `@`.
- A diferencia del filtro existente por apellido (que devuelve lista vacía
  cuando el valor es blanco), este endpoint trata el dominio como su único
  criterio de búsqueda obligatorio: un valor vacío se considera una solicitud
  inválida en lugar de "sin filtro" (confirmado en la sesión de
  clarificación del 2026-07-13, ver FR-006).
- No se requiere paginación: el volumen de usuarios del sistema es
  suficientemente pequeño (contexto académico) para devolver la lista
  completa en una sola respuesta.
- No se requiere autenticación/autorización adicional para este endpoint,
  consistente con los endpoints existentes de usuarios.
