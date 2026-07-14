---

description: "Task list for Filtrado de Usuarios por Dominio de Correo"
---

# Tasks: Filtrado de Usuarios por Dominio de Correo

**Input**: Design documents from `specs/001-filter-users-by-email/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/get-users-by-email.md, quickstart.md

**Tests**: Aunque el flujo por defecto marca las pruebas como opcionales, la
**Constitución del proyecto (Principio V, NO NEGOCIABLE)** exige pruebas
unitarias para toda clase nueva/modificada en `domain.service` y
`aplication.usecases`, y recomienda una prueba de slice para el
controlador dado que cambia comportamiento observable. Por eso esta lista
incluye tareas de prueba obligatorias, ordenadas justo después de la
implementación de cada capa (en lugar de antes, dado que Java exige que el
método exista para poder compilar la prueba contra él).

**Organization**: Existe una única historia de usuario (US1, P1) según
`spec.md`; todas las tareas de implementación pertenecen a ella.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin
  dependencias pendientes)
- **[Story]**: A qué historia de usuario pertenece la tarea (US1)
- Cada tarea incluye la ruta de archivo exacta

## Path Conventions

Proyecto Gradle único existente (arquitectura hexagonal):
`src/main/java/org/cat/usercleanarchitecture/...` y
`src/test/java/org/cat/usercleanarchitecture/...`

---

## Phase 1: Setup

No se requiere inicialización de proyecto: se reutiliza el proyecto Gradle
existente, sus dependencias (Spring Boot, MapStruct, Lombok, JUnit 5) y su
estructura de paquetes. No hay tareas en esta fase.

## Phase 2: Foundational (Blocking Prerequisites)

No hay prerrequisitos bloqueantes compartidos entre historias: existe una
sola historia de usuario y reutiliza puertos/entidades ya existentes
(`UserPort.findAll()`, `domain.model.User`). Todo el trabajo se organiza
directamente dentro de la Fase 3.

---

## Phase 3: User Story 1 - Consultar usuarios por proveedor de correo (Priority: P1) 🎯 MVP

**Goal**: Exponer `GET /v1/users?email={filtro}` devolviendo únicamente los
usuarios cuyo correo contiene el filtro (coincidencia parcial,
insensible a mayúsculas/minúsculas), rechazando con `400` cuando el filtro
viene vacío o ausente.

**Independent Test**: Ver `quickstart.md` — crear usuarios con distintos
dominios de correo vía `POST /v1/users` y verificar los 5 escenarios de
`contracts/get-users-by-email.md` contra la app corriendo con
`./gradlew bootRun`.

### Implementación y pruebas para User Story 1

- [X] T001 [P] [US1] Añadir el campo `email` a `UserResponse` en `src/main/java/org/cat/usercleanarchitecture/infraestructure/adapters/input/dto/UserResponse.java` (ver `data-model.md` Decisión 4; `UserResponseMapper` no necesita cambios, MapStruct lo mapea por convención de nombre)
- [X] T002 [P] [US1] Implementar `UserService.findByEmailDomain(List<User> users, String domainFilter)` en `src/main/java/org/cat/usercleanarchitecture/domain/service/UserService.java`: normaliza `domainFilter` (recorta espacios, quita un `@` inicial, minúsculas), incluye usuarios cuyo `email` no nulo contiene el valor normalizado (case-insensitive); devuelve lista vacía (nunca `null`) si no hay coincidencias o `users` es `null`/vacía. Método estático puro, sin dependencias de Spring (Principio III de la constitución)
- [X] T003 [P] [US1] Añadir el método `List<User> findByEmailDomain(String domainFilter)` a la interfaz `IUserUseCase` en `src/main/java/org/cat/usercleanarchitecture/aplication/ports/input/IUserUseCase.java`
- [X] T004 [US1] Prueba unitaria `UserServiceTest` en `src/test/java/org/cat/usercleanarchitecture/domain/service/UserServiceTest.java` cubriendo los casos de `contracts/get-users-by-email.md` (#1–#5): coincidencia por dominio completo, por proveedor sin TLD, con `@` inicial normalizado, insensible a mayúsculas/minúsculas, sin coincidencias, `email` nulo excluido, lista de usuarios vacía o `null`. Debe ejecutarse sin contexto Spring (Principio V) — depende de T002
- [X] T005 [US1] Implementar `findByEmailDomain(String)` en `UserUseCaseImpl` (`src/main/java/org/cat/usercleanarchitecture/aplication/usecases/UserUseCaseImpl.java`): si el parámetro es `null` o está en blanco, lanzar `IllegalArgumentException` con mensaje indicando que el correo es requerido (FR-006); en caso contrario, obtener `userPort.findAll()` y delegar en `UserService.findByEmailDomain` — depende de T002, T003
- [X] T006 [US1] Prueba unitaria `UserUseCaseImplTest` en `src/test/java/org/cat/usercleanarchitecture/aplication/usecases/UserUseCaseImplTest.java` usando un `UserPort` simulado/fake: verifica que delega correctamente en `UserService.findByEmailDomain` para un filtro válido y que lanza `IllegalArgumentException` para filtro `null`/en blanco — depende de T005
- [X] T007 [US1] Añadir a `UserController` (`src/main/java/org/cat/usercleanarchitecture/infraestructure/adapters/input/UserController.java`) un método `GET` en la subruta dedicada `/v1/users/by-email` (`@GetMapping("/by-email")`, corregido durante la implementación respecto al plan original de `params = "email"` — ver `research.md` Decisión 3), que reciba `@RequestParam String email`, invoque `userUseCase.findByEmailDomain(email)`, capture `IllegalArgumentException` y responda `400 Bad Request` con un mensaje descriptivo, y mapee resultados exitosos a `List<UserResponse>` vía `UserResponseMapper.INSTANCE` (`200 OK`) — depende de T001, T005
- [X] T008 [US1] Prueba de slice MVC `UserControllerTest` en `src/test/java/org/cat/usercleanarchitecture/infraestructure/adapters/input/UserControllerTest.java` cubriendo la tabla de `contracts/get-users-by-email.md`: `200` con coincidencias, `200` con lista vacía, `400` con `email` vacío, `400` con `email` ausente — depende de T007

**Checkpoint**: En este punto, User Story 1 debe ser completamente funcional
y verificable de forma independiente siguiendo `quickstart.md`.

---

## Phase 4: Polish & Cross-Cutting Concerns

- [X] T009 [P] Ejecutar manualmente los 5 escenarios de `quickstart.md` contra la app levantada con `./gradlew bootRun`, confirmando que cada uno produce el resultado esperado
- [X] T010 Ejecutar `./gradlew test` y confirmar que toda la suite pasa, incluyendo las pruebas nuevas (T004, T006, T008) y las ya existentes (`UserCleanArchitectureApplicationTests`), sin regresión en el endpoint existente `GET /v1/users?lastName=`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Fase 1)**: Sin tareas — no bloquea nada
- **Foundational (Fase 2)**: Sin tareas — no bloquea nada
- **User Story 1 (Fase 3)**: Única historia; puede comenzar de inmediato
- **Polish (Fase 4)**: Depende de que toda la Fase 3 esté completa (T008)

### Dentro de User Story 1

- T001, T002, T003 no tienen dependencias entre sí → paralelizables
- T004 depende de T002
- T005 depende de T002 y T003
- T006 depende de T005
- T007 depende de T001 y T005
- T008 depende de T007

### Parallel Opportunities

```bash
# T001, T002 y T003 tocan archivos distintos sin dependencias entre ellos:
Task: "Añadir el campo email a UserResponse en .../dto/UserResponse.java"
Task: "Implementar UserService.findByEmailDomain en .../domain/service/UserService.java"
Task: "Añadir findByEmailDomain(String) a IUserUseCase en .../ports/input/IUserUseCase.java"
```

---

## Implementation Strategy

### MVP First (única historia)

1. Completar T001–T003 (en paralelo si hay más de un desarrollador)
2. Completar T004 (prueba de dominio) y verificar que pasa contra T002
3. Completar T005–T006 (caso de uso + su prueba)
4. Completar T007–T008 (adaptador HTTP + su prueba de slice)
5. **DETENER y VALIDAR**: ejecutar Fase 4 (quickstart + suite completa)

### Incremental Delivery

Al ser una única historia de usuario (P1 = MVP completo), no hay entregas
incrementales adicionales dentro de esta feature; el "incremento" es la
feature completa descrita en `spec.md`.

## Notes

- [P] = archivos distintos, sin dependencias pendientes
- [US1] = todas las tareas de implementación pertenecen a la única historia
  de usuario de este spec
- Verificar que cada prueba fallaría si la implementación asociada se
  revirtiera (falla real, no un "always green")
- Confirmar cumplimiento de los Principios I–V de la constitución antes de
  dar cada tarea por completa (ver `plan.md` → Constitution Check)
