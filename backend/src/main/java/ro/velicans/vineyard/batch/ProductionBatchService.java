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
