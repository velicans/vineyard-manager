# Vineyard Manager — CRUD Delete & Add Parcels Design

**Date:** 2026-05-18

---

## 1. Overview

Add missing create/delete operations to the Vineyard Manager UI and backend. Currently only batch creation and stage recording exist. This adds parcel creation, and delete for parcels, batches, harvest records, and pressing records.

---

## 2. Backend Changes

### New DELETE endpoints

```
DELETE /api/parcels/{id}
DELETE /api/batches/{id}
DELETE /api/batches/{batchId}/harvest
DELETE /api/batches/{batchId}/pressing
```

### Cascade behavior (service layer, not DB FK CASCADE)

**DELETE /api/parcels/{id}:**
1. Find all batches for this parcel
2. For each batch: delete bottling, pressing, harvest if they exist
3. Delete all batches
4. Delete the parcel

**DELETE /api/batches/{id}:**
1. Delete bottling if exists
2. Delete pressing if exists
3. Delete harvest if exists
4. Delete the batch

**DELETE /api/batches/{batchId}/harvest:**
1. Delete the harvest record
2. Batch status stays HARVESTED (unchanged — batch was HARVESTED when created)

**DELETE /api/batches/{batchId}/pressing:**
1. Delete the pressing record
2. Set batch status back to HARVESTED

All DELETE endpoints return `204 No Content` on success. Return `404` if the resource doesn't exist.

### Files to modify

- `ParcelService.java` — add `delete(UUID id)`
- `ParcelController.java` — add `DELETE /api/parcels/{id}`
- `ProductionBatchService.java` — add `delete(UUID id)`, `deleteByParcelId(UUID parcelId)`
- `ProductionBatchController.java` — add `DELETE /api/batches/{id}`
- `HarvestService.java` — add `delete(UUID batchId)`
- `HarvestController.java` — add `DELETE /api/batches/{batchId}/harvest`
- `PressingService.java` — add `delete(UUID batchId)`
- `PressingController.java` — add `DELETE /api/batches/{batchId}/pressing`

Also needed:
- `BottlingRepository.java` — add `deleteByBatchId(UUID batchId)`
- `PressingRepository.java` — add `deleteByBatchId(UUID batchId)`
- `HarvestRepository.java` — add `deleteByBatchId(UUID batchId)`
- `ProductionBatchRepository.java` — add `deleteByParcelId(UUID parcelId)` (find first, then delete each)

---

## 3. Frontend Changes

### API layer (`frontend/src/api/`)

**parcels.js** — add:
```js
export const deleteParcel = (id) => client.delete(`/parcels/${id}`)
export const createParcel = (data) => client.post('/parcels', data).then(r => r.data)  // already exists
```

**batches.js** — add:
```js
export const deleteBatch = (id) => client.delete(`/batches/${id}`)
export const deleteHarvest = (batchId) => client.delete(`/batches/${batchId}/harvest`)
export const deletePressing = (batchId) => client.delete(`/batches/${batchId}/pressing`)
```

### Pages / Components

**ParcelList.jsx:**
- Add `+ Parcelă nouă` button that toggles an inline form
- Form fields: name (text), grapeVariety (text), areaSqM (number)
- On submit: POST via `createParcel`, invalidate `['parcels']` query, hide form
- Each parcel card gets a `Șterge` button
- On delete: `window.confirm(...)` → DELETE via `deleteParcel`, invalidate `['parcels']`

**BatchList.jsx:**
- Each batch row gets a `Șterge` button
- On delete: `window.confirm(...)` → DELETE via `deleteBatch`, invalidate `['batches']`

**StageTimeline.jsx:**
- Accept `onDeleteHarvest` and `onDeletePressing` callback props
- When harvest data exists: show `Șterge` button next to harvest stage box
- When pressing data exists: show `Șterge` button next to pressing stage box

**BatchDetail.jsx:**
- Pass delete handlers to `StageTimeline`
- `onDeleteHarvest`: `window.confirm(...)` → DELETE harvest, invalidate batch + harvest queries
- `onDeletePressing`: `window.confirm(...)` → DELETE pressing, invalidate batch + pressing queries

---

## 4. Confirmation Pattern

All destructive actions use `window.confirm("Ești sigur? Această acțiune nu poate fi anulată.")`. No custom modal. If confirm returns false, no action is taken.

---

## 5. Constraints

- Bottling delete is NOT included (not requested)
- No undo — deletes are permanent
- Parcel delete cascades through all child data silently (no listing of what will be deleted)
