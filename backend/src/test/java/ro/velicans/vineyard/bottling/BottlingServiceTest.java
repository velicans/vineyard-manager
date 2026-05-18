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
        when(repo.findByBatchId(batchId)).thenReturn(Optional.empty());

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
