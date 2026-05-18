package ro.velicans.vineyard.harvest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record HarvestDto(UUID id, UUID batchId, LocalDate date, BigDecimal quantityKg) {}
