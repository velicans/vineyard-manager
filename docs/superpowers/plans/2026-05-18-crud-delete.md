# CRUD Delete & Add Parcels Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add delete operations for parcels, batches, harvest and pressing records, plus a create-parcel form in the UI.

**Architecture:** Backend adds DELETE endpoints to 4 modules with cascade handled in service layer. Frontend adds delete buttons to ParcelList, BatchList, StageTimeline and a create form in ParcelList. All tests are Mockito unit tests following existing patterns.

**Tech Stack:** Java 21, Spring Boot 3.x, JUnit 5 + Mockito, React 18, TanStack Query v5, Tailwind CSS.

---

## File Map

### Backend — modify existing files
```
backend/src/main/java/ro/velicans/vineyard/
  batch/ProductionBatchService.java   — add delete(UUID id), needs BottlingRepo + PressingRepo + HarvestRepo injected
  batch/ProductionBatchController.java — add DELETE /api/batches/{id}
  parcel/ParcelService.java           — add delete(UUID id), needs ProductionBatchRepository + ProductionBatchService injected
  parcel/ParcelController.java        — add DELETE /api/parcels/{id}
  harvest/HarvestService.java         — add delete(UUID batchId)
  harvest/HarvestController.java      — add DELETE /api/batches/{batchId}/harvest
  pressing/PressingService.java       — add delete(UUID batchId), resets batch status to HARVESTED
  pressing/PressingController.java    — add DELETE /api/batches/{batchId}/pressing

backend/src/test/java/ro/velicans/vineyard/
  batch/ProductionBatchServiceTest.java   — add delete tests
  parcel/ParcelServiceTest.java           — add delete tests
  harvest/HarvestServiceTest.java         — add delete tests
  pressing/PressingServiceTest.java       — add delete tests
```

### Frontend — modify existing files
```
frontend/src/api/parcels.js          — add deleteParcel
frontend/src/api/batches.js          — add deleteBatch, deleteHarvest, deletePressing
frontend/src/pages/ParcelList.jsx    — add create form + delete button per parcel
frontend/src/pages/BatchList.jsx     — add delete button per batch row
frontend/src/components/StageTimeline.jsx — add onDeleteHarvest + onDeletePressing props
frontend/src/pages/BatchDetail.jsx   — wire delete handlers to StageTimeline
```

---

## Task 1: Backend — delete harvest

**Files:**
- Modify: `backend/src/main/java/ro/velicans/vineyard/harvest/HarvestService.java`
- Modify: `backend/src/main/java/ro/velicans/vineyard/harvest/HarvestController.java`
- Modify: `backend/src/test/java/ro/velicans/vineyard/harvest/HarvestServiceTest.java`

- [ ] **Step 1: Write failing test**

Add to `HarvestServiceTest.java` (add to existing class — keep existing tests):

```java
@Test
void delete_removesHarvestWhenExists() {
    UUID batchId = UUID.randomUUID();
    Harvest h = new Harvest();
    when(repo.findByBatchId(batchId)).thenReturn(Optional.of(h));

    service.delete(batchId);

    verify(repo).delete(h);
}

@Test
void delete_doesNothingWhenNoHarvest() {
    UUID batchId = UUID.randomUUID();
    when(repo.findByBatchId(batchId)).thenReturn(Optional.empty());

    service.delete(batchId);

    verify(repo, never()).delete(any());
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
cd /Users/sorin/workspace/vineyard-mnager/backend && mvn test -Dtest=HarvestServiceTest 2>&1 | grep "cannot find symbol\|BUILD" | head -5
```

Expected: compile error — `delete` method not found.

- [ ] **Step 3: Add delete method to HarvestService.java**

Add after `findByBatchId`:

```java
public void delete(UUID batchId) {
    repo.findByBatchId(batchId).ifPresent(repo::delete);
}
```

- [ ] **Step 4: Add DELETE endpoint to HarvestController.java**

Add after `get`:

```java
@DeleteMapping
@ResponseStatus(HttpStatus.NO_CONTENT)
public void delete(@PathVariable UUID batchId) {
    service.delete(batchId);
}
```

Add `import org.springframework.http.HttpStatus;` if not already present (it is).

- [ ] **Step 5: Run tests**

```bash
cd /Users/sorin/workspace/vineyard-mnager/backend && mvn test -Dtest=HarvestServiceTest -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
cd /Users/sorin/workspace/vineyard-mnager && git add backend/src && git commit -m "feat: delete harvest endpoint"
```

---

## Task 2: Backend — delete pressing

**Files:**
- Modify: `backend/src/main/java/ro/velicans/vineyard/pressing/PressingService.java`
- Modify: `backend/src/main/java/ro/velicans/vineyard/pressing/PressingController.java`
- Modify: `backend/src/test/java/ro/velicans/vineyard/pressing/PressingServiceTest.java`

- [ ] **Step 1: Write failing test**

Add to `PressingServiceTest.java` (keep existing tests):

```java
@Test
void delete_removesPressingAndResetsBatchStatus() {
    UUID batchId = UUID.randomUUID();
    Pressing p = new Pressing();
    when(repo.findByBatchId(batchId)).thenReturn(Optional.of(p));

    service.delete(batchId);

    verify(repo).delete(p);
    verify(batchService).updateStatus(batchId, BatchStatus.HARVESTED);
}

@Test
void delete_doesNothingWhenNoPressing() {
    UUID batchId = UUID.randomUUID();
    when(repo.findByBatchId(batchId)).thenReturn(Optional.empty());

    service.delete(batchId);

    verify(repo, never()).delete(any());
    verify(batchService, never()).updateStatus(any(), any());
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
cd /Users/sorin/workspace/vineyard-mnager/backend && mvn test -Dtest=PressingServiceTest 2>&1 | grep "cannot find symbol\|BUILD" | head -5
```

- [ ] **Step 3: Add delete method to PressingService.java**

Add after `findByBatchId`:

```java
public void delete(UUID batchId) {
    repo.findByBatchId(batchId).ifPresent(p -> {
        repo.delete(p);
        batchService.updateStatus(batchId, BatchStatus.HARVESTED);
    });
}
```

- [ ] **Step 4: Add DELETE endpoint to PressingController.java**

Add after `get`:

```java
@DeleteMapping
@ResponseStatus(HttpStatus.NO_CONTENT)
public void delete(@PathVariable UUID batchId) {
    service.delete(batchId);
}
```

- [ ] **Step 5: Run tests**

```bash
cd /Users/sorin/workspace/vineyard-mnager/backend && mvn test -Dtest=PressingServiceTest -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
cd /Users/sorin/workspace/vineyard-mnager && git add backend/src && git commit -m "feat: delete pressing endpoint, resets batch to HARVESTED"
```

---

## Task 3: Backend — delete batch (cascade)

**Files:**
- Modify: `backend/src/main/java/ro/velicans/vineyard/batch/ProductionBatchService.java`
- Modify: `backend/src/main/java/ro/velicans/vineyard/batch/ProductionBatchController.java`
- Modify: `backend/src/test/java/ro/velicans/vineyard/batch/ProductionBatchServiceTest.java`

- [ ] **Step 1: Write failing test**

Add to `ProductionBatchServiceTest.java` (keep existing tests, add new mocks):

```java
@Mock ro.velicans.vineyard.bottling.BottlingRepository bottlingRepo;
@Mock ro.velicans.vineyard.pressing.PressingRepository pressingRepo;
@Mock ro.velicans.vineyard.harvest.HarvestRepository harvestRepo;
```

Add tests:

```java
@Test
void delete_removesAllCascadedDataThenBatch() {
    UUID batchId = UUID.randomUUID();
    Parcel p = new Parcel();
    p.setId(UUID.randomUUID());
    p.setName("Nord");
    p.setGrapeVariety("Feteasca");
    p.setAreaSqM(1000);
    ProductionBatch batch = new ProductionBatch();
    batch.setId(batchId);
    batch.setParcel(p);
    batch.setYear(2024);
    batch.setStatus(BatchStatus.BOTTLED);
    when(batchRepo.findById(batchId)).thenReturn(Optional.of(batch));

    ro.velicans.vineyard.bottling.Bottling bottling = new ro.velicans.vineyard.bottling.Bottling();
    ro.velicans.vineyard.pressing.Pressing pressing = new ro.velicans.vineyard.pressing.Pressing();
    ro.velicans.vineyard.harvest.Harvest harvest = new ro.velicans.vineyard.harvest.Harvest();
    when(bottlingRepo.findByBatchId(batchId)).thenReturn(Optional.of(bottling));
    when(pressingRepo.findByBatchId(batchId)).thenReturn(Optional.of(pressing));
    when(harvestRepo.findByBatchId(batchId)).thenReturn(Optional.of(harvest));

    service.delete(batchId);

    verify(bottlingRepo).delete(bottling);
    verify(pressingRepo).delete(pressing);
    verify(harvestRepo).delete(harvest);
    verify(batchRepo).deleteById(batchId);
}

@Test
void delete_throwsWhenBatchNotFound() {
    UUID batchId = UUID.randomUUID();
    when(batchRepo.findById(batchId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.delete(batchId))
        .isInstanceOf(NoSuchElementException.class);
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
cd /Users/sorin/workspace/vineyard-mnager/backend && mvn test -Dtest=ProductionBatchServiceTest 2>&1 | grep "cannot find symbol\|BUILD" | head -5
```

- [ ] **Step 3: Update ProductionBatchService.java**

Replace full file content:

```java
package ro.velicans.vineyard.batch;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.velicans.vineyard.bottling.BottlingRepository;
import ro.velicans.vineyard.harvest.HarvestRepository;
import ro.velicans.vineyard.parcel.ParcelRepository;
import ro.velicans.vineyard.pressing.PressingRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Transactional
@Service
public class ProductionBatchService {

    private final ProductionBatchRepository batchRepo;
    private final ParcelRepository parcelRepo;
    private final HarvestRepository harvestRepo;
    private final PressingRepository pressingRepo;
    private final BottlingRepository bottlingRepo;

    public ProductionBatchService(ProductionBatchRepository batchRepo, ParcelRepository parcelRepo,
                                   HarvestRepository harvestRepo, PressingRepository pressingRepo,
                                   BottlingRepository bottlingRepo) {
        this.batchRepo = batchRepo;
        this.parcelRepo = parcelRepo;
        this.harvestRepo = harvestRepo;
        this.pressingRepo = pressingRepo;
        this.bottlingRepo = bottlingRepo;
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

    public void delete(UUID batchId) {
        findEntityById(batchId);
        bottlingRepo.findByBatchId(batchId).ifPresent(bottlingRepo::delete);
        pressingRepo.findByBatchId(batchId).ifPresent(pressingRepo::delete);
        harvestRepo.findByBatchId(batchId).ifPresent(harvestRepo::delete);
        batchRepo.deleteById(batchId);
    }

    private ProductionBatchDto toDto(ProductionBatch b) {
        return new ProductionBatchDto(b.getId(), b.getParcel().getId(), b.getParcel().getName(), b.getYear(), b.getStatus());
    }
}
```

- [ ] **Step 4: Add DELETE endpoint to ProductionBatchController.java**

Add after `create`:

```java
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void delete(@PathVariable UUID id) {
    service.delete(id);
}
```

- [ ] **Step 5: Run all tests**

```bash
cd /Users/sorin/workspace/vineyard-mnager/backend && mvn test -q
```

Expected: `BUILD SUCCESS`, all tests green.

- [ ] **Step 6: Commit**

```bash
cd /Users/sorin/workspace/vineyard-mnager && git add backend/src && git commit -m "feat: delete batch endpoint with cascade"
```

---

## Task 4: Backend — delete parcel (cascade)

**Files:**
- Modify: `backend/src/main/java/ro/velicans/vineyard/parcel/ParcelService.java`
- Modify: `backend/src/main/java/ro/velicans/vineyard/parcel/ParcelController.java`
- Modify: `backend/src/test/java/ro/velicans/vineyard/parcel/ParcelServiceTest.java`

- [ ] **Step 1: Write failing test**

Add to `ParcelServiceTest.java` (add new mock, keep existing tests):

```java
@Mock ro.velicans.vineyard.batch.ProductionBatchRepository batchRepo;
@Mock ro.velicans.vineyard.batch.ProductionBatchService batchService;
```

Add tests:

```java
@Test
void delete_cascadesToBatches() {
    UUID parcelId = UUID.randomUUID();
    Parcel p = new Parcel();
    p.setId(parcelId);
    p.setName("Nord");
    p.setGrapeVariety("Feteasca");
    p.setAreaSqM(1000);
    when(repo.findById(parcelId)).thenReturn(Optional.of(p));

    ro.velicans.vineyard.batch.ProductionBatch batch = new ro.velicans.vineyard.batch.ProductionBatch();
    UUID batchId = UUID.randomUUID();
    batch.setId(batchId);
    batch.setParcel(p);
    batch.setYear(2024);
    batch.setStatus(ro.velicans.vineyard.batch.BatchStatus.HARVESTED);
    when(batchRepo.findByParcelId(parcelId)).thenReturn(List.of(batch));

    service.delete(parcelId);

    verify(batchService).delete(batchId);
    verify(repo).deleteById(parcelId);
}

@Test
void delete_throwsWhenParcelNotFound() {
    UUID parcelId = UUID.randomUUID();
    when(repo.findById(parcelId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.delete(parcelId))
        .isInstanceOf(NoSuchElementException.class);
}
```

- [ ] **Step 2: Run test — expect compile failure**

```bash
cd /Users/sorin/workspace/vineyard-mnager/backend && mvn test -Dtest=ParcelServiceTest 2>&1 | grep "cannot find symbol\|BUILD" | head -5
```

- [ ] **Step 3: Update ParcelService.java**

Replace full file content:

```java
package ro.velicans.vineyard.parcel;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.velicans.vineyard.batch.ProductionBatchRepository;
import ro.velicans.vineyard.batch.ProductionBatchService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Transactional
@Service
public class ParcelService {

    private final ParcelRepository repo;
    private final ProductionBatchRepository batchRepo;
    private final ProductionBatchService batchService;

    public ParcelService(ParcelRepository repo, ProductionBatchRepository batchRepo, ProductionBatchService batchService) {
        this.repo = repo;
        this.batchRepo = batchRepo;
        this.batchService = batchService;
    }

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

    public void delete(UUID id) {
        repo.findById(id).orElseThrow(() -> new NoSuchElementException("Parcel not found: " + id));
        batchRepo.findByParcelId(id).forEach(b -> batchService.delete(b.getId()));
        repo.deleteById(id);
    }

    private ParcelDto toDto(Parcel p) {
        return new ParcelDto(p.getId(), p.getName(), p.getGrapeVariety(), p.getAreaSqM());
    }
}
```

- [ ] **Step 4: Add DELETE endpoint to ParcelController.java**

Add after `create`:

```java
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void delete(@PathVariable UUID id) {
    service.delete(id);
}
```

- [ ] **Step 5: Run all tests**

```bash
cd /Users/sorin/workspace/vineyard-mnager/backend && mvn test -q
```

Expected: `BUILD SUCCESS`, all tests green.

- [ ] **Step 6: Commit**

```bash
cd /Users/sorin/workspace/vineyard-mnager && git add backend/src && git commit -m "feat: delete parcel endpoint with cascade through batches"
```

---

## Task 5: Frontend — API layer

**Files:**
- Modify: `frontend/src/api/parcels.js`
- Modify: `frontend/src/api/batches.js`

- [ ] **Step 1: Add deleteParcel to parcels.js**

Replace full file content:

```js
import client from './client'

export const getParcels = () => client.get('/parcels').then(r => r.data)
export const getParcel = (id) => client.get(`/parcels/${id}`).then(r => r.data)
export const createParcel = (data) => client.post('/parcels', data).then(r => r.data)
export const deleteParcel = (id) => client.delete(`/parcels/${id}`)
```

- [ ] **Step 2: Add delete functions to batches.js**

Replace full file content:

```js
import client from './client'

export const getBatches = () => client.get('/batches').then(r => r.data)
export const getBatch = (id) => client.get(`/batches/${id}`).then(r => r.data)
export const createBatch = (data) => client.post('/batches', data).then(r => r.data)
export const deleteBatch = (id) => client.delete(`/batches/${id}`)
export const recordHarvest = (batchId, data) => client.post(`/batches/${batchId}/harvest`, data).then(r => r.data)
export const recordPressing = (batchId, data) => client.post(`/batches/${batchId}/pressing`, data).then(r => r.data)
export const recordBottling = (batchId, data) => client.post(`/batches/${batchId}/bottling`, data).then(r => r.data)
export const getHarvest = (batchId) => client.get(`/batches/${batchId}/harvest`).then(r => r.data)
export const getPressing = (batchId) => client.get(`/batches/${batchId}/pressing`).then(r => r.data)
export const getBottling = (batchId) => client.get(`/batches/${batchId}/bottling`).then(r => r.data)
export const deleteHarvest = (batchId) => client.delete(`/batches/${batchId}/harvest`)
export const deletePressing = (batchId) => client.delete(`/batches/${batchId}/pressing`)
```

- [ ] **Step 3: Verify build**

```bash
cd /Users/sorin/workspace/vineyard-mnager/frontend && npm run build 2>&1 | tail -5
```

Expected: build succeeds (no new errors — these are just new exports).

- [ ] **Step 4: Commit**

```bash
cd /Users/sorin/workspace/vineyard-mnager && git add frontend/src/api && git commit -m "feat: add delete functions to API client"
```

---

## Task 6: Frontend — ParcelList with create form and delete

**Files:**
- Modify: `frontend/src/pages/ParcelList.jsx`

- [ ] **Step 1: Replace ParcelList.jsx**

```jsx
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getParcels, createParcel, deleteParcel } from '../api/parcels'

export default function ParcelList() {
  const qc = useQueryClient()
  const { data: parcels = [], isLoading } = useQuery({ queryKey: ['parcels'], queryFn: getParcels })
  const [showForm, setShowForm] = useState(false)
  const [name, setName] = useState('')
  const [grapeVariety, setGrapeVariety] = useState('')
  const [areaSqM, setAreaSqM] = useState('')
  const [formError, setFormError] = useState(null)

  const createMutation = useMutation({
    mutationFn: createParcel,
    onSuccess: () => {
      qc.invalidateQueries(['parcels'])
      setShowForm(false)
      setName('')
      setGrapeVariety('')
      setAreaSqM('')
      setFormError(null)
    },
    onError: (err) => setFormError(err.message),
  })

  const deleteMutation = useMutation({
    mutationFn: deleteParcel,
    onSuccess: () => qc.invalidateQueries(['parcels']),
  })

  const handleCreate = (e) => {
    e.preventDefault()
    createMutation.mutate({ name, grapeVariety, areaSqM: parseInt(areaSqM) })
  }

  const handleDelete = (id, parcelName) => {
    if (!window.confirm(`Ștergi parcela "${parcelName}"? Această acțiune nu poate fi anulată.`)) return
    deleteMutation.mutate(id)
  }

  if (isLoading) return <p>Se încarcă...</p>

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Parcele</h1>
        <button
          onClick={() => setShowForm(!showForm)}
          className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
        >
          {showForm ? 'Anulează' : '+ Parcelă nouă'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="bg-white rounded-lg shadow p-4 mb-6 space-y-3">
          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-sm font-medium mb-1">Nume</label>
              <input
                type="text" value={name} onChange={e => setName(e.target.value)} required
                className="w-full border rounded px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Soi</label>
              <input
                type="text" value={grapeVariety} onChange={e => setGrapeVariety(e.target.value)} required
                className="w-full border rounded px-3 py-2 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">Suprafață (m²)</label>
              <input
                type="number" value={areaSqM} onChange={e => setAreaSqM(e.target.value)} required min="1"
                className="w-full border rounded px-3 py-2 text-sm"
              />
            </div>
          </div>
          {formError && <p className="text-red-600 text-sm">{formError}</p>}
          <button
            type="submit"
            className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 text-sm"
            disabled={createMutation.isPending}
          >
            {createMutation.isPending ? 'Se salvează...' : 'Salvează'}
          </button>
        </form>
      )}

      <div className="grid gap-4">
        {parcels.map(p => (
          <div key={p.id} className="bg-white rounded-lg shadow p-4 flex items-center justify-between">
            <div>
              <div className="font-semibold text-lg">{p.name}</div>
              <div className="text-gray-500">{p.grapeVariety}</div>
              <div className="text-sm text-gray-400">{p.areaSqM} m²</div>
            </div>
            <button
              onClick={() => handleDelete(p.id, p.name)}
              className="text-red-500 hover:text-red-700 text-sm px-3 py-1 border border-red-200 rounded hover:bg-red-50"
              disabled={deleteMutation.isPending}
            >
              Șterge
            </button>
          </div>
        ))}
        {parcels.length === 0 && <p className="text-gray-400">Nicio parcelă înregistrată.</p>}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

```bash
cd /Users/sorin/workspace/vineyard-mnager/frontend && npm run build 2>&1 | tail -5
```

Expected: `built in ...ms` with no errors.

- [ ] **Step 3: Commit**

```bash
cd /Users/sorin/workspace/vineyard-mnager && git add frontend/src/pages/ParcelList.jsx && git commit -m "feat: add create form and delete button to ParcelList"
```

---

## Task 7: Frontend — BatchList delete button

**Files:**
- Modify: `frontend/src/pages/BatchList.jsx`

- [ ] **Step 1: Replace BatchList.jsx**

```jsx
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { useState } from 'react'
import { getBatches, deleteBatch } from '../api/batches'

export default function BatchList() {
  const qc = useQueryClient()
  const { data: batches = [], isLoading } = useQuery({ queryKey: ['batches'], queryFn: getBatches })
  const [yearFilter, setYearFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')

  const deleteMutation = useMutation({
    mutationFn: deleteBatch,
    onSuccess: () => qc.invalidateQueries(['batches']),
  })

  const handleDelete = (id, parcelName, year) => {
    if (!window.confirm(`Ștergi lotul "${parcelName} — ${year}"? Această acțiune nu poate fi anulată.`)) return
    deleteMutation.mutate(id)
  }

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
          <div key={b.id} className="flex items-center justify-between px-4 py-3 hover:bg-gray-50">
            <Link to={`/batches/${b.id}`} className="flex items-center gap-6 flex-1">
              <span className="font-medium">{b.parcelName}</span>
              <span className="text-gray-500">{b.year}</span>
              <span className={`text-xs px-2 py-1 rounded-full ${statusColor(b.status)}`}>{b.status}</span>
            </Link>
            <button
              onClick={() => handleDelete(b.id, b.parcelName, b.year)}
              className="text-red-500 hover:text-red-700 text-sm px-3 py-1 border border-red-200 rounded hover:bg-red-50 ml-4"
              disabled={deleteMutation.isPending}
            >
              Șterge
            </button>
          </div>
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

- [ ] **Step 2: Verify build**

```bash
cd /Users/sorin/workspace/vineyard-mnager/frontend && npm run build 2>&1 | tail -5
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
cd /Users/sorin/workspace/vineyard-mnager && git add frontend/src/pages/BatchList.jsx && git commit -m "feat: add delete button to BatchList"
```

---

## Task 8: Frontend — StageTimeline delete buttons + BatchDetail wiring

**Files:**
- Modify: `frontend/src/components/StageTimeline.jsx`
- Modify: `frontend/src/pages/BatchDetail.jsx`

- [ ] **Step 1: Replace StageTimeline.jsx**

```jsx
export default function StageTimeline({ harvest, pressing, bottling, onDeleteHarvest, onDeletePressing }) {
  const stages = [
    {
      label: 'Cules',
      data: harvest,
      detail: harvest ? `${harvest.quantityKg} kg — ${harvest.date}` : null,
      onDelete: onDeleteHarvest,
    },
    {
      label: 'Stors',
      data: pressing,
      detail: pressing ? `${pressing.mustLiters} L (randament: ${(pressing.yieldRatio * 100).toFixed(1)}%)` : null,
      onDelete: onDeletePressing,
    },
    {
      label: 'Îmbuteliere',
      data: bottling,
      detail: bottling ? `${bottling.bottleCount} sticle × ${bottling.bottleVolume === 'L075' ? '0.75L' : '1.5L'}` : null,
      onDelete: null,
    },
  ]

  return (
    <div className="flex gap-4 mb-6">
      {stages.map((s, i) => (
        <div key={i} className={`flex-1 rounded-lg p-3 ${s.data ? 'bg-green-50 border border-green-200' : 'bg-gray-50 border border-gray-200'}`}>
          <div className="flex items-center justify-between">
            <div className={`font-medium ${s.data ? 'text-green-700' : 'text-gray-400'}`}>{s.label}</div>
            {s.data && s.onDelete && (
              <button
                onClick={s.onDelete}
                className="text-red-400 hover:text-red-600 text-xs px-2 py-0.5 border border-red-200 rounded hover:bg-red-50"
              >
                Șterge
              </button>
            )}
          </div>
          {s.detail && <div className="text-sm text-gray-600 mt-1">{s.detail}</div>}
          {!s.data && <div className="text-xs text-gray-400 mt-1">neînregistrat</div>}
        </div>
      ))}
    </div>
  )
}
```

- [ ] **Step 2: Replace BatchDetail.jsx**

```jsx
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getBatch, getHarvest, getPressing, getBottling, deleteHarvest, deletePressing } from '../api/batches'
import StageTimeline from '../components/StageTimeline'
import HarvestForm from '../components/HarvestForm'
import PressingForm from '../components/PressingForm'
import BottlingForm from '../components/BottlingForm'

export default function BatchDetail() {
  const { id } = useParams()
  const qc = useQueryClient()
  const { data: batch, isLoading } = useQuery({ queryKey: ['batch', id], queryFn: () => getBatch(id) })
  const { data: harvest } = useQuery({ queryKey: ['harvest', id], queryFn: () => getHarvest(id) })
  const { data: pressing } = useQuery({ queryKey: ['pressing', id], queryFn: () => getPressing(id) })
  const { data: bottling } = useQuery({ queryKey: ['bottling', id], queryFn: () => getBottling(id) })

  const deleteHarvestMutation = useMutation({
    mutationFn: () => deleteHarvest(id),
    onSuccess: () => {
      qc.invalidateQueries(['batch', id])
      qc.invalidateQueries(['harvest', id])
    },
  })

  const deletePressingMutation = useMutation({
    mutationFn: () => deletePressing(id),
    onSuccess: () => {
      qc.invalidateQueries(['batch', id])
      qc.invalidateQueries(['pressing', id])
    },
  })

  const handleDeleteHarvest = () => {
    if (!window.confirm('Ștergi înregistrarea de cules? Această acțiune nu poate fi anulată.')) return
    deleteHarvestMutation.mutate()
  }

  const handleDeletePressing = () => {
    if (!window.confirm('Ștergi înregistrarea de stors? Această acțiune nu poate fi anulată.')) return
    deletePressingMutation.mutate()
  }

  if (isLoading) return <p>Se încarcă...</p>
  if (!batch) return <p>Lot negăsit.</p>

  return (
    <div>
      <h1 className="text-2xl font-bold mb-2">{batch.parcelName} — {batch.year}</h1>
      <StageTimeline
        harvest={harvest}
        pressing={pressing}
        bottling={bottling}
        onDeleteHarvest={handleDeleteHarvest}
        onDeletePressing={handleDeletePressing}
      />
      <div className="bg-white rounded-lg shadow p-6">
        {batch.status === 'HARVESTED' && !harvest && <HarvestForm batchId={id} />}
        {batch.status === 'HARVESTED' && harvest && <PressingForm batchId={id} />}
        {batch.status === 'PRESSED' && !pressing && <PressingForm batchId={id} />}
        {batch.status === 'PRESSED' && pressing && <BottlingForm batchId={id} />}
        {batch.status === 'BOTTLED' && <p className="text-green-600 font-medium">Lot finalizat.</p>}
      </div>
    </div>
  )
}
```

- [ ] **Step 3: Verify build**

```bash
cd /Users/sorin/workspace/vineyard-mnager/frontend && npm run build 2>&1 | tail -5
```

Expected: no errors.

- [ ] **Step 4: Commit**

```bash
cd /Users/sorin/workspace/vineyard-mnager && git add frontend/src && git commit -m "feat: delete buttons in StageTimeline and BatchDetail"
```

---

## Task 9: Deploy

- [ ] **Step 1: Rebuild and restart containers**

```bash
cd /Users/sorin/workspace/vineyard-mnager && docker compose up --build -d vineyard-api vineyard-ui 2>&1 | tail -10
```

- [ ] **Step 2: Wait for API to start**

```bash
until docker exec vineyard-api curl -s http://localhost:8080/api/parcels > /dev/null 2>&1; do sleep 3; done && echo "API ready"
```

- [ ] **Step 3: Smoke test delete parcel via API**

```bash
# Create a test parcel
PARCEL_ID=$(docker exec vineyard-api curl -s -X POST http://localhost:8080/api/parcels \
  -H "Content-Type: application/json" \
  -d '{"name":"TestDelete","grapeVariety":"Test","areaSqM":100}' | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
echo "Created: $PARCEL_ID"

# Delete it
docker exec vineyard-api curl -s -X DELETE http://localhost:8080/api/parcels/$PARCEL_ID -w "%{http_code}"
echo ""

# Verify gone
docker exec vineyard-api curl -s http://localhost:8080/api/parcels | python3 -c "import sys,json; d=json.load(sys.stdin); print('Parcels remaining:', len(d))"
```

Expected: `204`, parcel removed from list.

- [ ] **Step 4: Push to GitHub**

```bash
cd /Users/sorin/workspace/vineyard-mnager && git push origin main
```
