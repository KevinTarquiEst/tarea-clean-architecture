<!--
Sync Impact Report
==================
Cambio de versión: [TEMPLATE] → 1.0.0 (ratificación inicial)
Principios modificados: N/A (primera versión concreta, no existían principios
  nombrados previamente)
Secciones añadidas:
  - Principios Fundamentales I–V (Regla de Dependencia, Puertos y Adaptadores,
    Pureza del Dominio, Mapeo Explícito en los Límites, Cobertura de Pruebas
    para la Lógica de Negocio)
  - Restricciones del Stack Tecnológico
  - Flujo de Desarrollo
  - Gobernanza
Secciones eliminadas: ninguna (todos los placeholders del template fueron resueltos)
Plantillas que requieren actualización:
  - .specify/templates/plan-template.md ✅ sin cambios necesarios (la sección
    Constitution Check es genérica y lee las gates desde este archivo)
  - .specify/templates/spec-template.md ✅ sin cambios necesarios (agnóstico
    de tecnología)
  - .specify/templates/tasks-template.md ✅ sin cambios necesarios (las
    pruebas siguen siendo opcionales/contextuales, consistente con el
    alcance del Principio V)
  - .claude/skills (comandos relacionados con la constitution) ✅ no se
    encontraron referencias específicas de agente que contradigan los
    principios actualizados
Pendientes (TODOs): ninguno
-->

# Constitución de tarea-clean-architecture

## Principios Fundamentales

### I. Regla de Dependencia (Dependencias Solo Hacia Adentro)

El código fuente se organiza en tres capas concéntricas — `domain`,
`aplication` e `infraestructure` — y las dependencias DEBEN apuntar
únicamente hacia adentro: `infraestructure` PUEDE depender de `aplication` y
`domain`; `aplication` PUEDE depender de `domain`; `domain` NO DEBE depender
de `aplication` ni de `infraestructure`. Una clase de una capa interna NO
DEBE importar una clase de una capa externa bajo ninguna circunstancia, ni
siquiera por conveniencia o para evitar duplicar un fragmento pequeño de
lógica.

**Justificación**: Esta es la regla definitoria de la Arquitectura Limpia
(Hexagonal). En cuanto se viola, la separación se degrada silenciosamente y el
código vuelve a ser un monolito acoplado al framework y difícil de probar —
exactamente el problema que esta estructura de proyecto busca evitar.

### II. Puertos y Adaptadores (Límites Hexagonales)

Toda interacción que cruce un límite de capa DEBE pasar por una interfaz Java
explícita ("puerto") definida en `aplication.ports.input` o
`aplication.ports.output`. Los casos de uso (`aplication.usecases`) DEBEN
depender únicamente de interfaces de puertos, nunca de implementaciones
concretas de adaptadores. Los adaptadores (`infraestructure.adapters.input` /
`.output`) DEBEN implementar un puerto y NO DEBEN ser referenciados
directamente por código de aplicación o de dominio.

**Justificación**: Los puertos son el punto de conexión que hace que las
capas de dominio y aplicación sean probables de forma independiente y permite
sustituir adaptadores (REST, JPA, futura mensajería, etc.) sin tocar la
lógica de negocio.

### III. Pureza del Dominio (Núcleo Agnóstico al Framework)

El código bajo `domain.model` y `domain.service` DEBE ser Java plano, sin
anotaciones ni imports de Spring, JPA/Hibernate, persistencia Jakarta o la
capa web. Lombok PUEDE usarse en el dominio únicamente para reducir
boilerplate (por ejemplo, getters/setters, constructores) — nunca para
conexión con el framework. Las preocupaciones de persistencia y transporte
(entidades, DTOs, modelos de request/response) DEBEN vivir exclusivamente en
`infraestructure`.

**Justificación**: Un dominio que compila y se ejecuta sin Spring ni un
driver de base de datos en el classpath demuestra que las reglas de negocio
son verdaderamente independientes del mecanismo de entrega, que es la
promesa central de esta arquitectura para el resto del equipo.

### IV. Mapeo Explícito en los Límites

La conversión entre modelos de dominio y cualquier representación externa
(DTOs REST, entidades JPA) DEBE ocurrir únicamente dentro de clases mapper
dedicadas bajo `infraestructure.adapters.*.mapper`, usando MapStruct. Los
controladores, casos de uso y servicios de dominio NO DEBEN realizar
traducción manual campo por campo entre un DTO/entidad y un objeto de
dominio.

**Justificación**: Centralizar el mapeo mantiene la lógica de traducción en
un único lugar identificable por adaptador, evita que los objetos de dominio
se filtren hacia preocupaciones de JSON/JPA, y mantiene a los controladores y
casos de uso enfocados en la orquestación.

### V. Cobertura de Pruebas para la Lógica de Negocio (NO NEGOCIABLE)

Toda clase nueva o modificada en `domain.service` y `aplication.usecases`
DEBE ir acompañada de pruebas unitarias que la ejerciten de forma aislada (sin
contexto de Spring, con puertos simulados/falsos). Las pruebas del código de
`domain` DEBEN ejecutarse sin levantar el contexto de la aplicación Spring.
Los cambios en controladores y adaptadores DEBERÍAN incluir una prueba de
integración o "slice" enfocada cuando alteren el comportamiento observable
(códigos de estado, forma del payload, consultas de persistencia).

**Justificación**: Las reglas de negocio (por ejemplo, filtrado, validación)
son el código de mayor valor y más barato de probar en esta arquitectura
precisamente porque no tienen dependencias de framework; omitir sus pruebas
renuncia a esa ventaja sin ningún ahorro real de tiempo.

## Restricciones del Stack Tecnológico

- **Lenguaje/Runtime**: Java, fijado mediante el toolchain de Gradle
  (`JavaLanguageVersion` en `build.gradle`). No bajar la versión del
  toolchain sin actualizar `build.gradle` y este documento juntos.
- **Framework**: Spring Boot (web MVC + Spring Data JPA) es el único
  framework de aplicación. DEBE permanecer confinado a `infraestructure`
  (Principio III).
- **Persistencia**: las entidades JPA viven bajo
  `infraestructure.adapters.output`; H2 es la base de datos de desarrollo/
  pruebas. Cambiar el motor de base de datos NO DEBE requerir cambios fuera
  de `infraestructure`.
- **Mapeo/Boilerplate**: MapStruct es la herramienta requerida para el mapeo
  DTO/entidad ↔ dominio (Principio IV); Lombok es la herramienta requerida
  para reducir boilerplate de constructores/getters/setters.
- **Build**: Gradle es la única herramienta de build; todas las versiones de
  dependencias se declaran en `build.gradle`.
- **Pruebas**: JUnit 5 (vía `junit-platform-launcher`) es el runner de
  pruebas para todas las capas.

## Flujo de Desarrollo

- La funcionalidad nueva se construye de afuera hacia adentro ("outside-in")
  siguiendo este orden de capas: modelo/servicio de dominio → puertos
  (input/output) → implementación del caso de uso → adaptador de
  infraestructura (controlador, repositorio, mapper), siguiendo como
  referencia la funcionalidad `User` ya existente.
- Los mensajes de commit siguen un prefijo corto estilo conventional-commit
  (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`), tal como ya se usa en el
  historial de este repositorio.
- Antes de dar por completo un cambio, verificar que no introduce una
  violación de los Principios I–IV (por ejemplo, una clase de dominio
  importando `jakarta.persistence` o un controlador mapeando campos a mano) y
  que se cumplen las expectativas de pruebas del Principio V para cualquier
  código de `domain` o `aplication` modificado.

## Gobernanza

Esta constitución tiene precedencia sobre convenciones ad hoc para este
repositorio. Cualquier cambio a las reglas de capas, restricciones del stack
tecnológico o flujo de trabajo anteriores es una enmienda a este documento y
DEBE hacerse aquí primero, no simplemente inferirse a partir de código nuevo.

**Procedimiento de enmienda**: Editar este archivo, actualizar el Sync Impact
Report al inicio, e incrementar la versión según versionado semántico:
- MAYOR (MAJOR): se elimina o redefine un principio de forma incompatible
  hacia atrás (por ejemplo, relajar la Regla de Dependencia).
- MENOR (MINOR): se añade un principio nuevo o una sección ampliada de forma
  material.
- PARCHE (PATCH): correcciones de redacción, aclaraciones o typos sin cambio
  de regla.

**Revisión de cumplimiento**: Al ser un proyecto académico de un solo
contribuyente, el cumplimiento se autorrevisa antes de cada commit/merge,
comprobando el código nuevo o modificado bajo `src/main/java` contra los
Principios I–V anteriores. Cualquier desviación intencional y temporal DEBE
anotarse en el mensaje de commit o descripción del PR junto con la razón.

**Versión**: 1.0.0 | **Ratificada**: 2026-07-13 | **Última Enmienda**: 2026-07-13
