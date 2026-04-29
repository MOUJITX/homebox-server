# HomeBox

A Spring Boot backend application for home management with member and role-based access control.

## Tech Stack

- Java 17
- Spring Boot 4.0.3
- Spring Security (JWT authentication)
- Spring Data JPA (Hibernate)
- MySQL
- Gradle 8.14

## Project Structure

```
src/main/java/com/moujitx/homebox/server/
├── HomeBoxApplication.java           # Application entry point
├── config/                           # Security configuration
├── controller/                       # REST controllers
├── dto/request/                      # Request DTOs
├── dto/response/                     # Response DTOs
├── entity/                           # JPA entities
├── exception/                        # Exception handling
├── initializer/                      # Data seeding on startup
├── repository/                       # Spring Data repositories
├── security/                         # JWT filter and token provider
└── service/                          # Business logic
```

## Getting Started

### Prerequisites

- JDK 17+
- MySQL 8.0+

### Setup

1. Create a `.env` file in the project root:

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=homebox
DB_USERNAME=root
DB_PASSWORD=your_db_password
ROOT_USERNAME=admin
ROOT_PASSWORD=admin123
JWT_SECRET=your-256-bit-secret-key-that-is-at-least-32-characters-long
```

2. Ensure MySQL is running and accessible with the credentials above. The database will be created automatically if it doesn't exist.

### Build & Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The server starts on `http://localhost:8080` by default.

### First Login

On first startup, the application automatically creates:
- Roles: `root`, `member`
- A root user with credentials from `ROOT_USERNAME` / `ROOT_PASSWORD` env vars

The root user must change their password on first login (`forceChangePassword: true`).

## API Overview

- `POST /api/auth/login` — authenticate and get JWT token
- `POST /api/auth/change-password` — change password (authenticated)
- `GET/POST /api/members` — list/create members (root only)
- `GET/PUT/DELETE /api/members/{id}` — get/update/delete member (root only)
- `GET/POST /api/roles` — list/create roles (root only)
- `GET/PUT/DELETE /api/roles/{id}` — get/update/delete role (root only)

See [docs/api.md](docs/api.md) for full API documentation.
