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
