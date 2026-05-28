# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

滁州亭城GIS系统 (TingChengGIS) — Spring Boot 3.2 / Java 21 GIS backend serving a Leaflet (2D) + Cesium (3D) frontend. The domain is the catalogue of pavilions (亭子) in Chuzhou, with geospatial query, multi-point routing, AI-generated cultural content, and OSRM-based navigation. Frontend is plain HTML/JS in `src/main/resources/static/` (single-page `index.html`).

## Build & Run

- Build: `mvn clean install`
- Run (dev profile, H2 in-memory): `mvn spring-boot:run`
- App URL: `http://localhost:8092` — H2 console at `/h2-console`
- Production profile: set `--spring.profiles.active=prod` to use PostgreSQL/PostGIS (`tingchenggis_pg` on `localhost:5432`).
- Run all tests: `mvn test`
- Run a single test class: `mvn test -Dtest=PavilionServiceImplTest`
- Run a single test method: `mvn test -Dtest=PavilionServiceImplTest#methodName`

## Bootstrap data

`data/凉亭汇总表.xlsx` (228 rows) is the canonical source. On a fresh DB, `DataInitializer` logs a hint to import it via `POST /thousand-pavilions/import` (multipart). Default seeded accounts: admin `419116` / user `206004` (password = username). Pavilion data must be imported before most map/route features work — there is no SQL seeding.

## Architecture

Standard Spring layered architecture under `com.tingchenggis.tingcheng`:
- `controller/` — REST endpoints. Two route prefixes coexist: `/thousand-pavilions/*` (the main pavilion CRUD + tour features) and `/pavilions/*`, `/pavilions-gis/*` (lower-level GIS operations). Treat them as separate surfaces — they share the `Pavilion` entity but expose different shapes.
- `service/` interfaces + `service/impl/` implementations. Services are split by concern (`PavilionService`, `PavilionGISService`, `PavilionImportService`, `PavilionExportService`, `PavilionCollectorService`, `ThousandPavilionsService`) — when modifying pavilion behavior, identify which service owns it before editing.
- `entity/` — JPA entities. `Pavilion` stores geometry as **WKT text** in `geom_wkt` plus separate `longitude`/`latitude` doubles, with parallel `longitudeGcj`/`latitudeGcj` for GCJ-02 (高德/腾讯). This dual storage is intentional for cross-DB portability (H2 ↔ PostGIS) — don't switch to native geometry types.
- `ai/AiService.java` — multi-provider LLM client (DeepSeek / Zhipu / OpenAI), selected by `tingcheng.ai.active-provider`. Falls back to hand-written Chinese templates when no API key is configured. Default active provider is `deepseek`.
- `service/RoutingClient.java` — calls public OSRM (`router.project-osrm.org`) for routing/navigation steps. Network-dependent; expect failures in offline test runs.
- `security/` — JWT-based stateless auth. `SecurityConfig` whitelists most `GET` endpoints + `/ai/**` + `/ogc/**` for the demo; writes (`POST`/`PUT`/`DELETE`) require auth; admin-only paths: `/transport-routes/build-network`, `/transport-routes/build-multi-modal`, `/coordinate/correct-pavilions`, `/osm/import/**`.
- `config/DataInitializer.java` — `CommandLineRunner` that seeds users and logs the import hint. No automatic data load.
- `util/CoordinateTransform.java` — WGS-84 ↔ GCJ-02 conversion. Use this when adding any new lat/lng field; do not write inline conversions.

### Key cross-cutting decisions

- **Coordinates**: persist WGS-84 in `longitude`/`latitude` *and* GCJ-02 in `*Gcj` columns. Frontend maps that need GCJ-02 (Gaode/Tencent) read the `*Gcj` fields directly — no client-side conversion.
- **Routing**: OSRM is the single backend for shortest path, navigation steps, and TSP-based "optimal route". TSP solving is in `util/TspSolver.java`.
- **AI**: `AiService` always provides a usable response — if the API key is missing or the call fails, it returns a baked-in Chinese template tied to 醉翁亭记/欧阳修 themes. Do not assume AI calls succeed; preserve the template fallback when editing.
- **Security whitelist**: when adding a new public read endpoint, add it to `SecurityConfig.filterChain` — otherwise it returns 401 even for `GET`.
- **Static frontend**: `WebConfig` forwards `/`, `/index`, `/home` → `/index.html`. Static resources are served from `classpath:/static/`.

## Conventions

- Code, log messages, and user-facing strings are predominantly Chinese. Match existing tone and language when adding logs or error messages.
- Test suite has 175 tests (per most recent commit). Run them after non-trivial service/controller changes.
- The dev profile uses `ddl-auto: create-drop` — H2 schema is wiped on every restart; no migrations.

## External services

- OSRM public server: `https://router.project-osrm.org` — required for routing endpoints.
- AI providers (one of): DeepSeek / Zhipu / OpenAI. Configured under `tingcheng.ai.*` in `application.yml`. The repo currently has live keys committed in `application.yml` — flag this if rotating or refactoring AI config.
