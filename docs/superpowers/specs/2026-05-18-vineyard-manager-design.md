# Vineyard Manager — Design Document

**Date:** 2026-05-18  
**Domain:** `vineyard-manager.velicans.eu`  
**Stack:** Java 21 + Spring Boot 3.x, React 18, PostgreSQL 16, Docker Compose, Cloudflare Tunnel

---

## 1. Overview

Web application for managing a small vineyard's production process across three linked stages: harvest (cules), pressing (stors), and bottling (îmbuteliere). The app is hosted locally via Docker and exposed publicly through a Cloudflare Tunnel. No authentication required — internal use only.

---

## 2. Architecture

Four containers in a single `docker-compose.yml`, connected via an internal Docker network (`vineyard-net`):

| Container        | Image / Build       | Role                                      |
|------------------|---------------------|-------------------------------------------|
| `vineyard-db`    | PostgreSQL 16       | Persistent data storage                   |
| `vineyard-api`   | Build from ./backend | Spring Boot REST API (port 8080 internal) |
| `vineyard-ui`    | Build from ./frontend | Nginx serving React SPA (port 3000 internal) |
| `cloudflared`    | cloudflare/cloudflared | Cloudflare Tunnel — routes external traffic |

**Traffic routing via Cloudflare Tunnel (`vineyard-tunnel`):**
- `/api/**`, `/swagger-ui/**`, `/v3/**` → `vineyard-api:8080`
- `/**` → `vineyard-ui:3000`

Tunnel config follows the same pattern as the existing `n8n-local` setup: named tunnel in `~/.cloudflared`, mounted as a volume into the `cloudflared` container.

No host ports are exposed externally beyond what is needed for local access.

**Repository structure:**
```
vineyard-manager/
  backend/           # Spring Boot project (Maven)
  frontend/          # React project (Vite)
  docker-compose.yml
  docs/
```

---

## 3. Data Model

### Entities

**Parcel**
- `id` (UUID)
- `name` (String)
- `grapeVariety` (String)
- `areaSqM` (Integer)

**ProductionBatch**
- `id` (UUID)
- `parcelId` (FK → Parcel)
- `year` (Integer)
- `status` (Enum: `HARVESTED` | `PRESSED` | `BOTTLED`)

**Harvest**
- `id` (UUID)
- `batchId` (FK → ProductionBatch)
- `date` (LocalDate)
- `quantityKg` (BigDecimal)

**Pressing**
- `id` (UUID)
- `batchId` (FK → ProductionBatch)
- `date` (LocalDate)
- `mustLiters` (BigDecimal)
- `yieldRatio` (BigDecimal) — calculated at creation: `mustLiters ÷ harvest.quantityKg`

**Bottling**
- `id` (UUID)
- `batchId` (FK → ProductionBatch)
- `date` (LocalDate)
- `bottleCount` (Integer)
- `bottleVolume` (Enum: `L075` | `L150`) — fixed presets, no custom values

### Production Flow

A `ProductionBatch` is created linked to a parcel → `Harvest` is recorded → `Pressing` is recorded → `Bottling` is recorded. Batch status advances automatically at each completed stage. Each batch belongs to exactly one parcel.

---

## 4. Backend (Spring Boot)

### Package Structure
```
ro.velicans.vineyard
  ├── parcel/        (Entity, Repository, Service, Controller, DTO)
  ├── batch/
  ├── harvest/
  ├── pressing/
  ├── bottling/
  └── reports/
```

### API Endpoints

```
# Parcels
GET    /api/parcels
POST   /api/parcels
GET    /api/parcels/{id}

# Batches
GET    /api/batches
POST   /api/batches
GET    /api/batches/{id}

# Stage progression
POST   /api/batches/{id}/harvest
POST   /api/batches/{id}/pressing
POST   /api/batches/{id}/bottling

# Reports
GET    /api/reports/harvest?year=
GET    /api/reports/pressing?year=
GET    /api/reports/bottling?year=
```

### Key Dependencies
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `springdoc-openapi-starter-webmvc-ui` — Swagger UI at `/swagger-ui.html`, spec at `/v3/api-docs`
- `flyway-core` — versioned SQL migrations, no auto-DDL
- `postgresql` driver

### Error Handling
`@ControllerAdvice` returns structured JSON errors: `{ "code": "...", "message": "..." }` for all 4xx/5xx responses.

### Testing
Unit tests on the Service layer using JUnit 5 + Mockito.

---

## 5. Frontend (React)

### Technologies
- React 18, React Router v6
- Axios for API calls
- TanStack Query for server state management
- Tailwind CSS for styling (no heavy component library)
- Vite as build tool

### Pages / Routes

| Route                  | Description                                                        |
|------------------------|--------------------------------------------------------------------|
| `/`                    | Dashboard — production summary + recent batches                    |
| `/parcels`             | List of 3 parcels with details                                     |
| `/batches`             | All batches, filterable by year / parcel / status                  |
| `/batches/new`         | Create new batch (select parcel)                                   |
| `/batches/:id`         | Batch detail — completed stages + form for next stage              |
| `/reports/harvest`     | Harvest report (total kg per parcel/year, averages)                |
| `/reports/pressing`    | Pressing report (total must liters, average yield)                 |
| `/reports/bottling`    | Bottling report (total bottles by volume/year)                     |

### Batch Detail Logic
The `/batches/:id` page auto-detects the next stage from `batch.status` and renders the appropriate form (Harvest → Pressing → Bottling). When status is `BOTTLED`, no form is shown.

### Error Handling
API errors are displayed inline on forms. No dedicated error pages.

---

## 6. Infrastructure

### docker-compose.yml (structure)

```yaml
services:
  vineyard-db:
    image: postgres:16
    environment:
      POSTGRES_DB: vineyard
      POSTGRES_USER: vineyard
      POSTGRES_PASSWORD: <secret>
    volumes:
      - vineyard-db-data:/var/lib/postgresql/data
    networks:
      - vineyard-net

  vineyard-api:
    build: ./backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://vineyard-db:5432/vineyard
      SPRING_DATASOURCE_USERNAME: vineyard
      SPRING_DATASOURCE_PASSWORD: <secret>
    depends_on:
      - vineyard-db
    networks:
      - vineyard-net

  vineyard-ui:
    build: ./frontend
    depends_on:
      - vineyard-api
    networks:
      - vineyard-net

  cloudflared:
    image: cloudflare/cloudflared:latest
    restart: unless-stopped
    command: tunnel run vineyard-tunnel
    volumes:
      - ~/.cloudflared:/home/nonroot/.cloudflared
    depends_on:
      - vineyard-api
      - vineyard-ui
    networks:
      - vineyard-net

volumes:
  vineyard-db-data:

networks:
  vineyard-net:
```

### Nginx Config (vineyard-ui container)
Routes `/api/`, `/swagger-ui/`, `/v3/` to `vineyard-api:8080`; all other requests serve `index.html` for SPA routing.

### Frontend Dockerfile
Multi-stage: Node build stage → Nginx serving the static build output.

---

## 7. Constraints & Decisions

- **No authentication** — internal use, trusted network
- **Bottle volumes are fixed** — `L075` and `L150` only, hardcoded enum, no UI to add others
- **3 parcels** — fixed in practice, but Parcel is a proper entity (not hardcoded) to allow future flexibility
- **yieldRatio is stored** — not computed on read, so historical data remains accurate if harvest quantity is ever corrected
- **Flyway migrations** — schema managed explicitly, not via `spring.jpa.hibernate.ddl-auto`
