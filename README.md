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
├── config/                           # Security & AI configuration
├── controller/                       # REST controllers (19 controllers)
├── dto/request/                      # Request DTOs
├── dto/response/                     # Response DTOs
├── entity/                           # JPA entities (Role, User, Good, GoodItem, GoodCategory, GoodBrand, GoodPicture, FileRecord, Asset, AssetCategory, AssetPlace, AssetStore, AssetPicture, AssetInvoice, Invoice, InvoiceAttachment, SystemConfig)
├── enums/                            # Enumerations (GoodStatus, ItemStatus, InvoiceType, InvoiceStatus, WarrantyStatus)
├── exception/                        # Exception handling
├── initializer/                      # Data seeding on startup
├── repository/                       # Spring Data repositories
├── security/                         # JWT filter and token provider
├── util/                             # Utility classes (DateCalculator, StringUtil)
└── service/                          # Business logic (24 services)
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

By default, files are stored on the local filesystem. To enable Qiniu OSS, configure it via the Settings page in the web UI, or seed initial values via `.env`:

```properties
QINIU_ACCESS_KEY=your_access_key
QINIU_SECRET_KEY=your_secret_key
QINIU_BUCKET=your_bucket_name
QINIU_FOLDER=your_folder_name
QINIU_CDN_DOMAIN=https://your-cdn-domain.com
```

On first startup, these env vars are seeded into the `system_config` table. Subsequent changes can be made via the Settings UI (root only), which hot-reloads the storage strategy without restarting. When no Qiniu access key is configured, local filesystem storage is used.

**Optional: AI-powered invoice parsing**

PDF and OFD invoice files are parsed using an AI model (OpenAI-compatible API). Configure AI models via the Settings page in the web UI (supports multiple models, select active model, test connections). Initial models can be seeded via the `ai.models` system config (JSON array in the `system_config` table). When no AI model is configured, PDF/OFD formats return empty results (XML parsing is always available as it uses direct XML extraction).

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
- `GET /api/profile` — get current user profile
- `PUT /api/profile` — update current user profile
- `PUT /api/profile/password` — change current user password
- `GET/POST /api/members` — list/create members (root only)
- `GET/PUT/DELETE /api/members/{id}` — get/update/delete member (root only)
- `GET/POST /api/roles` — list/create roles (root only)
- `GET/PUT/DELETE /api/roles/{id}` — get/update/delete role (root only)
- `GET/POST /api/goods` — list/create goods with pagination and filtering
- `GET/PUT/DELETE /api/goods/{id}` — get/update/delete good
- `GET /api/goods/barcode/{barcode}` — look up good by barcode
- `GET/POST /api/goods/{id}/items` — list/create good items
- `PUT/DELETE /api/goods/{id}/items/{itemId}` — update/delete good item
- `POST /api/goods/{id}/pictures` — upload picture for a good
- `GET /api/goods/{id}/pictures/{pid}/file` — serve picture inline
- `DELETE /api/goods/{id}/pictures/{pid}` — delete picture
- `GET/POST /api/good-categories` — list/create good categories
- `GET/PUT/DELETE /api/good-categories/{id}` — get/update/delete good category
- `GET/POST /api/good-brands` — list/create good brands
- `GET/PUT/DELETE /api/good-brands/{id}` — get/update/delete good brand
- `GET /api/system-config?group={group}` — get system config by group
- `PUT /api/system-config/{group}` — update system config group
- `POST /api/system-config/test/qiniu` — test Qiniu OSS connection
- `POST /api/system-config/test/ai` — test AI model connection
- `GET /api/assets/{id}/invoices` — list invoices bound to an asset
- `POST /api/assets/{assetId}/invoices/{invoiceId}` — bind invoice to asset
- `DELETE /api/assets/{assetId}/invoices/{invoiceId}` — unbind invoice from asset
- `GET/POST /api/files` — list/upload files
- `GET /api/files/{id}` — get file metadata
- `GET /api/files/{id}/preview` — preview file inline
- `GET /api/files/{id}/download` — download file with original filename
- `PATCH /api/files/{id}/rename` — rename a file
- `DELETE /api/files/{id}` — delete file
- `GET/POST /api/assets` — list/create assets with pagination and filtering
- `GET/PUT/DELETE /api/assets/{id}` — get/update/delete asset
- `POST /api/assets/{id}/pictures` — upload picture for an asset
- `GET /api/assets/{id}/pictures/{pid}/file` — serve asset picture inline
- `DELETE /api/assets/{id}/pictures/{pid}` — delete asset picture
- `GET/POST /api/asset-categories` — list/create asset categories
- `GET/PUT/DELETE /api/asset-categories/{id}` — get/update/delete asset category
- `GET/POST /api/asset-places` — list/create asset places
- `GET/PUT/DELETE /api/asset-places/{id}` — get/update/delete asset place
- `GET/POST /api/asset-stores` — list/create asset stores
- `GET/PUT/DELETE /api/asset-stores/{id}` — get/update/delete asset store
- `GET/POST /api/invoices` — list/create invoices with pagination and filtering
- `GET/PUT/DELETE /api/invoices/{id}` — get/update/delete invoice
- `POST /api/invoices/parse` — parse invoice file (PDF/XML/OFD)
- `GET /api/invoices/{id}/file/preview` — preview invoice file inline
- `GET /api/invoices/{id}/file/download` — download invoice file
- `POST /api/invoices/{id}/attachments` — upload invoice attachment
- `GET /api/invoices/{id}/attachments/{aid}/file` — serve attachment file
- `DELETE /api/invoices/{id}/attachments/{aid}` — delete attachment

See [docs/api.md](docs/api.md) for full API documentation.
