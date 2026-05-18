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
