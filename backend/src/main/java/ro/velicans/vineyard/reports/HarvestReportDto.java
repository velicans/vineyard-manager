package ro.velicans.vineyard.reports;

import java.math.BigDecimal;
import java.util.UUID;

public record HarvestReportDto(UUID parcelId, String parcelName, Integer year, BigDecimal totalKg, BigDecimal avgKg) {}
