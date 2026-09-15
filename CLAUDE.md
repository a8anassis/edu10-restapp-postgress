# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 4.1.1 / Java 21 REST API (`edu10-restapp-postgress`, base package `gr.aueb.cf.eduapp`) for managing teachers, their personal info, and user accounts, with JWT-based authentication and role/capability-based authorization. PostgreSQL via Flyway migrations, Gradle build.

## Commands

Run all commands from the repo root using the Gradle wrapper (`gradlew.bat` on Windows / `./gradlew` in bash).

- Start the dev database: `docker-compose up -d` (Postgres 18, exposed on host port `5433`, container port `5432`; reads `POSTGRES_DB`/`POSTGRES_USER`/`POSTGRES_PASSWORD` from `.env`).
- Build: `gradlew.bat build`
- Run the app: `gradlew.bat bootRun` (defaults to the `dev` Spring profile — see `application.properties`)
- Run all tests: `gradlew.bat test`
- Run a single test class: `gradlew.bat test --tests "gr.aueb.cf.edu10restapppostgress.Edu10RestappPostgressApplicationTests"`
- Run a single test method: `gradlew.bat test --tests "FullyQualifiedClassName.methodName"`

Tests use `spring-boot-testcontainers` with the Postgres Testcontainers module, so Docker must be available when running tests that spin up a real database.

Configuration secrets (`JWT_SECRET_KEY`, DB credentials) live in `.env` at the repo root and are loaded via `spring.config.import=optional:file:.env[.properties]` in `application-dev.properties`.

## Architecture

Standard layered structure under `src/main/java/gr/aueb/cf/eduapp/`:

- `api/` — `@RestController`s (`TeacherRestController`, `UserRestController`, `AuthRestController`, `EligibleRestController`). Controllers run bean-validation (`@Valid`) plus a secondary manual pass through `validator/` classes (`TeacherInsertValidator`, `TeacherEditValidator`, `UserInsertValidator`) against the `BindingResult`, then throw `ValidationException` if either layer produced errors.
- `service/` — interfaces (`ITeacherService`, `IUserService`, `IEligibleService`) + impls. Authorization is enforced here via `@PreAuthorize` (e.g. `hasAuthority('VIEW_TEACHERS')`), not in controllers. `TeacherServiceImpl.getTeacherByUUIDDeletedFalse` shows the "owner-or-privileged" pattern: `hasAuthority('VIEW_TEACHER') or (hasAuthority('VIEW_ONLY_TEACHER') and @securityService.isOwnTeacherProfile(#uuid, authentication))`.
- `model/` — JPA entities. All extend `AbstractEntity` (`@MappedSuperclass`) which provides `createdAt`/`updatedAt` (via `JpaAuditingConfig` + `AuditingEntityListener`) and a **soft-delete** convention (`deleted` boolean + `deletedAt`, set via `softDelete()`). Deletes never remove rows — repository/service methods that should exclude soft-deleted rows use explicit `...DeletedFalse` query methods (e.g. `findAllByDeletedFalse`, `findByUuidAndDeletedFalse`); plain `findByUuid`/`findAll` include soft-deleted rows, so pick the right one deliberately.
- `repository/` — Spring Data JPA repositories; `TeacherRepository` also supports `Specification<Teacher>` for dynamic filtering.
- `specification/TeacherSpecification` + `core/filters/TeacherFilters` — dynamic query filtering (lastname prefix, region, deleted flag) built from request query params bound via `@ModelAttribute`.
- `mapper/Mapper` — hand-written entity↔DTO mapping (no MapStruct). `mapToTeacherEntity` builds the full `Teacher` + nested `User` + `PersonalInfo` graph from a single `TeacherInsertDTO`.
- `dto/` — Java records for request/response payloads (`*InsertDTO`, `*UpdateDTO`, `*ReadOnlyDTO`).
- `security/` — `SecurityConfiguration` (stateless JWT filter chain, CORS from `allowed.origins`, per-endpoint `authorizeHttpRequests` rules), `JwtAuthenticationFilter`, `CustomAuthenticationEntryPoint`/`CustomAccessDeniedHandler` (401/403 JSON responses), `SecurityService` (bean `@securityService`, used from SpEL in `@PreAuthorize` for ownership checks).
- `authentication/` — `JwtService` (HS256 token issue/parse via `jjwt`), `AuthenticationService`, `CustomUserDetailsService`.
- `core/exceptions/` + `core/ErrorHandler` — domain exceptions (`EntityNotFoundException`, `EntityAlreadyExistsException`, `EntityInvalidArgumentException`, `ValidationException`, `FileUploadException`) are mapped to HTTP status codes in one central `@ControllerAdvice`. New failure cases should get a new typed exception rather than throwing generic exceptions from services.
- `core/MDCLoggingFilter` — puts request-scoped context into MDC for structured logging.

### Authorization model

Role/Capability many-to-many, seeded by Flyway (`V3__insert_roles_capabilites.sql`): roles are `ADMIN`, `EMPLOYEE`, `TEACHER`. Capabilities (`VIEW_TEACHERS`, `VIEW_TEACHER`, `VIEW_ONLY_TEACHER`, `EDIT_TEACHER`, `DELETE_TEACHER`, `INSERT_TEACHER`, ...) are granted to roles in that migration — ADMIN gets everything, EMPLOYEE gets view access, TEACHER gets only `VIEW_ONLY_TEACHER` (their own profile). JWTs carry the user's role as a claim; `SecurityConfiguration` maps HTTP verb+path combinations to required authorities, and `@PreAuthorize` in the service layer double-checks per-method authority (and ownership, via `SecurityService`). When adding a new protected capability, it needs: a row in the roles/capabilities migration data, a route rule in `SecurityConfiguration`, and a `@PreAuthorize` on the service method.

### Persistence

- Flyway migrations in `src/main/resources/db/migration/` (`V1__initial_schema.sql`, `V2__insert_regions.sql`, `V3__insert_roles_capabilites.sql`) are the source of truth for schema; `spring.jpa.hibernate.ddl-auto=validate`, so entities must match migrations exactly — schema changes go through a new `Vn__*.sql` migration, never `ddl-auto=update`.
- Reference/lookup SQL lives in `src/main/resources/db/queries/` (`teachers.sql`, `teacher_stats.sql`, `deleted_status.sql`) — useful for understanding expected query shapes (e.g. the `TeacherStatusReportView` DTO projection).
- Entities use UUID public identifiers (`uuid` column) separate from the internal `Long id` primary key — external APIs should always reference/accept `uuid`, not `id`.

### File uploads

`TeacherServiceImpl.saveAmkaFile` stores an uploaded AMKA document to `file.upload.dir` (default `uploads/`), detects content type with Apache Tika, and persists metadata as an `Attachment` linked from `PersonalInfo` (old file is deleted before the new one is attached — see `orphanRemoval` on `PersonalInfo.amkaFile`). The method is `@Retryable` on `IOException`/`HttpServerErrorException`.

### API docs

springdoc-openapi is wired via `core/OpenApiConfig`; Swagger UI is served at `/swagger-ui/**` and is permitted without auth in `SecurityConfiguration`.

## Notes

- A duplicate, stale copy of several classes exists in `./eduapp/` at the repo root (outside `src/`) — it is **not** part of the Gradle source set (`src/main/java/gr/aueb/cf/eduapp/` is canonical) and is not wired into the build. Ignore it; do not edit it thinking it is live code.
- Package names differ slightly between the app (`gr.aueb.cf.eduapp`) and the default generated test class (`gr.aueb.cf.edu10restapppostgress`) — this is a leftover from project generation, not a structural convention to imitate.
