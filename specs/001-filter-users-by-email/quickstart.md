# Quickstart: Validar el filtrado de usuarios por dominio de correo

**Feature**: `001-filter-users-by-email`

## Prerrequisitos

- JDK compatible con el toolchain declarado en `build.gradle` (Java 25).
- Ninguna configuración adicional: el proyecto usa H2 en memoria
  (`spring-boot-h2console`), sin base de datos externa.

## Levantar la aplicación

```bash
./gradlew bootRun
```

La API queda disponible en `http://localhost:8080` (puerto por defecto de
Spring Boot, salvo que `application.yml` indique otro).

## Preparar datos de prueba

Crear al menos dos usuarios con distintos dominios de correo, usando el
endpoint existente:

```bash
curl -X POST http://localhost:8080/v1/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Ana","lastName":"Gomez","email":"ana@gmail.com","phone":"0000000"}'

curl -X POST http://localhost:8080/v1/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Luis","lastName":"Diaz","email":"luis@hotmail.com","phone":"0000000"}'
```

## Escenarios de validación (ver contracts/get-users-by-email.md)

1. **Filtrar por dominio completo**:
   ```bash
   curl "http://localhost:8080/v1/users/by-email?email=gmail.com"
   ```
   Esperado: 200 OK, solo aparece Ana.

2. **Filtrar por proveedor sin TLD**:
   ```bash
   curl "http://localhost:8080/v1/users/by-email?email=gmail"
   ```
   Esperado: 200 OK, solo aparece Ana (coincidencia parcial).

3. **Filtrar sin coincidencias**:
   ```bash
   curl "http://localhost:8080/v1/users/by-email?email=yahoo.com"
   ```
   Esperado: 200 OK, cuerpo `[]`.

4. **Parámetro vacío**:
   ```bash
   curl -i "http://localhost:8080/v1/users/by-email?email="
   ```
   Esperado: 400 Bad Request.

5. **Parámetro ausente**:
   ```bash
   curl -i "http://localhost:8080/v1/users/by-email"
   ```
   Esperado: 400 Bad Request (Spring rechaza automáticamente por parámetro
   `email` requerido faltante).

## Nota sobre coexistencia con el filtro por apellido

`GET /v1/users` (filtro por `lastName`) y `GET /v1/users/by-email` (filtro
por `email`) son subrutas independientes del mismo controlador, cada una
con su propia validación de parámetro requerido; ninguna interfiere con la
otra (ver `research.md` Decisión 3). El endpoint `GET /v1/users` no cambia
su comportamiento previo a esta feature.

## Criterio de éxito

La feature se considera validada cuando los 5 escenarios anteriores
producen el resultado esperado y las pruebas unitarias de
`UserService.findByEmailDomain` (sin contexto Spring, per Principio V de la
constitución) pasan en `./gradlew test`.
