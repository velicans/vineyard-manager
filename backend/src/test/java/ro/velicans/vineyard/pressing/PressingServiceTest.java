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

        when(repo.findByBatchId(batchId)).thenReturn(Optional.empty());

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
}
