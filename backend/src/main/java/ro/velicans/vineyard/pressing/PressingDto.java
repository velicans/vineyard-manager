package ro.velicans.vineyard.pressing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PressingDto(UUID id, UUID batchId, LocalDate date, BigDecimal mustLiters, BigDecimal yieldRatio) {}
