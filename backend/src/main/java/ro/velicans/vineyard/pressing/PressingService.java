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
