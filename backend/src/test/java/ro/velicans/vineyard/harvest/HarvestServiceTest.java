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
}
