# AGENTS.md

## Quick Commands

```bash
./gradlew build          # compile + test + package
./gradlew bootRun        # run locally (port 8080, JVM capped at 256MB)
./gradlew test           # tests only
./gradlew sonar          # SonarQube analysis (needs SONAR_TOKEN + SONAR_HOST_URL)
```

## Environment Setup

- `.env` in project root is the config source (loaded via `spring.config.import`). It's gitignored.
- Required `.env` vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `ROOT_USERNAME`, `ROOT_PASSWORD`, `JWT_SECRET`
- Optional: `ES_HOST`/`ES_PORT` (Elasticsearch), `QINIU_*` (OSS storage)
- Hibernate `ddl-auto: update` — schema auto-creates/updates. No migration tool.
- MySQL required. App connects at startup; fails fast if DB unreachable.

## Architecture Gotchas

- **Elasticsearch is always autoconfigured**: `spring-boot-starter-data-elasticsearch` is on classpath and `spring.elasticsearch.uris` defaults to `localhost:9200`. The `ElasticsearchClient` bean is always created; `EsClientProvider` uses `@Autowired(required = false)` as a safety net. Search is toggled at runtime via the `elasticsearch.enabled` system config (Settings UI).
- **File storage uses Strategy pattern**: `FileStorageStrategyProvider` selects `LocalStorageStrategy` (default) or `QiniuStorageStrategy` based on `qiniu.access-key` in `system_config` table. Hot-reloadable via Settings UI.
- **JVM heap capped at 256MB** (`-Xmx256m` in `bootRun` task). Don't assume more memory is available.
- **Lombok everywhere**: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@RequiredArgsConstructor`, `@Slf4j`. Entity DTOs use `@AllArgsConstructor` for JPQL `SELECT new` constructor expressions.
- **N+1 prevention**: `default_batch_fetch_size: 16` globally, `@BatchSize` on collections, bulk repository queries with GROUP BY for list endpoints.
- **Invoice preview**: PDF/OFD rendered to PNG at parse time (PDFBox/ofdrw), stored as base64 in `invoices.preview_image`. Auto-generates on first view if missing.
- **AI invoice parsing**: OpenAI-compatible API for PDF/OFD parsing (optional, configured via Settings UI). Multi-model support, hot-reloadable.
- **Text extraction pipeline** (async via `@EnableAsync`): `FileService` → `TextExtractionService` (PDFBox for PDF page-by-page, Tika for other formats) → `ChunkingService` (~500-char chunks, 50-char overlap) → `EsIndexService` (bulk index to `chunks` index). IK Analyzer for Chinese tokenization.

## Documentation Rules

After code changes, update:
- `README.md` and `AGENTS.md` if features/commands/architecture/deps change
- `docs/api.md` and `docs/homebox.postman_collection.json` if API endpoints change
- `docs/database.md` if schema changes

Do **not** edit or create tests unless explicitly instructed.

## CI

- Gitea workflows (not GitHub Actions)
- `build.yml`: triggered on `v*` tags, builds JAR, creates GitHub release, deploys to docker repo
- `sonar.yml`: triggered on push to `main`, runs `./gradlew build sonar`
