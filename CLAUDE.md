# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Tech Stack

- Java 17
- Spring Boot 4.0.3
- Spring Security (JWT-based stateless auth)
- Spring Data JPA (Hibernate, MySQL)
- Spring Data Elasticsearch (Elasticsearch 8.x for full-text content search)
- Gradle 8.14 (wrapper included)
- JJWT 0.13.0 (JWT token handling)
- Qiniu Java SDK 7.16.0 (optional Qiniu OSS file storage)
- Apache Tika 3.2.0 (multi-format text extraction)
- PDFBox (PDF page-by-page text extraction)
- Lombok (boilerplate reduction: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor`, `@Slf4j`)
- RestTemplate (OpenAI-compatible API integration for invoice parsing)

## Build Commands

- `./gradlew build` — compile, test, and package
- `./gradlew bootRun` — run the application locally
- `./gradlew test` — run tests only

## Project Layout

- `src/main/java/com/moujitx/homebox/server/` — application source code
  - `config/` — Spring Security, AI, Async (@EnableAsync + ThreadPoolTaskExecutor)
  - `controller/` — REST API controllers
  - `dto/request/` — request DTOs with validation
  - `dto/response/` — response DTOs
  - `entity/` — JPA entities (Role, User, Good, GoodItem, GoodCategory, GoodBrand, GoodPicture, GoodAttachment, FileRecord, TextChunk, Asset, AssetCategory, AssetPlace, AssetStore, AssetPicture, AssetAttachment, AssetInvoice, Invoice, InvoiceAttachment, SystemConfig)
  - `enums/` — enumerations (GoodStatus, ItemStatus, InvoiceType, InvoiceStatus, WarrantyStatus, SourceType)
  - `util/` — utility classes (DateCalculator, StringUtil)
  - `exception/` — custom exceptions and global handler
  - `initializer/` — data seeding (root role/user on startup)
  - `repository/` — Spring Data JPA repositories
  - `security/` — JWT token provider, auth filter, UserDetailsService
  - `service/` — business logic (GoodService, AssetService, InvoiceService, InvoiceParseService, AiService, DashboardService, AuthService, MemberService, ProfileService, RoleService, FileService, SystemConfigService, FileStorageStrategyProvider, LocalStorageStrategy, QiniuStorageStrategy, TextExtractionService, ChunkingService, EsIndexService, SearchService, AssetAttachmentService, GoodAttachmentService, etc.)
- `src/main/resources/application.yml` — configuration (loads .env via spring.config.import)
- `src/test/java/com/moujitx/homebox/server/` — tests
- `docs/` — API docs, database schema, Postman collection

## Architecture

- Layered: Controller → Service → Repository → Entity
- Authentication: JWT tokens (stateless, no sessions)
- Authorization: Role-based (root role required for member/role management)
- Database: MySQL with Hibernate ddl-auto:update (auto-creates/updates tables)
- Configuration: .env file loaded via Spring Boot's native config import
- File Storage: Strategy pattern — `FileStorageStrategyProvider` selects between `LocalStorageStrategy` (default) and `QiniuStorageStrategy` based on the `qiniu.access-key` system config (seeded from `QINIU_ACCESS_KEY` env var on first startup, hot-reloadable via Settings UI). Max file size: 100MB.
- Content Search: Elasticsearch 8.17.0 with IK Analyzer for Chinese tokenization. Files uploaded via `FileService` are asynchronously processed through `TextExtractionService` (PDFBox for PDF page-by-page extraction, Tika for other formats) → `ChunkingService` (~500-char chunks with 50-char overlap) → `EsIndexService` (bulk index to `chunks` index). Search API uses `SearchService` with ES `multi_match` + highlight, then enriches results with MySQL source attribution (ASSET/GOOD/FILE). ES is opt-in: auto-configuration is excluded in `application.yml`; the `ElasticsearchClient` bean is only created by `ElasticsearchConfig` when `app.elasticsearch.host` is set (non-empty) via the `ES_HOST` env var. Without ES configured, the app starts normally and `EsClientProvider.isAvailable()` returns false (search degrades gracefully).
- AI Integration: OpenAI-compatible API for PDF/OFD invoice parsing (optional, configured via Settings UI or `ai.models` system config — supports multiple models, selectable active model, hot-reloadable)
- Invoice Preview: PDF/OFD files are rendered to PNG images at parse time (PDFBox PDFRenderer for PDF, ofdrw ImageMaker for OFD) and stored as base64 in the `invoices.preview_image` column. Existing invoices without a preview auto-generate one on first view.
- List Query Optimization: List endpoints use bulk repository queries (GROUP BY with Tuple projections) to fetch computed counts (subAssetCount, itemCountTotal, etc.) and first picture URLs in batch, avoiding N+1 query patterns. Response DTOs use `@AllArgsConstructor` for JPQL `SELECT new` constructor expressions (InvoiceResponse) or bulk-optimized factory methods accepting pre-fetched maps (AssetResponse, GoodResponse).
- Memory Optimization: JVM heap is capped at 256MB via `bootRun` task in `build.gradle` (`-Xmx256m`). Hibernate uses LAZY fetch for `@ManyToOne` associations (GoodPicture.file, AssetPicture.file, InvoiceAttachment.file) with global `default_batch_fetch_size: 16` and entity-level `@BatchSize` on collections to prevent N+1 queries. Invoice preview rendering uses 150 DPI (PDF) / scale 6 (OFD) with explicit `BufferedImage.flush()` to minimize peak memory.

## Rules

- Before coding, clarify and detail the requirements. The user's initial request may be high-level or incomplete — ask questions, identify edge cases, and flesh out the full scope before writing any code. Use the `feature-dev` skill when appropriate to analyze the codebase and produce a thorough implementation plan.
- After every task, immediately update `README.md` and `CLAUDE.md` if the changes warrant documentation updates (e.g. new features, changed commands, altered architecture, new dependencies, updated setup steps).
- Commit changes only after all steps are approved by the user. For large modifications containing multiple small tasks or features, commit at each small task/feature boundary rather than one big commit at the end.
- Do not edit or create tests unless explicitly noted.
- When API usage changes, update the API usage doc (`docs/api.md`) and the Postman collection file (`docs/homebox.postman_collection.json`). If either file does not exist, create it.
- When database schema changes, update `docs/database.md`.
- Use the latest stable versions of technologies, libraries, and frameworks. Code structure and content should follow common technical standards and best practices, but avoid over-engineering or unnecessary complexity.
