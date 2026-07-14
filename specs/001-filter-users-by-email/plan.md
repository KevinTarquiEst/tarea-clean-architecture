# Implementation Plan: Filtrado de Usuarios por Dominio de Correo

**Branch**: `001-filter-users-by-email` | **Date**: 2026-07-13 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-filter-users-by-email/spec.md`

## Summary

Añadir un endpoint de consulta (`GET /v1/users?email={filtro}`) que devuelve
la lista de usuarios cuyo correo contiene el texto de filtro dado (proveedor
sin TLD o dominio completo, coincidencia parcial e insensible a
mayúsculas/minúsculas), rechazando con `400` cuando el filtro viene vacío o
ausente. Se implementa siguiendo el mismo patrón ya usado por el filtro
existente por apellido: un método puro en `domain.service.UserService` que
opera sobre la lista completa de usuarios obtenida vía `UserPort.findAll()`,
expuesto a través de un nuevo método en el puerto de entrada
`IUserUseCase` y del caso de uso `UserUseCaseImpl`, y consumido por un nuevo
método `GET` en `UserController` disparado por la presencia del parámetro de
consulta `email`. Se añade el campo `email` a `UserResponse` para que el
cliente pueda ver qué correo produjo cada coincidencia.

## Technical Context

**Language/Version**: Java 25 (toolchain fijado en `build.gradle`)

**Primary Dependencies**: Spring Boot 4.1.0 (`spring-boot-starter-webmvc`,
`spring-boot-starter-data-jpa`, `spring-boot-h2console`), MapStruct 1.6.3,
Lombok — todas ya presentes en el proyecto, no se añaden dependencias nuevas.

**Storage**: H2 en memoria vía Spring Data JPA (`UserEntity`,
`UserEntityRepository`, ya existentes). Sin cambios de esquema: esta feature
reutiliza `UserPort.findAll()`.

**Testing**: JUnit 5 (`junit-platform-launcher`). Por Principio V de la
constitución, las pruebas de `domain.service` y `aplication.usecases` deben
ejecutarse sin contexto Spring; las pruebas de `UserController` pueden usar
`spring-boot-starter-webmvc-test` (slice de MVC).

**Target Platform**: Servidor JVM (Spring Boot embebido), mismo destino de
despliegue que el resto de la aplicación.

**Project Type**: Servicio web de un solo proyecto Gradle (arquitectura
hexagonal ya existente bajo
`src/main/java/org/cat/usercleanarchitecture`).

**Performance Goals**: Ninguno específico más allá de lo ya implícito en los
endpoints existentes (filtrado en memoria sobre la lista completa de
usuarios; alcance académico, sin SLA de latencia declarado).

**Constraints**: Debe cumplir los Principios I–V de la constitución (ver
Constitution Check); no debe introducir dependencias nuevas ni cambios de
esquema de base de datos.

**Scale/Scope**: Un endpoint de lectura nuevo sobre el feature `User` ya
existente; sin entidades nuevas, sin migraciones.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Evaluación | Resultado |
|-----------|------------|-----------|
| I. Regla de Dependencia | El filtrado se implementa en `domain.service` (sin salir hacia `aplication`/`infraestructure`); el caso de uso solo orquesta llamando al dominio y al puerto de salida ya existente. | PASS |
| II. Puertos y Adaptadores | El nuevo método se expone a través de `IUserUseCase` (puerto de entrada); `UserController` depende de la interfaz, no de `UserUseCaseImpl` directamente (ya es así hoy). No se necesita un nuevo puerto de salida: se reutiliza `UserPort.findAll()`. | PASS |
| III. Pureza del Dominio | `UserService.findByEmailDomain` es Java plano (String, List, sin anotaciones Spring/JPA), igual que `findByLastName`. | PASS |
| IV. Mapeo Explícito en los Límites | El único cambio de DTO (`UserResponse` + campo `email`) se sigue mapeando vía `UserResponseMapper` (MapStruct); no hay mapeo manual en el controlador. | PASS |
| V. Cobertura de Pruebas (NO NEGOCIABLE) | Se planifican pruebas unitarias para `UserService.findByEmailDomain` (sin Spring) y para `UserUseCaseImpl` (puerto simulado); prueba de slice MVC para el controlador dado que cambia comportamiento observable (nuevo query param, nuevo campo en la respuesta, nuevo 400). | PASS (gate cumplido en el diseño; verificar en tasks/implementación) |

**Resultado**: Sin violaciones. No se requieren entradas en Complexity
Tracking.

*Re-chequeo post-diseño (tras Fase 1)*: `data-model.md` y
`contracts/get-users-by-email.md` confirman que no se introdujeron
entidades nuevas, puertos nuevos, ni mapeo manual — las cinco filas de la
tabla anterior se mantienen en PASS sin cambios.

## Project Structure

### Documentation (this feature)

```text
specs/001-filter-users-by-email/
├── plan.md              # Este archivo
├── research.md          # Fase 0
├── data-model.md         # Fase 1
├── quickstart.md         # Fase 1
├── contracts/            # Fase 1
│   └── get-users-by-email.md
└── tasks.md              # Fase 2 (/speckit-tasks, no creado por /speckit-plan)
```

### Source Code (repository root)

```text
src/main/java/org/cat/usercleanarchitecture/
├── domain/
│   ├── model/
│   │   └── User.java                         # Sin cambios
│   └── service/
│       └── UserService.java                  # + findByEmailDomain(List<User>, String)
├── aplication/
│   ├── ports/
│   │   ├── input/
│   │   │   └── IUserUseCase.java             # + findByEmailDomain(String)
│   │   └── output/
│   │       └── UserPort.java                 # Sin cambios (se reutiliza findAll())
│   └── usecases/
│       └── UserUseCaseImpl.java              # + implementación de findByEmailDomain
└── infraestructure/
    └── adapters/
        └── input/
            ├── UserController.java           # + GET /v1/users?email=... (params="email")
            ├── dto/
            │   └── UserResponse.java          # + campo email
            └── mapper/
                └── UserResponseMapper.java    # Sin cambios de código (MapStruct mapea el nuevo campo por convención)

src/test/java/org/cat/usercleanarchitecture/
├── domain/service/
│   └── UserServiceTest.java                  # Nuevo — sin contexto Spring (Principio V)
├── aplication/usecases/
│   └── UserUseCaseImplTest.java              # Nuevo — UserPort simulado/fake
└── infraestructure/adapters/input/
    └── UserControllerTest.java                # Nuevo — slice MVC (@WebMvcTest o equivalente)
```

**Structure Decision**: Se mantiene el proyecto Gradle único existente y su
arquitectura hexagonal de tres capas (`domain` / `aplication` /
`infraestructure`). No se crean módulos ni paquetes nuevos de alto nivel;
todos los cambios son adiciones dentro de los paquetes ya usados por el
feature `User`, siguiendo el flujo de desarrollo "outside-in" descrito en la
constitución (dominio → puertos → caso de uso → adaptador).

## Complexity Tracking

*Sin violaciones de la Constitution Check — tabla intencionalmente vacía.*
