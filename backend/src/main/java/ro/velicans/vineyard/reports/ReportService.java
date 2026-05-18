package ro.velicans.vineyard.reports;

import org.springframework.stereotype.Service;
import ro.velicans.vineyard.bottling.BottleVolume;
import ro.velicans.vineyard.bottling.Bottling;
import ro.velicans.vineyard.bottling.BottlingRepository;
import ro.velicans.vineyard.harvest.Harvest;
import ro.velicans.vineyard.harvest.HarvestRepository;
import ro.velicans.vineyard.pressing.Pressing;
import ro.velicans.vineyard.pressing.PressingRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final HarvestRepository harvestRepo;
    private final PressingRepository pressingRepo;
    private final BottlingRepository bottlingRepo;

    public ReportService(HarvestRepository harvestRepo, PressingRepository pressingRepo, BottlingRepository bottlingRepo) {
        this.harvestRepo = harvestRepo;
        this.pressingRepo = pressingRepo;
        this.bottlingRepo = bottlingRepo;
    }

    public List<HarvestReportDto> harvestReport(Integer year) {
        return harvestRepo.findAll().stream()
            .filter(h -> year == null || h.getBatch().getYear().equals(year))
            .collect(Collectors.groupingBy(h -> h.getBatch().getParcel()))
            .entrySet().stream()
            .map(e -> {
                var parcel = e.getKey();
                var harvests = e.getValue();
                BigDecimal total = harvests.stream().map(Harvest::getQuantityKg).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avg = total.divide(BigDecimal.valueOf(harvests.size()), 2, RoundingMode.HALF_UP);
                return new HarvestReportDto(parcel.getId(), parcel.getName(), year, total, avg);
            })
            .toList();
    }

    public List<PressingReportDto> pressingReport(Integer year) {
        return pressingRepo.findAll().stream()
            .filter(p -> year == null || p.getBatch().getYear().equals(year))
            .collect(Collectors.groupingBy(p -> p.getBatch().getParcel()))
            .entrySet().stream()
            .map(e -> {
                var parcel = e.getKey();
                var pressings = e.getValue();
                BigDecimal totalLiters = pressings.stream().map(Pressing::getMustLiters).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal avgYield = pressings.stream().map(Pressing::getYieldRatio).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(pressings.size()), 4, RoundingMode.HALF_UP);
                return new PressingReportDto(parcel.getId(), parcel.getName(), year, totalLiters, avgYield);
            })
            .toList();
    }

    public List<BottlingReportDto> bottlingReport(Integer year) {
        return bottlingRepo.findAll().stream()
            .filter(b -> year == null || b.getBatch().getYear().equals(year))
            .collect(Collectors.groupingBy(Bottling::getBottleVolume))
            .entrySet().stream()
            .map(e -> {
                BottleVolume vol = e.getKey();
                int total = e.getValue().stream().mapToInt(Bottling::getBottleCount).sum();
                return new BottlingReportDto(year, vol, total);
            })
            .toList();
    }
}
