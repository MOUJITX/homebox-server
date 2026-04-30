# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Tech Stack

- Java 17
- Spring Boot 4.0.3
- Spring Security (JWT-based stateless auth)
- Spring Data JPA (Hibernate, MySQL)
- Gradle 8.14 (wrapper included)
- JJWT 0.13.0 (JWT token handling)
- Lombok (boilerplate reduction: `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Slf4j`)

## Build Commands

- `./gradlew build` — compile, test, and package
- `./gradlew bootRun` — run the application locally
- `./gradlew test` — run tests only

## Project Layout

- `src/main/java/com/moujitx/homebox/server/` — application source code
  - `config/` — Spring Security configuration
  - `controller/` — REST API controllers
  - `dto/request/` — request DTOs with validation
  - `dto/response/` — response DTOs
  - `entity/` — JPA entities (Role, User, Good, GoodItem, GoodCategory, GoodBrand, GoodPicture, FileRecord)
  - `enums/` — enumerations (GoodStatus)
  - `exception/` — custom exceptions and global handler
  - `initializer/` — data seeding (root role/user on startup)
  - `repository/` — Spring Data JPA repositories
  - `security/` — JWT token provider, auth filter, UserDetailsService
  - `service/` — business logic layer
- `src/main/resources/application.yml` — configuration (loads .env via spring.config.import)
- `src/test/java/com/moujitx/homebox/server/` — tests
- `docs/` — API docs, database schema, Postman collection

## Architecture

- Layered: Controller → Service → Repository → Entity
- Authentication: JWT tokens (stateless, no sessions)
- Authorization: Role-based (root role required for member/role management)
- Database: MySQL with Hibernate ddl-auto:update (auto-creates/updates tables)
- Configuration: .env file loaded via Spring Boot's native config import

## Rules

- Before coding, clarify and detail the requirements. The user's initial request may be high-level or incomplete — ask questions, identify edge cases, and flesh out the full scope before writing any code. Use the `feature-dev` skill when appropriate to analyze the codebase and produce a thorough implementation plan.
- After every task, immediately update `README.md` and `CLAUDE.md` if the changes warrant documentation updates (e.g. new features, changed commands, altered architecture, new dependencies, updated setup steps).
- Commit changes only after all steps are approved by the user. For large modifications containing multiple small tasks or features, commit at each small task/feature boundary rather than one big commit at the end.
- Do not edit or create tests unless explicitly noted.
- When API usage changes, update the API usage doc (`docs/api.md`) and the Postman collection file (`docs/homebox.postman_collection.json`). If either file does not exist, create it.
- When database schema changes, update `docs/database.md`.
- Use the latest stable versions of technologies, libraries, and frameworks. Code structure and content should follow common technical standards and best practices, but avoid over-engineering or unnecessary complexity.
