package ro.velicans.vineyard.reports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.velicans.vineyard.batch.BatchStatus;
import ro.velicans.vineyard.batch.ProductionBatch;
import ro.velicans.vineyard.bottling.BottleVolume;
import ro.velicans.vineyard.bottling.Bottling;
import ro.velicans.vineyard.bottling.BottlingRepository;
import ro.velicans.vineyard.harvest.Harvest;
import ro.velicans.vineyard.harvest.HarvestRepository;
import ro.velicans.vineyard.parcel.Parcel;
import ro.velicans.vineyard.pressing.Pressing;
import ro.velicans.vineyard.pressing.PressingRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock HarvestRepository harvestRepo;
    @Mock PressingRepository pressingRepo;
    @Mock BottlingRepository bottlingRepo;
    @InjectMocks ReportService service;

    private ProductionBatch batchFor(String parcelName, int year) {
        Parcel p = new Parcel();
        p.setId(UUID.randomUUID());
        p.setName(parcelName);
        p.setGrapeVariety("Feteasca");
        p.setAreaSqM(1000);
        ProductionBatch b = new ProductionBatch();
        b.setId(UUID.randomUUID());
        b.setParcel(p);
        b.setYear(year);
        b.setStatus(BatchStatus.BOTTLED);
        return b;
    }

    @Test
    void harvestReport_aggregatesByParcelAndYear() {
        ProductionBatch b = batchFor("Nord", 2024);
        Harvest h = new Harvest();
        h.setBatch(b);
        h.setDate(LocalDate.of(2024, 9, 10));
        h.setQuantityKg(new BigDecimal("500.00"));
        when(harvestRepo.findAll()).thenReturn(List.of(h));

        List<HarvestReportDto> result = service.harvestReport(2024);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).totalKg()).isEqualByComparingTo("500.00");
        assertThat(result.get(0).parcelName()).isEqualTo("Nord");
    }

    @Test
    void bottlingReport_countsByVolume() {
        ProductionBatch b = batchFor("Nord", 2024);
        Bottling bt = new Bottling();
        bt.setBatch(b);
        bt.setDate(LocalDate.of(2024, 11, 1));
        bt.setBottleCount(400);
        bt.setBottleVolume(BottleVolume.L075);
        when(bottlingRepo.findAll()).thenReturn(List.of(bt));

        List<BottlingReportDto> result = service.bottlingReport(2024);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).totalBottles()).isEqualTo(400);
        assertThat(result.get(0).bottleVolume()).isEqualTo(BottleVolume.L075);
    }
}
