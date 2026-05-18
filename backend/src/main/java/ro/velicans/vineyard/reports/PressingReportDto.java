package ro.velicans.vineyard.reports;

import java.math.BigDecimal;
import java.util.UUID;

public record PressingReportDto(UUID parcelId, String parcelName, Integer year, BigDecimal totalMustLiters, BigDecimal avgYieldRatio) {}
