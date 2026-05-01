# HomeBox

A Spring Boot backend application for home management with goods expiration tracking, asset management, and role-based access control.

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
├── entity/                           # JPA entities (including Asset, Place, Store)
├── exception/                        # Exception handling
├── initializer/                      # Data seeding on startup
├── repository/                       # Spring Data repositories
├── security/                         # JWT filter and token provider
├── util/                             # Utility classes (DateCalculator)
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

**Optional: Qiniu OSS file storage**

By default, files are stored on the local filesystem. To use Qiniu OSS instead, add these to your `.env` file:

```properties
QINIU_ACCESS_KEY=your_access_key
QINIU_SECRET_KEY=your_secret_key
QINIU_BUCKET=your_bucket_name
QINIU_FOLDER=your_folder_name
QINIU_CDN_DOMAIN=https://your-cdn-domain.com
```

When `QINIU_ACCESS_KEY` is set, all file uploads will go to Qiniu OSS. When unset, local filesystem storage is used.

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
- `POST /api/files` — upload any file
- `GET /api/files/{id}` — get file metadata
- `GET /api/files/{id}/download` — download file with original filename
- `DELETE /api/files/{id}` — delete file
- `POST /api/goods/{id}/pictures` — upload picture for a good
- `GET /api/goods/{id}/pictures/{pid}/file` — serve picture inline
- `DELETE /api/goods/{id}/pictures/{pid}` — delete picture
- `GET/POST /api/assets` — list/create assets (authenticated)
- `GET/PUT/DELETE /api/assets/{id}` — get/update/delete asset (authenticated)
- `POST /api/assets/{id}/pictures` — upload picture for an asset
- `GET /api/assets/{id}/pictures/{pid}/file` — serve asset picture inline
- `DELETE /api/assets/{id}/pictures/{pid}` — delete asset picture
- `GET/POST /api/places` — list/create places (authenticated)
- `GET/PUT/DELETE /api/places/{id}` — get/update/delete place (authenticated)
- `GET/POST /api/stores` — list/create stores (authenticated)
- `GET/PUT/DELETE /api/stores/{id}` — get/update/delete store (authenticated)

See [docs/api.md](docs/api.md) for full API documentation.
