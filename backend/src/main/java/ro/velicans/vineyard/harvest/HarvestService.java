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
