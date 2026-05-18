# Vineyard Manager Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a local Docker-hosted vineyard production management app (harvest → pressing → bottling) with a Spring Boot API and React frontend exposed via Cloudflare Tunnel.

**Architecture:** Four Docker containers (vineyard-db, vineyard-api, vineyard-ui, cloudflared) on an internal network. Nginx in the UI container proxies `/api/**`, `/swagger-ui/**`, `/v3/**` to the API; all other routes serve the React SPA.

**Tech Stack:** Java 21, Spring Boot 3.x (Maven), PostgreSQL 16, Flyway, springdoc-openapi, React 18, Vite, Tailwind CSS, TanStack Query, React Router v6, Axios, Docker Compose, Cloudflare Tunnel.

---

## File Map

### Backend (`backend/`)
```
src/main/java/ro/velicans/vineyard/
  VineyardApplication.java
  config/
    OpenApiConfig.java
    GlobalExceptionHandler.java
  parcel/
    Parcel.java                  (Entity)
    ParcelRepository.java
    ParcelService.java
    ParcelController.java
    ParcelDto.java
  batch/
    ProductionBatch.java         (Entity)
    BatchStatus.java             (Enum)
    ProductionBatchRepository.java
    ProductionBatchService.java
    ProductionBatchController.java
    ProductionBatchDto.java
  harvest/
    Harvest.java
    HarvestRepository.java
    HarvestService.java
    HarvestController.java
    HarvestDto.java
  pressing/
    Pressing.java
    PressingRepository.java
    PressingService.java
    PressingController.java
    PressingDto.java
  bottling/
    Bottling.java
    BottleVolume.java            (Enum: L075, L150)
    BottlingRepository.java
    BottlingService.java
    BottlingController.java
    BottlingDto.java
  reports/
    ReportService.java
    ReportController.java
    HarvestReportDto.java
    PressingReportDto.java
    BottlingReportDto.java

src/main/resources/
  application.properties
  db/migration/
    V1__create_parcels.sql
    V2__create_production_batches.sql
    V3__create_harvests.sql
    V4__create_pressings.sql
    V5__create_bottlings.sql

src/test/java/ro/velicans/vineyard/
  parcel/ParcelServiceTest.java
  batch/ProductionBatchServiceTest.java
  harvest/HarvestServiceTest.java
  pressing/PressingServiceTest.java
  bottling/BottlingServiceTest.java
  reports/ReportServiceTest.java

pom.xml
Dockerfile
```

### Frontend (`frontend/`)
```
src/
  main.jsx
  App.jsx
  api/
    client.js            (Axios instance)
    parcels.js
    batches.js
    reports.js
  pages/
    Dashboard.jsx
    ParcelList.jsx
    BatchList.jsx
    BatchNew.jsx
    BatchDetail.jsx
    ReportHarvest.jsx
    ReportPressing.jsx
    ReportBottling.jsx
  components/
    Layout.jsx
    NavBar.jsx
    HarvestForm.jsx
    PressingForm.jsx
    BottlingForm.jsx
    StageTimeline.jsx
index.html
vite.config.js
tailwind.config.js
postcss.config.js
Dockerfile
nginx.conf
package.json
```

### Root
```
docker-compose.yml
.env.example
```

---

## Task 1: Repo & project scaffolding

**Files:**
- Create: `backend/pom.xml`
- Create: `frontend/package.json`, `frontend/vite.config.js`, `frontend/tailwind.config.js`, `frontend/postcss.config.js`
- Create: `.gitignore`

- [ ] **Step 1: Initialize git repo**

```bash
cd /Users/sorin/workspace/vineyard-mnager
git init
```

- [ ] **Step 2: Create .gitignore**

```
# Java
backend/target/
*.class
*.jar

# Node
frontend/node_modules/
frontend/dist/

# Env
.env

# IDE
.idea/
*.iml
.vscode/
```

- [ ] **Step 3: Create backend/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
  </parent>
  <groupId>ro.velicans</groupId>
  <artifactId>vineyard</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <properties>
    <java.version>21</java.version>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-database-postgresql</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.5.0</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 4: Create frontend/package.json**

```json
{
  "name": "vineyard-ui",
  "version": "0.0.1",
  "private": true,
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "react": "^18.3.0",
    "react-dom": "^18.3.0",
    "react-router-dom": "^6.23.0",
    "@tanstack/react-query": "^5.40.0",
    "axios": "^1.7.0"
  },
  "devDependencies": {
    "@vitejs/plugin-react": "^4.3.0",
    "vite": "^5.3.0",
    "tailwindcss": "^3.4.0",
    "postcss": "^8.4.0",
    "autoprefixer": "^10.4.0"
  }
}
```

- [ ] **Step 5: Create frontend/vite.config.js**

```js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/swagger-ui': 'http://localhost:8080',
      '/v3': 'http://localhost:8080',
    }
  }
})
```

- [ ] **Step 6: Create frontend/tailwind.config.js**

```js
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: { extend: {} },
  plugins: [],
}
```

- [ ] **Step 7: Create frontend/postcss.config.js**

```js
export default {
  plugins: { tailwindcss: {}, autoprefixer: {} }
}
```

- [ ] **Step 8: Commit**

```bash
git add .
git commit -m "chore: initial project scaffold"
```

---

## Task 2: Backend — application entry point & config

**Files:**
- Create: `backend/src/main/java/ro/velicans/vineyard/VineyardApplication.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/config/GlobalExceptionHandler.java`
- Create: `backend/src/main/resources/application.properties`

- [ ] **Step 1: Create VineyardApplication.java**

```java
package ro.velicans.vineyard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VineyardApplication {
    public static void main(String[] args) {
        SpringApplication.run(VineyardApplication.class, args);
    }
}
```

- [ ] **Step 2: Create GlobalExceptionHandler.java**

```java
package ro.velicans.vineyard.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("code", "NOT_FOUND", "message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleBadState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("code", "CONFLICT", "message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadArg(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("code", "BAD_REQUEST", "message", ex.getMessage()));
    }
}
```

- [ ] **Step 3: Create application.properties**

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/vineyard}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:vineyard}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:vineyard}
spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
spring.flyway.locations=classpath:db/migration
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
```

- [ ] **Step 4: Commit**

```bash
git add backend/src
git commit -m "feat: backend entry point and global error handler"
```

---

## Task 3: Flyway migrations

**Files:**
- Create: `backend/src/main/resources/db/migration/V1__create_parcels.sql`
- Create: `backend/src/main/resources/db/migration/V2__create_production_batches.sql`
- Create: `backend/src/main/resources/db/migration/V3__create_harvests.sql`
- Create: `backend/src/main/resources/db/migration/V4__create_pressings.sql`
- Create: `backend/src/main/resources/db/migration/V5__create_bottlings.sql`

- [ ] **Step 1: V1__create_parcels.sql**

```sql
CREATE TABLE parcel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    grape_variety VARCHAR(100) NOT NULL,
    area_sq_m INTEGER NOT NULL
);
```

- [ ] **Step 2: V2__create_production_batches.sql**

```sql
CREATE TYPE batch_status AS ENUM ('HARVESTED', 'PRESSED', 'BOTTLED');

CREATE TABLE production_batch (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parcel_id UUID NOT NULL REFERENCES parcel(id),
    year INTEGER NOT NULL,
    status batch_status NOT NULL
);
```

- [ ] **Step 3: V3__create_harvests.sql**

```sql
CREATE TABLE harvest (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL UNIQUE REFERENCES production_batch(id),
    date DATE NOT NULL,
    quantity_kg NUMERIC(10,2) NOT NULL
);
```

- [ ] **Step 4: V4__create_pressings.sql**

```sql
CREATE TABLE pressing (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL UNIQUE REFERENCES production_batch(id),
    date DATE NOT NULL,
    must_liters NUMERIC(10,2) NOT NULL,
    yield_ratio NUMERIC(6,4) NOT NULL
);
```

- [ ] **Step 5: V5__create_bottlings.sql**

```sql
CREATE TYPE bottle_volume AS ENUM ('L075', 'L150');

CREATE TABLE bottling (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id UUID NOT NULL UNIQUE REFERENCES production_batch(id),
    date DATE NOT NULL,
    bottle_count INTEGER NOT NULL,
    bottle_volume bottle_volume NOT NULL
);
```

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/resources/db
git commit -m "feat: flyway schema migrations V1-V5"
```

---

## Task 4: Parcel module

**Files:**
- Create: `backend/src/main/java/ro/velicans/vineyard/parcel/Parcel.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/parcel/ParcelRepository.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/parcel/ParcelDto.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/parcel/ParcelService.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/parcel/ParcelController.java`
- Create: `backend/src/test/java/ro/velicans/vineyard/parcel/ParcelServiceTest.java`

- [ ] **Step 1: Write failing test**

```java
// backend/src/test/java/ro/velicans/vineyard/parcel/ParcelServiceTest.java
package ro.velicans.vineyard.parcel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelServiceTest {

    @Mock ParcelRepository repo;
    @InjectMocks ParcelService service;

    @Test
    void findAll_returnsMappedDtos() {
        Parcel p = new Parcel();
        p.setId(UUID.randomUUID());
        p.setName("Nord");
        p.setGrapeVariety("Feteasca Alba");
        p.setAreaSqM(1200);
        when(repo.findAll()).thenReturn(List.of(p));

        List<ParcelDto> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Nord");
    }

    @Test
    void findById_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(id))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void create_savesAndReturnsDto() {
        ParcelDto req = new ParcelDto(null, "Sud", "Merlot", 800);
        Parcel saved = new Parcel();
        saved.setId(UUID.randomUUID());
        saved.setName("Sud");
        saved.setGrapeVariety("Merlot");
        saved.setAreaSqM(800);
        when(repo.save(any())).thenReturn(saved);

        ParcelDto result = service.create(req);

        assertThat(result.name()).isEqualTo("Sud");
        assertThat(result.id()).isNotNull();
    }
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
cd backend && mvn test -pl . -Dtest=ParcelServiceTest 2>&1 | tail -20
```

Expected: compilation error — classes not yet defined.

- [ ] **Step 3: Create Parcel.java**

```java
package ro.velicans.vineyard.parcel;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Parcel {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private String grapeVariety;
    private Integer areaSqM;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGrapeVariety() { return grapeVariety; }
    public void setGrapeVariety(String grapeVariety) { this.grapeVariety = grapeVariety; }
    public Integer getAreaSqM() { return areaSqM; }
    public void setAreaSqM(Integer areaSqM) { this.areaSqM = areaSqM; }
}
```

- [ ] **Step 4: Create ParcelRepository.java**

```java
package ro.velicans.vineyard.parcel;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ParcelRepository extends JpaRepository<Parcel, UUID> {}
```

- [ ] **Step 5: Create ParcelDto.java**

```java
package ro.velicans.vineyard.parcel;

import java.util.UUID;

public record ParcelDto(UUID id, String name, String grapeVariety, Integer areaSqM) {}
```

- [ ] **Step 6: Create ParcelService.java**

```java
package ro.velicans.vineyard.parcel;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ParcelService {

    private final ParcelRepository repo;

    public ParcelService(ParcelRepository repo) { this.repo = repo; }

    public List<ParcelDto> findAll() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public ParcelDto findById(UUID id) {
        return repo.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new NoSuchElementException("Parcel not found: " + id));
    }

    public ParcelDto create(ParcelDto dto) {
        Parcel p = new Parcel();
        p.setName(dto.name());
        p.setGrapeVariety(dto.grapeVariety());
        p.setAreaSqM(dto.areaSqM());
        return toDto(repo.save(p));
    }

    private ParcelDto toDto(Parcel p) {
        return new ParcelDto(p.getId(), p.getName(), p.getGrapeVariety(), p.getAreaSqM());
    }
}
```

- [ ] **Step 7: Create ParcelController.java**

```java
package ro.velicans.vineyard.parcel;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parcels")
public class ParcelController {

    private final ParcelService service;

    public ParcelController(ParcelService service) { this.service = service; }

    @GetMapping
    public List<ParcelDto> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ParcelDto findById(@PathVariable UUID id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParcelDto create(@RequestBody ParcelDto dto) { return service.create(dto); }
}
```

- [ ] **Step 8: Run test — expect pass**

```bash
cd backend && mvn test -Dtest=ParcelServiceTest -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 9: Commit**

```bash
git add backend/src
git commit -m "feat: parcel module (entity, service, controller, tests)"
```

---

## Task 5: Batch module

**Files:**
- Create: `backend/src/main/java/ro/velicans/vineyard/batch/BatchStatus.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/batch/ProductionBatch.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/batch/ProductionBatchRepository.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/batch/ProductionBatchDto.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/batch/ProductionBatchService.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/batch/ProductionBatchController.java`
- Create: `backend/src/test/java/ro/velicans/vineyard/batch/ProductionBatchServiceTest.java`

- [ ] **Step 1: Write failing test**

```java
// backend/src/test/java/ro/velicans/vineyard/batch/ProductionBatchServiceTest.java
package ro.velicans.vineyard.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.velicans.vineyard.parcel.Parcel;
import ro.velicans.vineyard.parcel.ParcelRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductionBatchServiceTest {

    @Mock ProductionBatchRepository batchRepo;
    @Mock ParcelRepository parcelRepo;
    @InjectMocks ProductionBatchService service;

    @Test
    void create_linksBatchToParcel() {
        UUID parcelId = UUID.randomUUID();
        Parcel parcel = new Parcel();
        parcel.setId(parcelId);
        parcel.setName("Nord");
        parcel.setGrapeVariety("Feteasca");
        parcel.setAreaSqM(1000);
        when(parcelRepo.findById(parcelId)).thenReturn(Optional.of(parcel));

        ProductionBatch saved = new ProductionBatch();
        saved.setId(UUID.randomUUID());
        saved.setParcel(parcel);
        saved.setYear(2024);
        saved.setStatus(BatchStatus.HARVESTED);
        when(batchRepo.save(any())).thenReturn(saved);

        ProductionBatchDto result = service.create(new ProductionBatchDto(null, parcelId, "Nord", 2024, BatchStatus.HARVESTED));

        assertThat(result.parcelId()).isEqualTo(parcelId);
        assertThat(result.status()).isEqualTo(BatchStatus.HARVESTED);
    }

    @Test
    void create_throwsWhenParcelMissing() {
        UUID parcelId = UUID.randomUUID();
        when(parcelRepo.findById(parcelId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(new ProductionBatchDto(null, parcelId, null, 2024, null)))
            .isInstanceOf(NoSuchElementException.class);
    }
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
cd backend && mvn test -Dtest=ProductionBatchServiceTest 2>&1 | tail -10
```

- [ ] **Step 3: Create BatchStatus.java**

```java
package ro.velicans.vineyard.batch;

public enum BatchStatus { HARVESTED, PRESSED, BOTTLED }
```

- [ ] **Step 4: Create ProductionBatch.java**

```java
package ro.velicans.vineyard.batch;

import jakarta.persistence.*;
import ro.velicans.vineyard.parcel.Parcel;
import java.util.UUID;

@Entity
public class ProductionBatch {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcel_id")
    private Parcel parcel;

    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "batch_status")
    private BatchStatus status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Parcel getParcel() { return parcel; }
    public void setParcel(Parcel parcel) { this.parcel = parcel; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }
}
```

- [ ] **Step 5: Create ProductionBatchRepository.java**

```java
package ro.velicans.vineyard.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, UUID> {
    List<ProductionBatch> findByParcelId(UUID parcelId);
    List<ProductionBatch> findByYear(Integer year);
    List<ProductionBatch> findByParcelIdAndYear(UUID parcelId, Integer year);
}
```

- [ ] **Step 6: Create ProductionBatchDto.java**

```java
package ro.velicans.vineyard.batch;

import java.util.UUID;

public record ProductionBatchDto(UUID id, UUID parcelId, String parcelName, Integer year, BatchStatus status) {}
```

- [ ] **Step 7: Create ProductionBatchService.java**

```java
package ro.velicans.vineyard.batch;

import org.springframework.stereotype.Service;
import ro.velicans.vineyard.parcel.ParcelRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ProductionBatchService {

    private final ProductionBatchRepository batchRepo;
    private final ParcelRepository parcelRepo;

    public ProductionBatchService(ProductionBatchRepository batchRepo, ParcelRepository parcelRepo) {
        this.batchRepo = batchRepo;
        this.parcelRepo = parcelRepo;
    }

    public List<ProductionBatchDto> findAll() {
        return batchRepo.findAll().stream().map(this::toDto).toList();
    }

    public ProductionBatchDto findById(UUID id) {
        return batchRepo.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new NoSuchElementException("Batch not found: " + id));
    }

    public ProductionBatch findEntityById(UUID id) {
        return batchRepo.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Batch not found: " + id));
    }

    public ProductionBatchDto create(ProductionBatchDto dto) {
        var parcel = parcelRepo.findById(dto.parcelId())
            .orElseThrow(() -> new NoSuchElementException("Parcel not found: " + dto.parcelId()));
        ProductionBatch batch = new ProductionBatch();
        batch.setParcel(parcel);
        batch.setYear(dto.year());
        batch.setStatus(BatchStatus.HARVESTED);
        return toDto(batchRepo.save(batch));
    }

    public void updateStatus(UUID batchId, BatchStatus status) {
        ProductionBatch batch = findEntityById(batchId);
        batch.setStatus(status);
        batchRepo.save(batch);
    }

    private ProductionBatchDto toDto(ProductionBatch b) {
        return new ProductionBatchDto(b.getId(), b.getParcel().getId(), b.getParcel().getName(), b.getYear(), b.getStatus());
    }
}
```

- [ ] **Step 8: Create ProductionBatchController.java**

```java
package ro.velicans.vineyard.batch;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches")
public class ProductionBatchController {

    private final ProductionBatchService service;

    public ProductionBatchController(ProductionBatchService service) { this.service = service; }

    @GetMapping
    public List<ProductionBatchDto> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ProductionBatchDto findById(@PathVariable UUID id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductionBatchDto create(@RequestBody ProductionBatchDto dto) { return service.create(dto); }
}
```

- [ ] **Step 9: Run tests**

```bash
cd backend && mvn test -Dtest=ProductionBatchServiceTest -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 10: Commit**

```bash
git add backend/src
git commit -m "feat: production batch module"
```

---

## Task 6: Harvest module

**Files:**
- Create: `backend/src/main/java/ro/velicans/vineyard/harvest/Harvest.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/harvest/HarvestRepository.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/harvest/HarvestDto.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/harvest/HarvestService.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/harvest/HarvestController.java`
- Create: `backend/src/test/java/ro/velicans/vineyard/harvest/HarvestServiceTest.java`

- [ ] **Step 1: Write failing test**

```java
// backend/src/test/java/ro/velicans/vineyard/harvest/HarvestServiceTest.java
package ro.velicans.vineyard.harvest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.velicans.vineyard.batch.BatchStatus;
import ro.velicans.vineyard.batch.ProductionBatch;
import ro.velicans.vineyard.batch.ProductionBatchService;
import ro.velicans.vineyard.parcel.Parcel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestServiceTest {

    @Mock HarvestRepository repo;
    @Mock ProductionBatchService batchService;
    @InjectMocks HarvestService service;

    private ProductionBatch batchWithStatus(BatchStatus status) {
        Parcel p = new Parcel();
        p.setId(UUID.randomUUID());
        p.setName("Nord");
        p.setGrapeVariety("Feteasca");
        p.setAreaSqM(1000);
        ProductionBatch b = new ProductionBatch();
        b.setId(UUID.randomUUID());
        b.setParcel(p);
        b.setYear(2024);
        b.setStatus(status);
        return b;
    }

    @Test
    void record_savesHarvestAndAdvancesStatus() {
        ProductionBatch batch = batchWithStatus(BatchStatus.HARVESTED);
        UUID batchId = batch.getId();
        when(batchService.findEntityById(batchId)).thenReturn(batch);

        Harvest saved = new Harvest();
        saved.setId(UUID.randomUUID());
        saved.setBatch(batch);
        saved.setDate(LocalDate.of(2024, 9, 10));
        saved.setQuantityKg(new BigDecimal("500.00"));
        when(repo.save(any())).thenReturn(saved);

        HarvestDto req = new HarvestDto(null, batchId, LocalDate.of(2024, 9, 10), new BigDecimal("500.00"));
        HarvestDto result = service.record(batchId, req);

        assertThat(result.quantityKg()).isEqualByComparingTo("500.00");
        verify(batchService).updateStatus(batchId, BatchStatus.HARVESTED);
    }

    @Test
    void record_throwsWhenHarvestAlreadyExists() {
        ProductionBatch batch = batchWithStatus(BatchStatus.PRESSED);
        UUID batchId = batch.getId();
        when(batchService.findEntityById(batchId)).thenReturn(batch);
        when(repo.findByBatchId(batchId)).thenReturn(Optional.of(new Harvest()));

        HarvestDto req = new HarvestDto(null, batchId, LocalDate.now(), BigDecimal.TEN);
        assertThatThrownBy(() -> service.record(batchId, req))
            .isInstanceOf(IllegalStateException.class);
    }
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
cd backend && mvn test -Dtest=HarvestServiceTest 2>&1 | tail -10
```

- [ ] **Step 3: Create Harvest.java**

```java
package ro.velicans.vineyard.harvest;

import jakarta.persistence.*;
import ro.velicans.vineyard.batch.ProductionBatch;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Harvest {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch batch;

    private LocalDate date;
    private BigDecimal quantityKg;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ProductionBatch getBatch() { return batch; }
    public void setBatch(ProductionBatch batch) { this.batch = batch; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getQuantityKg() { return quantityKg; }
    public void setQuantityKg(BigDecimal quantityKg) { this.quantityKg = quantityKg; }
}
```

- [ ] **Step 4: Create HarvestRepository.java**

```java
package ro.velicans.vineyard.harvest;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface HarvestRepository extends JpaRepository<Harvest, UUID> {
    Optional<Harvest> findByBatchId(UUID batchId);
}
```

- [ ] **Step 5: Create HarvestDto.java**

```java
package ro.velicans.vineyard.harvest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record HarvestDto(UUID id, UUID batchId, LocalDate date, BigDecimal quantityKg) {}
```

- [ ] **Step 6: Create HarvestService.java**

```java
package ro.velicans.vineyard.harvest;

import org.springframework.stereotype.Service;
import ro.velicans.vineyard.batch.BatchStatus;
import ro.velicans.vineyard.batch.ProductionBatch;
import ro.velicans.vineyard.batch.ProductionBatchService;

import java.util.UUID;

@Service
public class HarvestService {

    private final HarvestRepository repo;
    private final ProductionBatchService batchService;

    public HarvestService(HarvestRepository repo, ProductionBatchService batchService) {
        this.repo = repo;
        this.batchService = batchService;
    }

    public HarvestDto record(UUID batchId, HarvestDto dto) {
        ProductionBatch batch = batchService.findEntityById(batchId);
        if (repo.findByBatchId(batchId).isPresent()) {
            throw new IllegalStateException("Harvest already recorded for batch: " + batchId);
        }
        Harvest h = new Harvest();
        h.setBatch(batch);
        h.setDate(dto.date());
        h.setQuantityKg(dto.quantityKg());
        Harvest saved = repo.save(h);
        batchService.updateStatus(batchId, BatchStatus.HARVESTED);
        return toDto(saved);
    }

    public HarvestDto findByBatchId(UUID batchId) {
        return repo.findByBatchId(batchId).map(this::toDto).orElse(null);
    }

    private HarvestDto toDto(Harvest h) {
        return new HarvestDto(h.getId(), h.getBatch().getId(), h.getDate(), h.getQuantityKg());
    }
}
```

- [ ] **Step 7: Create HarvestController.java**

```java
package ro.velicans.vineyard.harvest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches/{batchId}/harvest")
public class HarvestController {

    private final HarvestService service;

    public HarvestController(HarvestService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HarvestDto record(@PathVariable UUID batchId, @RequestBody HarvestDto dto) {
        return service.record(batchId, dto);
    }

    @GetMapping
    public HarvestDto get(@PathVariable UUID batchId) {
        return service.findByBatchId(batchId);
    }
}
```

- [ ] **Step 8: Run test**

```bash
cd backend && mvn test -Dtest=HarvestServiceTest -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 9: Commit**

```bash
git add backend/src
git commit -m "feat: harvest module"
```

---

## Task 7: Pressing module

**Files:**
- Create: `backend/src/main/java/ro/velicans/vineyard/pressing/Pressing.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/pressing/PressingRepository.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/pressing/PressingDto.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/pressing/PressingService.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/pressing/PressingController.java`
- Create: `backend/src/test/java/ro/velicans/vineyard/pressing/PressingServiceTest.java`

- [ ] **Step 1: Write failing test**

```java
// backend/src/test/java/ro/velicans/vineyard/pressing/PressingServiceTest.java
package ro.velicans.vineyard.pressing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.velicans.vineyard.batch.BatchStatus;
import ro.velicans.vineyard.batch.ProductionBatch;
import ro.velicans.vineyard.batch.ProductionBatchService;
import ro.velicans.vineyard.harvest.Harvest;
import ro.velicans.vineyard.harvest.HarvestRepository;
import ro.velicans.vineyard.parcel.Parcel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PressingServiceTest {

    @Mock PressingRepository repo;
    @Mock ProductionBatchService batchService;
    @Mock HarvestRepository harvestRepo;
    @InjectMocks PressingService service;

    private ProductionBatch harvestedBatch() {
        Parcel p = new Parcel();
        p.setId(UUID.randomUUID());
        p.setName("Nord");
        p.setGrapeVariety("Feteasca");
        p.setAreaSqM(1000);
        ProductionBatch b = new ProductionBatch();
        b.setId(UUID.randomUUID());
        b.setParcel(p);
        b.setYear(2024);
        b.setStatus(BatchStatus.HARVESTED);
        return b;
    }

    @Test
    void record_calculatesYieldRatio() {
        ProductionBatch batch = harvestedBatch();
        UUID batchId = batch.getId();
        when(batchService.findEntityById(batchId)).thenReturn(batch);

        Harvest harvest = new Harvest();
        harvest.setQuantityKg(new BigDecimal("500.00"));
        when(harvestRepo.findByBatchId(batchId)).thenReturn(Optional.of(harvest));

        Pressing saved = new Pressing();
        saved.setId(UUID.randomUUID());
        saved.setBatch(batch);
        saved.setDate(LocalDate.of(2024, 9, 11));
        saved.setMustLiters(new BigDecimal("350.00"));
        saved.setYieldRatio(new BigDecimal("0.7000"));
        when(repo.save(any())).thenReturn(saved);

        PressingDto req = new PressingDto(null, batchId, LocalDate.of(2024, 9, 11), new BigDecimal("350.00"), null);
        PressingDto result = service.record(batchId, req);

        assertThat(result.yieldRatio()).isEqualByComparingTo("0.7000");
        verify(batchService).updateStatus(batchId, BatchStatus.PRESSED);
    }

    @Test
    void record_throwsWhenNoHarvest() {
        ProductionBatch batch = harvestedBatch();
        UUID batchId = batch.getId();
        when(batchService.findEntityById(batchId)).thenReturn(batch);
        when(harvestRepo.findByBatchId(batchId)).thenReturn(Optional.empty());

        PressingDto req = new PressingDto(null, batchId, LocalDate.now(), BigDecimal.TEN, null);
        assertThatThrownBy(() -> service.record(batchId, req))
            .isInstanceOf(IllegalStateException.class);
    }
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
cd backend && mvn test -Dtest=PressingServiceTest 2>&1 | tail -10
```

- [ ] **Step 3: Create Pressing.java**

```java
package ro.velicans.vineyard.pressing;

import jakarta.persistence.*;
import ro.velicans.vineyard.batch.ProductionBatch;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Pressing {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch batch;

    private LocalDate date;
    private BigDecimal mustLiters;
    private BigDecimal yieldRatio;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ProductionBatch getBatch() { return batch; }
    public void setBatch(ProductionBatch batch) { this.batch = batch; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getMustLiters() { return mustLiters; }
    public void setMustLiters(BigDecimal mustLiters) { this.mustLiters = mustLiters; }
    public BigDecimal getYieldRatio() { return yieldRatio; }
    public void setYieldRatio(BigDecimal yieldRatio) { this.yieldRatio = yieldRatio; }
}
```

- [ ] **Step 4: Create PressingRepository.java**

```java
package ro.velicans.vineyard.pressing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PressingRepository extends JpaRepository<Pressing, UUID> {
    Optional<Pressing> findByBatchId(UUID batchId);
}
```

- [ ] **Step 5: Create PressingDto.java**

```java
package ro.velicans.vineyard.pressing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PressingDto(UUID id, UUID batchId, LocalDate date, BigDecimal mustLiters, BigDecimal yieldRatio) {}
```

- [ ] **Step 6: Create PressingService.java**

```java
package ro.velicans.vineyard.pressing;

import org.springframework.stereotype.Service;
import ro.velicans.vineyard.batch.BatchStatus;
import ro.velicans.vineyard.batch.ProductionBatch;
import ro.velicans.vineyard.batch.ProductionBatchService;
import ro.velicans.vineyard.harvest.Harvest;
import ro.velicans.vineyard.harvest.HarvestRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class PressingService {

    private final PressingRepository repo;
    private final ProductionBatchService batchService;
    private final HarvestRepository harvestRepo;

    public PressingService(PressingRepository repo, ProductionBatchService batchService, HarvestRepository harvestRepo) {
        this.repo = repo;
        this.batchService = batchService;
        this.harvestRepo = harvestRepo;
    }

    public PressingDto record(UUID batchId, PressingDto dto) {
        ProductionBatch batch = batchService.findEntityById(batchId);
        Harvest harvest = harvestRepo.findByBatchId(batchId)
            .orElseThrow(() -> new IllegalStateException("No harvest recorded for batch: " + batchId));
        if (repo.findByBatchId(batchId).isPresent()) {
            throw new IllegalStateException("Pressing already recorded for batch: " + batchId);
        }
        BigDecimal yieldRatio = dto.mustLiters().divide(harvest.getQuantityKg(), 4, RoundingMode.HALF_UP);
        Pressing p = new Pressing();
        p.setBatch(batch);
        p.setDate(dto.date());
        p.setMustLiters(dto.mustLiters());
        p.setYieldRatio(yieldRatio);
        Pressing saved = repo.save(p);
        batchService.updateStatus(batchId, BatchStatus.PRESSED);
        return toDto(saved);
    }

    public PressingDto findByBatchId(UUID batchId) {
        return repo.findByBatchId(batchId).map(this::toDto).orElse(null);
    }

    private PressingDto toDto(Pressing p) {
        return new PressingDto(p.getId(), p.getBatch().getId(), p.getDate(), p.getMustLiters(), p.getYieldRatio());
    }
}
```

- [ ] **Step 7: Create PressingController.java**

```java
package ro.velicans.vineyard.pressing;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches/{batchId}/pressing")
public class PressingController {

    private final PressingService service;

    public PressingController(PressingService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PressingDto record(@PathVariable UUID batchId, @RequestBody PressingDto dto) {
        return service.record(batchId, dto);
    }

    @GetMapping
    public PressingDto get(@PathVariable UUID batchId) {
        return service.findByBatchId(batchId);
    }
}
```

- [ ] **Step 8: Run tests**

```bash
cd backend && mvn test -Dtest=PressingServiceTest -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 9: Commit**

```bash
git add backend/src
git commit -m "feat: pressing module with yield ratio calculation"
```

---

## Task 8: Bottling module

**Files:**
- Create: `backend/src/main/java/ro/velicans/vineyard/bottling/BottleVolume.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/bottling/Bottling.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/bottling/BottlingRepository.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/bottling/BottlingDto.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/bottling/BottlingService.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/bottling/BottlingController.java`
- Create: `backend/src/test/java/ro/velicans/vineyard/bottling/BottlingServiceTest.java`

- [ ] **Step 1: Write failing test**

```java
// backend/src/test/java/ro/velicans/vineyard/bottling/BottlingServiceTest.java
package ro.velicans.vineyard.bottling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.velicans.vineyard.batch.BatchStatus;
import ro.velicans.vineyard.batch.ProductionBatch;
import ro.velicans.vineyard.batch.ProductionBatchService;
import ro.velicans.vineyard.parcel.Parcel;
import ro.velicans.vineyard.pressing.PressingRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BottlingServiceTest {

    @Mock BottlingRepository repo;
    @Mock ProductionBatchService batchService;
    @Mock PressingRepository pressingRepo;
    @InjectMocks BottlingService service;

    private ProductionBatch pressedBatch() {
        Parcel p = new Parcel();
        p.setId(UUID.randomUUID());
        p.setName("Nord");
        p.setGrapeVariety("Feteasca");
        p.setAreaSqM(1000);
        ProductionBatch b = new ProductionBatch();
        b.setId(UUID.randomUUID());
        b.setParcel(p);
        b.setYear(2024);
        b.setStatus(BatchStatus.PRESSED);
        return b;
    }

    @Test
    void record_savesBottlingAndAdvancesToBottled() {
        ProductionBatch batch = pressedBatch();
        UUID batchId = batch.getId();
        when(batchService.findEntityById(batchId)).thenReturn(batch);
        when(pressingRepo.findByBatchId(batchId)).thenReturn(Optional.of(new ro.velicans.vineyard.pressing.Pressing()));

        Bottling saved = new Bottling();
        saved.setId(UUID.randomUUID());
        saved.setBatch(batch);
        saved.setDate(LocalDate.of(2024, 11, 1));
        saved.setBottleCount(400);
        saved.setBottleVolume(BottleVolume.L075);
        when(repo.save(any())).thenReturn(saved);

        BottlingDto req = new BottlingDto(null, batchId, LocalDate.of(2024, 11, 1), 400, BottleVolume.L075);
        BottlingDto result = service.record(batchId, req);

        assertThat(result.bottleCount()).isEqualTo(400);
        assertThat(result.bottleVolume()).isEqualTo(BottleVolume.L075);
        verify(batchService).updateStatus(batchId, BatchStatus.BOTTLED);
    }

    @Test
    void record_throwsWhenNoPressing() {
        ProductionBatch batch = pressedBatch();
        UUID batchId = batch.getId();
        when(batchService.findEntityById(batchId)).thenReturn(batch);
        when(pressingRepo.findByBatchId(batchId)).thenReturn(Optional.empty());

        BottlingDto req = new BottlingDto(null, batchId, LocalDate.now(), 100, BottleVolume.L075);
        assertThatThrownBy(() -> service.record(batchId, req))
            .isInstanceOf(IllegalStateException.class);
    }
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
cd backend && mvn test -Dtest=BottlingServiceTest 2>&1 | tail -10
```

- [ ] **Step 3: Create BottleVolume.java**

```java
package ro.velicans.vineyard.bottling;

public enum BottleVolume { L075, L150 }
```

- [ ] **Step 4: Create Bottling.java**

```java
package ro.velicans.vineyard.bottling;

import jakarta.persistence.*;
import ro.velicans.vineyard.batch.ProductionBatch;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Bottling {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch batch;

    private LocalDate date;
    private Integer bottleCount;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "bottle_volume")
    private BottleVolume bottleVolume;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ProductionBatch getBatch() { return batch; }
    public void setBatch(ProductionBatch batch) { this.batch = batch; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Integer getBottleCount() { return bottleCount; }
    public void setBottleCount(Integer bottleCount) { this.bottleCount = bottleCount; }
    public BottleVolume getBottleVolume() { return bottleVolume; }
    public void setBottleVolume(BottleVolume bottleVolume) { this.bottleVolume = bottleVolume; }
}
```

- [ ] **Step 5: Create BottlingRepository.java**

```java
package ro.velicans.vineyard.bottling;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BottlingRepository extends JpaRepository<Bottling, UUID> {
    Optional<Bottling> findByBatchId(UUID batchId);
    List<Bottling> findByBottleVolume(BottleVolume volume);
}
```

- [ ] **Step 6: Create BottlingDto.java**

```java
package ro.velicans.vineyard.bottling;

import java.time.LocalDate;
import java.util.UUID;

public record BottlingDto(UUID id, UUID batchId, LocalDate date, Integer bottleCount, BottleVolume bottleVolume) {}
```

- [ ] **Step 7: Create BottlingService.java**

```java
package ro.velicans.vineyard.bottling;

import org.springframework.stereotype.Service;
import ro.velicans.vineyard.batch.BatchStatus;
import ro.velicans.vineyard.batch.ProductionBatch;
import ro.velicans.vineyard.batch.ProductionBatchService;
import ro.velicans.vineyard.pressing.PressingRepository;

import java.util.UUID;

@Service
public class BottlingService {

    private final BottlingRepository repo;
    private final ProductionBatchService batchService;
    private final PressingRepository pressingRepo;

    public BottlingService(BottlingRepository repo, ProductionBatchService batchService, PressingRepository pressingRepo) {
        this.repo = repo;
        this.batchService = batchService;
        this.pressingRepo = pressingRepo;
    }

    public BottlingDto record(UUID batchId, BottlingDto dto) {
        ProductionBatch batch = batchService.findEntityById(batchId);
        pressingRepo.findByBatchId(batchId)
            .orElseThrow(() -> new IllegalStateException("No pressing recorded for batch: " + batchId));
        if (repo.findByBatchId(batchId).isPresent()) {
            throw new IllegalStateException("Bottling already recorded for batch: " + batchId);
        }
        Bottling b = new Bottling();
        b.setBatch(batch);
        b.setDate(dto.date());
        b.setBottleCount(dto.bottleCount());
        b.setBottleVolume(dto.bottleVolume());
        Bottling saved = repo.save(b);
        batchService.updateStatus(batchId, BatchStatus.BOTTLED);
        return toDto(saved);
    }

    public BottlingDto findByBatchId(UUID batchId) {
        return repo.findByBatchId(batchId).map(this::toDto).orElse(null);
    }

    private BottlingDto toDto(Bottling b) {
        return new BottlingDto(b.getId(), b.getBatch().getId(), b.getDate(), b.getBottleCount(), b.getBottleVolume());
    }
}
```

- [ ] **Step 8: Create BottlingController.java**

```java
package ro.velicans.vineyard.bottling;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches/{batchId}/bottling")
public class BottlingController {

    private final BottlingService service;

    public BottlingController(BottlingService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BottlingDto record(@PathVariable UUID batchId, @RequestBody BottlingDto dto) {
        return service.record(batchId, dto);
    }

    @GetMapping
    public BottlingDto get(@PathVariable UUID batchId) {
        return service.findByBatchId(batchId);
    }
}
```

- [ ] **Step 9: Run tests**

```bash
cd backend && mvn test -Dtest=BottlingServiceTest -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 10: Commit**

```bash
git add backend/src
git commit -m "feat: bottling module"
```

---

## Task 9: Reports module

**Files:**
- Create: `backend/src/main/java/ro/velicans/vineyard/reports/HarvestReportDto.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/reports/PressingReportDto.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/reports/BottlingReportDto.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/reports/ReportService.java`
- Create: `backend/src/main/java/ro/velicans/vineyard/reports/ReportController.java`
- Create: `backend/src/test/java/ro/velicans/vineyard/reports/ReportServiceTest.java`

- [ ] **Step 1: Write failing test**

```java
// backend/src/test/java/ro/velicans/vineyard/reports/ReportServiceTest.java
package ro.velicans.vineyard.reports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.velicans.vineyard.batch.BatchStatus;
import ro.velicans.vineyard.batch.ProductionBatch;
import ro.velicans.vineyard.bottling.BottleVolume;
import ro.velicans.vineyard.bottling.Bottling;
import ro.velicans.vineyard.bottling.BottlingRepository;
import ro.velicans.vineyard.harvest.Harvest;
import ro.velicans.vineyard.harvest.HarvestRepository;
import ro.velicans.vineyard.parcel.Parcel;
import ro.velicans.vineyard.pressing.Pressing;
import ro.velicans.vineyard.pressing.PressingRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock HarvestRepository harvestRepo;
    @Mock PressingRepository pressingRepo;
    @Mock BottlingRepository bottlingRepo;
    @InjectMocks ReportService service;

    private ProductionBatch batchFor(String parcelName, int year) {
        Parcel p = new Parcel();
        p.setId(UUID.randomUUID());
        p.setName(parcelName);
        p.setGrapeVariety("Feteasca");
        p.setAreaSqM(1000);
        ProductionBatch b = new ProductionBatch();
        b.setId(UUID.randomUUID());
        b.setParcel(p);
        b.setYear(year);
        b.setStatus(BatchStatus.BOTTLED);
        return b;
    }

    @Test
    void harvestReport_aggregatesByParcelAndYear() {
        ProductionBatch b = batchFor("Nord", 2024);
        Harvest h = new Harvest();
        h.setBatch(b);
        h.setDate(LocalDate.of(2024, 9, 10));
        h.setQuantityKg(new BigDecimal("500.00"));
        when(harvestRepo.findAll()).thenReturn(List.of(h));

        List<HarvestReportDto> result = service.harvestReport(2024);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).totalKg()).isEqualByComparingTo("500.00");
        assertThat(result.get(0).parcelName()).isEqualTo("Nord");
    }

    @Test
    void bottlingReport_countsByVolume() {
        ProductionBatch b = batchFor("Nord", 2024);
        Bottling bt = new Bottling();
        bt.setBatch(b);
        bt.setDate(LocalDate.of(2024, 11, 1));
        bt.setBottleCount(400);
        bt.setBottleVolume(BottleVolume.L075);
        when(bottlingRepo.findAll()).thenReturn(List.of(bt));

        List<BottlingReportDto> result = service.bottlingReport(2024);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).totalBottles()).isEqualTo(400);
        assertThat(result.get(0).bottleVolume()).isEqualTo(BottleVolume.L075);
    }
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
cd backend && mvn test -Dtest=ReportServiceTest 2>&1 | tail -10
```

- [ ] **Step 3: Create report DTOs**

```java
// HarvestReportDto.java
package ro.velicans.vineyard.reports;
import java.math.BigDecimal;
import java.util.UUID;
public record HarvestReportDto(UUID parcelId, String parcelName, Integer year, BigDecimal totalKg, BigDecimal avgKg) {}
```

```java
// PressingReportDto.java
package ro.velicans.vineyard.reports;
import java.math.BigDecimal;
import java.util.UUID;
public record PressingReportDto(UUID parcelId, String parcelName, Integer year, BigDecimal totalMustLiters, BigDecimal avgYieldRatio) {}
```

```java
// BottlingReportDto.java
package ro.velicans.vineyard.reports;
import ro.velicans.vineyard.bottling.BottleVolume;
public record BottlingReportDto(Integer year, BottleVolume bottleVolume, Integer totalBottles) {}
```

- [ ] **Step 4: Create ReportService.java**

```java
package ro.velicans.vineyard.reports;

import org.springframework.stereotype.Service;
import ro.velicans.vineyard.bottling.BottleVolume;
import ro.velicans.vineyard.bottling.Bottling;
import ro.velicans.vineyard.bottling.BottlingRepository;
import ro.velicans.vineyard.harvest.Harvest;
import ro.velicans.vineyard.harvest.HarvestRepository;
import ro.velicans.vineyard.pressing.Pressing;
import ro.velicans.vineyard.pressing.PressingRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final HarvestRepository harvestRepo;
    private final PressingRepository pressingRepo;
    private final BottlingRepository bottlingRepo;

    public ReportService(HarvestRepository harvestRepo, PressingRepository pressingRepo, BottlingRepository bottlingRepo) {
        this.harvestRepo = harvestRepo;
        this.pressingRepo = pressingRepo;
        this.bottlingRepo = bottlingRepo;
    }

    public List<HarvestReportDto> harvestReport(Integer year) {
        return harvestRepo.findAll().stream()
            .filter(h -> year == null || h.getBatch().getYear().equals(year))
            .collect(Collectors.groupingBy(h -> h.getBatch().getParcel()))
            .entrySet().stream()
            .map(e -> {
                var parcel = e.getKey();
                var harvests = e.getValue();
                BigDecimal total = harvests.stream().map(Harvest::getQuantityKg).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avg = total.divide(BigDecimal.valueOf(harvests.size()), 2, RoundingMode.HALF_UP);
                return new HarvestReportDto(parcel.getId(), parcel.getName(), year, total, avg);
            })
            .toList();
    }

    public List<PressingReportDto> pressingReport(Integer year) {
        return pressingRepo.findAll().stream()
            .filter(p -> year == null || p.getBatch().getYear().equals(year))
            .collect(Collectors.groupingBy(p -> p.getBatch().getParcel()))
            .entrySet().stream()
            .map(e -> {
                var parcel = e.getKey();
                var pressings = e.getValue();
                BigDecimal totalLiters = pressings.stream().map(Pressing::getMustLiters).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avgYield = pressings.stream().map(Pressing::getYieldRatio).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(pressings.size()), 4, RoundingMode.HALF_UP);
                return new PressingReportDto(parcel.getId(), parcel.getName(), year, totalLiters, avgYield);
            })
            .toList();
    }

    public List<BottlingReportDto> bottlingReport(Integer year) {
        return bottlingRepo.findAll().stream()
            .filter(b -> year == null || b.getBatch().getYear().equals(year))
            .collect(Collectors.groupingBy(Bottling::getBottleVolume))
            .entrySet().stream()
            .map(e -> {
                BottleVolume vol = e.getKey();
                int total = e.getValue().stream().mapToInt(Bottling::getBottleCount).sum();
                return new BottlingReportDto(year, vol, total);
            })
            .toList();
    }
}
```

- [ ] **Step 5: Create ReportController.java**

```java
package ro.velicans.vineyard.reports;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) { this.service = service; }

    @GetMapping("/harvest")
    public List<HarvestReportDto> harvest(@RequestParam(required = false) Integer year) {
        return service.harvestReport(year);
    }

    @GetMapping("/pressing")
    public List<PressingReportDto> pressing(@RequestParam(required = false) Integer year) {
        return service.pressingReport(year);
    }

    @GetMapping("/bottling")
    public List<BottlingReportDto> bottling(@RequestParam(required = false) Integer year) {
        return service.bottlingReport(year);
    }
}
```

- [ ] **Step 6: Run all backend tests**

```bash
cd backend && mvn test -q
```

Expected: `BUILD SUCCESS`, all tests green.

- [ ] **Step 7: Commit**

```bash
git add backend/src
git commit -m "feat: reports module (harvest, pressing, bottling)"
```

---

## Task 10: Backend Dockerfile

**Files:**
- Create: `backend/Dockerfile`

- [ ] **Step 1: Create backend/Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven && mvn package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 2: Verify Dockerfile syntax**

```bash
docker build -t vineyard-api-test backend/ --no-cache 2>&1 | tail -5
```

Expected: `Successfully built ...`

- [ ] **Step 3: Commit**

```bash
git add backend/Dockerfile
git commit -m "chore: backend Dockerfile"
```

---

## Task 11: Frontend scaffold

**Files:**
- Create: `frontend/index.html`
- Create: `frontend/src/main.jsx`
- Create: `frontend/src/App.jsx`
- Create: `frontend/src/api/client.js`
- Create: `frontend/src/api/parcels.js`
- Create: `frontend/src/api/batches.js`
- Create: `frontend/src/api/reports.js`
- Create: `frontend/src/components/Layout.jsx`
- Create: `frontend/src/components/NavBar.jsx`

- [ ] **Step 1: Install frontend dependencies**

```bash
cd frontend && npm install
```

- [ ] **Step 2: Create frontend/index.html**

```html
<!doctype html>
<html lang="ro">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Vineyard Manager</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
```

- [ ] **Step 3: Create frontend/src/main.jsx**

```jsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App'
import './index.css'

const queryClient = new QueryClient()

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </QueryClientProvider>
  </React.StrictMode>
)
```

- [ ] **Step 4: Create frontend/src/index.css**

```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

- [ ] **Step 5: Create frontend/src/api/client.js**

```js
import axios from 'axios'

const client = axios.create({ baseURL: '/api' })

client.interceptors.response.use(
  res => res,
  err => {
    const message = err.response?.data?.message || err.message
    return Promise.reject(new Error(message))
  }
)

export default client
```

- [ ] **Step 6: Create frontend/src/api/parcels.js**

```js
import client from './client'

export const getParcels = () => client.get('/parcels').then(r => r.data)
export const getParcel = (id) => client.get(`/parcels/${id}`).then(r => r.data)
export const createParcel = (data) => client.post('/parcels', data).then(r => r.data)
```

- [ ] **Step 7: Create frontend/src/api/batches.js**

```js
import client from './client'

export const getBatches = () => client.get('/batches').then(r => r.data)
export const getBatch = (id) => client.get(`/batches/${id}`).then(r => r.data)
export const createBatch = (data) => client.post('/batches', data).then(r => r.data)
export const recordHarvest = (batchId, data) => client.post(`/batches/${batchId}/harvest`, data).then(r => r.data)
export const recordPressing = (batchId, data) => client.post(`/batches/${batchId}/pressing`, data).then(r => r.data)
export const recordBottling = (batchId, data) => client.post(`/batches/${batchId}/bottling`, data).then(r => r.data)
export const getHarvest = (batchId) => client.get(`/batches/${batchId}/harvest`).then(r => r.data)
export const getPressing = (batchId) => client.get(`/batches/${batchId}/pressing`).then(r => r.data)
export const getBottling = (batchId) => client.get(`/batches/${batchId}/bottling`).then(r => r.data)
```

- [ ] **Step 8: Create frontend/src/api/reports.js**

```js
import client from './client'

export const getHarvestReport = (year) => client.get('/reports/harvest', { params: { year } }).then(r => r.data)
export const getPressingReport = (year) => client.get('/reports/pressing', { params: { year } }).then(r => r.data)
export const getBottlingReport = (year) => client.get('/reports/bottling', { params: { year } }).then(r => r.data)
```

- [ ] **Step 9: Create frontend/src/components/NavBar.jsx**

```jsx
import { Link, NavLink } from 'react-router-dom'

export default function NavBar() {
  const linkClass = ({ isActive }) =>
    `px-3 py-2 rounded text-sm font-medium ${isActive ? 'bg-green-700 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'}`

  return (
    <nav className="bg-gray-800 px-4 py-3 flex items-center gap-4">
      <Link to="/" className="text-white font-bold text-lg mr-4">Vineyard</Link>
      <NavLink to="/parcels" className={linkClass}>Parcele</NavLink>
      <NavLink to="/batches" className={linkClass}>Loturi</NavLink>
      <NavLink to="/reports/harvest" className={linkClass}>Raport Cules</NavLink>
      <NavLink to="/reports/pressing" className={linkClass}>Raport Stors</NavLink>
      <NavLink to="/reports/bottling" className={linkClass}>Raport Imbuteliere</NavLink>
    </nav>
  )
}
```

- [ ] **Step 10: Create frontend/src/components/Layout.jsx**

```jsx
import NavBar from './NavBar'

export default function Layout({ children }) {
  return (
    <div className="min-h-screen bg-gray-50">
      <NavBar />
      <main className="max-w-5xl mx-auto px-4 py-8">{children}</main>
    </div>
  )
}
```

- [ ] **Step 11: Commit**

```bash
git add frontend/
git commit -m "feat: frontend scaffold, API client, layout"
```

---

## Task 12: Frontend pages

**Files:**
- Create: `frontend/src/App.jsx`
- Create: `frontend/src/pages/Dashboard.jsx`
- Create: `frontend/src/pages/ParcelList.jsx`
- Create: `frontend/src/pages/BatchList.jsx`
- Create: `frontend/src/pages/BatchNew.jsx`
- Create: `frontend/src/pages/BatchDetail.jsx`
- Create: `frontend/src/components/HarvestForm.jsx`
- Create: `frontend/src/components/PressingForm.jsx`
- Create: `frontend/src/components/BottlingForm.jsx`
- Create: `frontend/src/components/StageTimeline.jsx`

- [ ] **Step 1: Create App.jsx**

```jsx
import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import ParcelList from './pages/ParcelList'
import BatchList from './pages/BatchList'
import BatchNew from './pages/BatchNew'
import BatchDetail from './pages/BatchDetail'
import ReportHarvest from './pages/ReportHarvest'
import ReportPressing from './pages/ReportPressing'
import ReportBottling from './pages/ReportBottling'

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/parcels" element={<ParcelList />} />
        <Route path="/batches" element={<BatchList />} />
        <Route path="/batches/new" element={<BatchNew />} />
        <Route path="/batches/:id" element={<BatchDetail />} />
        <Route path="/reports/harvest" element={<ReportHarvest />} />
        <Route path="/reports/pressing" element={<ReportPressing />} />
        <Route path="/reports/bottling" element={<ReportBottling />} />
      </Routes>
    </Layout>
  )
}
```

- [ ] **Step 2: Create Dashboard.jsx**

```jsx
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getBatches } from '../api/batches'
import { getParcels } from '../api/parcels'

export default function Dashboard() {
  const { data: batches = [] } = useQuery({ queryKey: ['batches'], queryFn: getBatches })
  const { data: parcels = [] } = useQuery({ queryKey: ['parcels'], queryFn: getParcels })

  const recent = batches.slice(-5).reverse()

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Dashboard</h1>
      <div className="grid grid-cols-3 gap-4 mb-8">
        <div className="bg-white rounded-lg p-4 shadow">
          <div className="text-3xl font-bold text-green-600">{parcels.length}</div>
          <div className="text-gray-500">Parcele</div>
        </div>
        <div className="bg-white rounded-lg p-4 shadow">
          <div className="text-3xl font-bold text-blue-600">{batches.length}</div>
          <div className="text-gray-500">Loturi totale</div>
        </div>
        <div className="bg-white rounded-lg p-4 shadow">
          <div className="text-3xl font-bold text-purple-600">
            {batches.filter(b => b.status === 'BOTTLED').length}
          </div>
          <div className="text-gray-500">Loturi finalizate</div>
        </div>
      </div>
      <h2 className="text-lg font-semibold mb-3">Ultimele loturi</h2>
      <div className="bg-white rounded-lg shadow divide-y">
        {recent.map(b => (
          <Link key={b.id} to={`/batches/${b.id}`} className="flex items-center justify-between px-4 py-3 hover:bg-gray-50">
            <span>{b.parcelName} — {b.year}</span>
            <span className={`text-xs px-2 py-1 rounded-full ${statusColor(b.status)}`}>{b.status}</span>
          </Link>
        ))}
        {recent.length === 0 && <p className="px-4 py-3 text-gray-400">Niciun lot înregistrat.</p>}
      </div>
      <Link to="/batches/new" className="mt-6 inline-block bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
        + Lot nou
      </Link>
    </div>
  )
}

function statusColor(status) {
  if (status === 'BOTTLED') return 'bg-purple-100 text-purple-700'
  if (status === 'PRESSED') return 'bg-blue-100 text-blue-700'
  return 'bg-yellow-100 text-yellow-700'
}
```

- [ ] **Step 3: Create ParcelList.jsx**

```jsx
import { useQuery } from '@tanstack/react-query'
import { getParcels } from '../api/parcels'

export default function ParcelList() {
  const { data: parcels = [], isLoading } = useQuery({ queryKey: ['parcels'], queryFn: getParcels })

  if (isLoading) return <p>Se încarcă...</p>

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Parcele</h1>
      <div className="grid gap-4">
        {parcels.map(p => (
          <div key={p.id} className="bg-white rounded-lg shadow p-4">
            <div className="font-semibold text-lg">{p.name}</div>
            <div className="text-gray-500">{p.grapeVariety}</div>
            <div className="text-sm text-gray-400">{p.areaSqM} m²</div>
          </div>
        ))}
      </div>
    </div>
  )
}
```

- [ ] **Step 4: Create BatchList.jsx**

```jsx
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { useState } from 'react'
import { getBatches } from '../api/batches'

export default function BatchList() {
  const { data: batches = [], isLoading } = useQuery({ queryKey: ['batches'], queryFn: getBatches })
  const [yearFilter, setYearFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')

  const filtered = batches
    .filter(b => !yearFilter || b.year === parseInt(yearFilter))
    .filter(b => !statusFilter || b.status === statusFilter)

  if (isLoading) return <p>Se încarcă...</p>

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Loturi</h1>
        <Link to="/batches/new" className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">+ Lot nou</Link>
      </div>
      <div className="flex gap-3 mb-4">
        <input
          type="number" placeholder="An" value={yearFilter}
          onChange={e => setYearFilter(e.target.value)}
          className="border rounded px-3 py-1 w-24"
        />
        <select value={statusFilter} onChange={e => setStatusFilter(e.target.value)} className="border rounded px-3 py-1">
          <option value="">Toate statusurile</option>
          <option value="HARVESTED">HARVESTED</option>
          <option value="PRESSED">PRESSED</option>
          <option value="BOTTLED">BOTTLED</option>
        </select>
      </div>
      <div className="bg-white rounded-lg shadow divide-y">
        {filtered.map(b => (
          <Link key={b.id} to={`/batches/${b.id}`} className="flex items-center justify-between px-4 py-3 hover:bg-gray-50">
            <span className="font-medium">{b.parcelName}</span>
            <span className="text-gray-500">{b.year}</span>
            <span className={`text-xs px-2 py-1 rounded-full ${statusColor(b.status)}`}>{b.status}</span>
          </Link>
        ))}
        {filtered.length === 0 && <p className="px-4 py-3 text-gray-400">Niciun lot găsit.</p>}
      </div>
    </div>
  )
}

function statusColor(status) {
  if (status === 'BOTTLED') return 'bg-purple-100 text-purple-700'
  if (status === 'PRESSED') return 'bg-blue-100 text-blue-700'
  return 'bg-yellow-100 text-yellow-700'
}
```

- [ ] **Step 5: Create BatchNew.jsx**

```jsx
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getParcels } from '../api/parcels'
import { createBatch } from '../api/batches'

export default function BatchNew() {
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { data: parcels = [] } = useQuery({ queryKey: ['parcels'], queryFn: getParcels })
  const [parcelId, setParcelId] = useState('')
  const [year, setYear] = useState(new Date().getFullYear())
  const [error, setError] = useState(null)

  const mutation = useMutation({
    mutationFn: createBatch,
    onSuccess: (batch) => { qc.invalidateQueries(['batches']); navigate(`/batches/${batch.id}`) },
    onError: (err) => setError(err.message),
  })

  const submit = (e) => {
    e.preventDefault()
    if (!parcelId) { setError('Selectează o parcelă'); return }
    mutation.mutate({ parcelId, year: parseInt(year) })
  }

  return (
    <div className="max-w-md">
      <h1 className="text-2xl font-bold mb-6">Lot nou</h1>
      <form onSubmit={submit} className="bg-white rounded-lg shadow p-6 space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1">Parcelă</label>
          <select value={parcelId} onChange={e => setParcelId(e.target.value)} className="w-full border rounded px-3 py-2">
            <option value="">-- Selectează --</option>
            {parcels.map(p => <option key={p.id} value={p.id}>{p.name} ({p.grapeVariety})</option>)}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">An</label>
          <input type="number" value={year} onChange={e => setYear(e.target.value)} className="w-full border rounded px-3 py-2" />
        </div>
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <button type="submit" className="w-full bg-green-600 text-white py-2 rounded hover:bg-green-700" disabled={mutation.isPending}>
          {mutation.isPending ? 'Se creează...' : 'Creează lot'}
        </button>
      </form>
    </div>
  )
}
```

- [ ] **Step 6: Create HarvestForm.jsx**

```jsx
import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { recordHarvest } from '../api/batches'

export default function HarvestForm({ batchId }) {
  const qc = useQueryClient()
  const [date, setDate] = useState('')
  const [quantityKg, setQuantityKg] = useState('')
  const [error, setError] = useState(null)

  const mutation = useMutation({
    mutationFn: (data) => recordHarvest(batchId, data),
    onSuccess: () => qc.invalidateQueries(['batch', batchId]),
    onError: (err) => setError(err.message),
  })

  const submit = (e) => {
    e.preventDefault()
    mutation.mutate({ date, quantityKg: parseFloat(quantityKg) })
  }

  return (
    <form onSubmit={submit} className="space-y-3">
      <h3 className="font-semibold text-lg">Înregistrează Cules</h3>
      <div>
        <label className="block text-sm font-medium mb-1">Data</label>
        <input type="date" value={date} onChange={e => setDate(e.target.value)} required className="border rounded px-3 py-2 w-full" />
      </div>
      <div>
        <label className="block text-sm font-medium mb-1">Cantitate (kg)</label>
        <input type="number" step="0.01" value={quantityKg} onChange={e => setQuantityKg(e.target.value)} required className="border rounded px-3 py-2 w-full" />
      </div>
      {error && <p className="text-red-600 text-sm">{error}</p>}
      <button type="submit" className="bg-yellow-500 text-white px-4 py-2 rounded hover:bg-yellow-600" disabled={mutation.isPending}>
        {mutation.isPending ? 'Se salvează...' : 'Salvează'}
      </button>
    </form>
  )
}
```

- [ ] **Step 7: Create PressingForm.jsx**

```jsx
import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { recordPressing } from '../api/batches'

export default function PressingForm({ batchId }) {
  const qc = useQueryClient()
  const [date, setDate] = useState('')
  const [mustLiters, setMustLiters] = useState('')
  const [error, setError] = useState(null)

  const mutation = useMutation({
    mutationFn: (data) => recordPressing(batchId, data),
    onSuccess: () => qc.invalidateQueries(['batch', batchId]),
    onError: (err) => setError(err.message),
  })

  const submit = (e) => {
    e.preventDefault()
    mutation.mutate({ date, mustLiters: parseFloat(mustLiters) })
  }

  return (
    <form onSubmit={submit} className="space-y-3">
      <h3 className="font-semibold text-lg">Înregistrează Stors</h3>
      <div>
        <label className="block text-sm font-medium mb-1">Data</label>
        <input type="date" value={date} onChange={e => setDate(e.target.value)} required className="border rounded px-3 py-2 w-full" />
      </div>
      <div>
        <label className="block text-sm font-medium mb-1">Must obținut (litri)</label>
        <input type="number" step="0.01" value={mustLiters} onChange={e => setMustLiters(e.target.value)} required className="border rounded px-3 py-2 w-full" />
      </div>
      {error && <p className="text-red-600 text-sm">{error}</p>}
      <button type="submit" className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600" disabled={mutation.isPending}>
        {mutation.isPending ? 'Se salvează...' : 'Salvează'}
      </button>
    </form>
  )
}
```

- [ ] **Step 8: Create BottlingForm.jsx**

```jsx
import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { recordBottling } from '../api/batches'

export default function BottlingForm({ batchId }) {
  const qc = useQueryClient()
  const [date, setDate] = useState('')
  const [bottleCount, setBottleCount] = useState('')
  const [bottleVolume, setBottleVolume] = useState('L075')
  const [error, setError] = useState(null)

  const mutation = useMutation({
    mutationFn: (data) => recordBottling(batchId, data),
    onSuccess: () => qc.invalidateQueries(['batch', batchId]),
    onError: (err) => setError(err.message),
  })

  const submit = (e) => {
    e.preventDefault()
    mutation.mutate({ date, bottleCount: parseInt(bottleCount), bottleVolume })
  }

  return (
    <form onSubmit={submit} className="space-y-3">
      <h3 className="font-semibold text-lg">Înregistrează Îmbuteliere</h3>
      <div>
        <label className="block text-sm font-medium mb-1">Data</label>
        <input type="date" value={date} onChange={e => setDate(e.target.value)} required className="border rounded px-3 py-2 w-full" />
      </div>
      <div>
        <label className="block text-sm font-medium mb-1">Număr sticle</label>
        <input type="number" value={bottleCount} onChange={e => setBottleCount(e.target.value)} required className="border rounded px-3 py-2 w-full" />
      </div>
      <div>
        <label className="block text-sm font-medium mb-1">Volum sticlă</label>
        <select value={bottleVolume} onChange={e => setBottleVolume(e.target.value)} className="border rounded px-3 py-2 w-full">
          <option value="L075">0.75 L</option>
          <option value="L150">1.5 L</option>
        </select>
      </div>
      {error && <p className="text-red-600 text-sm">{error}</p>}
      <button type="submit" className="bg-purple-500 text-white px-4 py-2 rounded hover:bg-purple-600" disabled={mutation.isPending}>
        {mutation.isPending ? 'Se salvează...' : 'Salvează'}
      </button>
    </form>
  )
}
```

- [ ] **Step 9: Create StageTimeline.jsx**

```jsx
export default function StageTimeline({ harvest, pressing, bottling }) {
  const stages = [
    { label: 'Cules', data: harvest, detail: harvest ? `${harvest.quantityKg} kg — ${harvest.date}` : null },
    { label: 'Stors', data: pressing, detail: pressing ? `${pressing.mustLiters} L (randament: ${(pressing.yieldRatio * 100).toFixed(1)}%)` : null },
    { label: 'Îmbuteliere', data: bottling, detail: bottling ? `${bottling.bottleCount} sticle × ${bottling.bottleVolume === 'L075' ? '0.75L' : '1.5L'}` : null },
  ]

  return (
    <div className="flex gap-4 mb-6">
      {stages.map((s, i) => (
        <div key={i} className={`flex-1 rounded-lg p-3 ${s.data ? 'bg-green-50 border border-green-200' : 'bg-gray-50 border border-gray-200'}`}>
          <div className={`font-medium ${s.data ? 'text-green-700' : 'text-gray-400'}`}>{s.label}</div>
          {s.detail && <div className="text-sm text-gray-600 mt-1">{s.detail}</div>}
          {!s.data && <div className="text-xs text-gray-400 mt-1">neînregistrat</div>}
        </div>
      ))}
    </div>
  )
}
```

- [ ] **Step 10: Create BatchDetail.jsx**

```jsx
import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getBatch, getHarvest, getPressing, getBottling } from '../api/batches'
import StageTimeline from '../components/StageTimeline'
import HarvestForm from '../components/HarvestForm'
import PressingForm from '../components/PressingForm'
import BottlingForm from '../components/BottlingForm'

export default function BatchDetail() {
  const { id } = useParams()
  const { data: batch, isLoading } = useQuery({ queryKey: ['batch', id], queryFn: () => getBatch(id) })
  const { data: harvest } = useQuery({ queryKey: ['harvest', id], queryFn: () => getHarvest(id) })
  const { data: pressing } = useQuery({ queryKey: ['pressing', id], queryFn: () => getPressing(id) })
  const { data: bottling } = useQuery({ queryKey: ['bottling', id], queryFn: () => getBottling(id) })

  if (isLoading) return <p>Se încarcă...</p>
  if (!batch) return <p>Lot negăsit.</p>

  return (
    <div>
      <h1 className="text-2xl font-bold mb-2">{batch.parcelName} — {batch.year}</h1>
      <StageTimeline harvest={harvest} pressing={pressing} bottling={bottling} />
      <div className="bg-white rounded-lg shadow p-6">
        {batch.status === 'HARVESTED' && !harvest && <HarvestForm batchId={id} />}
        {batch.status === 'HARVESTED' && harvest && <PressingForm batchId={id} />}
        {batch.status === 'PRESSED' && <PressingForm batchId={id} />}
        {batch.status === 'PRESSED' && pressing && <BottlingForm batchId={id} />}
        {batch.status === 'BOTTLED' && <p className="text-green-600 font-medium">Lot finalizat.</p>}
      </div>
    </div>
  )
}
```

- [ ] **Step 11: Create report pages**

```jsx
// frontend/src/pages/ReportHarvest.jsx
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getHarvestReport } from '../api/reports'

export default function ReportHarvest() {
  const [year, setYear] = useState(new Date().getFullYear())
  const { data = [], isLoading } = useQuery({
    queryKey: ['report', 'harvest', year],
    queryFn: () => getHarvestReport(year),
  })

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Raport Cules</h1>
      <input type="number" value={year} onChange={e => setYear(parseInt(e.target.value))} className="border rounded px-3 py-2 mb-4 w-24" />
      {isLoading ? <p>Se încarcă...</p> : (
        <table className="w-full bg-white rounded-lg shadow text-sm">
          <thead className="bg-gray-100"><tr>
            <th className="px-4 py-2 text-left">Parcelă</th>
            <th className="px-4 py-2 text-right">Total kg</th>
            <th className="px-4 py-2 text-right">Medie kg</th>
          </tr></thead>
          <tbody>
            {data.map((r, i) => (
              <tr key={i} className="border-t">
                <td className="px-4 py-2">{r.parcelName}</td>
                <td className="px-4 py-2 text-right">{r.totalKg}</td>
                <td className="px-4 py-2 text-right">{r.avgKg}</td>
              </tr>
            ))}
            {data.length === 0 && <tr><td colSpan={3} className="px-4 py-3 text-gray-400">Fără date.</td></tr>}
          </tbody>
        </table>
      )}
    </div>
  )
}
```

```jsx
// frontend/src/pages/ReportPressing.jsx
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getPressingReport } from '../api/reports'

export default function ReportPressing() {
  const [year, setYear] = useState(new Date().getFullYear())
  const { data = [], isLoading } = useQuery({
    queryKey: ['report', 'pressing', year],
    queryFn: () => getPressingReport(year),
  })

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Raport Stors</h1>
      <input type="number" value={year} onChange={e => setYear(parseInt(e.target.value))} className="border rounded px-3 py-2 mb-4 w-24" />
      {isLoading ? <p>Se încarcă...</p> : (
        <table className="w-full bg-white rounded-lg shadow text-sm">
          <thead className="bg-gray-100"><tr>
            <th className="px-4 py-2 text-left">Parcelă</th>
            <th className="px-4 py-2 text-right">Total litri must</th>
            <th className="px-4 py-2 text-right">Randament mediu</th>
          </tr></thead>
          <tbody>
            {data.map((r, i) => (
              <tr key={i} className="border-t">
                <td className="px-4 py-2">{r.parcelName}</td>
                <td className="px-4 py-2 text-right">{r.totalMustLiters}</td>
                <td className="px-4 py-2 text-right">{(r.avgYieldRatio * 100).toFixed(1)}%</td>
              </tr>
            ))}
            {data.length === 0 && <tr><td colSpan={3} className="px-4 py-3 text-gray-400">Fără date.</td></tr>}
          </tbody>
        </table>
      )}
    </div>
  )
}
```

```jsx
// frontend/src/pages/ReportBottling.jsx
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getBottlingReport } from '../api/reports'

export default function ReportBottling() {
  const [year, setYear] = useState(new Date().getFullYear())
  const { data = [], isLoading } = useQuery({
    queryKey: ['report', 'bottling', year],
    queryFn: () => getBottlingReport(year),
  })

  const volumeLabel = (v) => v === 'L075' ? '0.75 L' : '1.5 L'

  return (
    <div>
      <h1 className="text-2xl font-bold mb-4">Raport Îmbuteliere</h1>
      <input type="number" value={year} onChange={e => setYear(parseInt(e.target.value))} className="border rounded px-3 py-2 mb-4 w-24" />
      {isLoading ? <p>Se încarcă...</p> : (
        <table className="w-full bg-white rounded-lg shadow text-sm">
          <thead className="bg-gray-100"><tr>
            <th className="px-4 py-2 text-left">Volum sticlă</th>
            <th className="px-4 py-2 text-right">Total sticle</th>
          </tr></thead>
          <tbody>
            {data.map((r, i) => (
              <tr key={i} className="border-t">
                <td className="px-4 py-2">{volumeLabel(r.bottleVolume)}</td>
                <td className="px-4 py-2 text-right">{r.totalBottles}</td>
              </tr>
            ))}
            {data.length === 0 && <tr><td colSpan={2} className="px-4 py-3 text-gray-400">Fără date.</td></tr>}
          </tbody>
        </table>
      )}
    </div>
  )
}
```

- [ ] **Step 12: Commit**

```bash
git add frontend/src
git commit -m "feat: all frontend pages and stage forms"
```

---

## Task 13: Frontend Dockerfile & Nginx config

**Files:**
- Create: `frontend/Dockerfile`
- Create: `frontend/nginx.conf`

- [ ] **Step 1: Create frontend/nginx.conf**

```nginx
server {
    listen 3000;

    location /api/ {
        proxy_pass http://vineyard-api:8080;
        proxy_set_header Host $host;
    }

    location /swagger-ui/ {
        proxy_pass http://vineyard-api:8080;
        proxy_set_header Host $host;
    }

    location /v3/ {
        proxy_pass http://vineyard-api:8080;
        proxy_set_header Host $host;
    }

    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }
}
```

- [ ] **Step 2: Create frontend/Dockerfile**

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 3000
```

- [ ] **Step 3: Commit**

```bash
git add frontend/Dockerfile frontend/nginx.conf
git commit -m "chore: frontend Dockerfile and nginx config"
```

---

## Task 14: docker-compose.yml & .env

**Files:**
- Create: `docker-compose.yml`
- Create: `.env.example`

- [ ] **Step 1: Create .env.example**

```
POSTGRES_PASSWORD=changeme
```

- [ ] **Step 2: Create docker-compose.yml**

```yaml
services:
  vineyard-db:
    image: postgres:16
    container_name: vineyard-db
    environment:
      POSTGRES_DB: vineyard
      POSTGRES_USER: vineyard
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - vineyard-db-data:/var/lib/postgresql/data
    networks:
      - vineyard-net
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U vineyard"]
      interval: 5s
      timeout: 5s
      retries: 5

  vineyard-api:
    build: ./backend
    container_name: vineyard-api
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://vineyard-db:5432/vineyard
      SPRING_DATASOURCE_USERNAME: vineyard
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
    depends_on:
      vineyard-db:
        condition: service_healthy
    networks:
      - vineyard-net

  vineyard-ui:
    build: ./frontend
    container_name: vineyard-ui
    depends_on:
      - vineyard-api
    networks:
      - vineyard-net

  cloudflared:
    image: cloudflare/cloudflared:latest
    container_name: vineyard-cloudflared
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

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml .env.example
git commit -m "chore: docker-compose with all 4 services"
```

---

## Task 15: Cloudflare Tunnel setup

This task requires manual steps on the Cloudflare dashboard and local CLI.

- [ ] **Step 1: Install cloudflared locally (if not already)**

```bash
brew install cloudflare/cloudflare/cloudflared
```

- [ ] **Step 2: Authenticate**

```bash
cloudflared tunnel login
```

A browser window opens — log in with your Cloudflare account.

- [ ] **Step 3: Create the named tunnel**

```bash
cloudflared tunnel create vineyard-tunnel
```

This creates `~/.cloudflared/<UUID>.json` (tunnel credentials).

- [ ] **Step 4: Create tunnel config at ~/.cloudflared/config.yml**

```yaml
tunnel: vineyard-tunnel
credentials-file: /home/nonroot/.cloudflared/<UUID>.json

ingress:
  - hostname: vineyard-manager.velicans.eu
    path: ^/(api|swagger-ui|v3)/.*
    service: http://vineyard-api:8080
  - hostname: vineyard-manager.velicans.eu
    service: http://vineyard-ui:3000
  - service: http_status:404
```

Replace `<UUID>` with the actual UUID from Step 3.

- [ ] **Step 5: Create DNS CNAME record**

```bash
cloudflared tunnel route dns vineyard-tunnel vineyard-manager.velicans.eu
```

- [ ] **Step 6: Verify tunnel config**

```bash
cloudflared tunnel ingress validate
```

Expected: `Validating rules from /Users/<you>/.cloudflared/config.yml` with no errors.

---

## Task 16: Full stack smoke test

- [ ] **Step 1: Copy .env and start stack**

```bash
cp .env.example .env
# Edit .env and set a real password
docker compose up --build -d
```

- [ ] **Step 2: Check all containers are running**

```bash
docker compose ps
```

Expected: all 4 containers with status `Up`.

- [ ] **Step 3: Check API health**

```bash
curl http://localhost:8080/api/parcels
```

Expected: `[]` (empty array, Flyway migrations ran).

- [ ] **Step 4: Check Swagger UI**

Open in browser: `http://localhost:8080/swagger-ui.html`

Expected: Swagger UI showing all API endpoints.

- [ ] **Step 5: Download OpenAPI spec for Postman**

```bash
curl http://localhost:8080/v3/api-docs -o vineyard-api.json
```

Import `vineyard-api.json` into Postman.

- [ ] **Step 6: Create a parcel via API**

```bash
curl -X POST http://localhost:8080/api/parcels \
  -H 'Content-Type: application/json' \
  -d '{"name":"Nord","grapeVariety":"Feteasca Alba","areaSqM":1200}'
```

Expected: JSON response with generated UUID.

- [ ] **Step 7: Check frontend**

Open `http://localhost:3000` — Dashboard loads, parcel appears in Parcele page.

- [ ] **Step 8: Check tunnel**

Open `https://vineyard-manager.velicans.eu` — same UI loads over HTTPS.

- [ ] **Step 9: Final commit**

```bash
git add .
git commit -m "chore: project complete — all services wired and verified"
```
