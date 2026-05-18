package ro.velicans.vineyard.bottling;

import java.time.LocalDate;
import java.util.UUID;

public record BottlingDto(UUID id, UUID batchId, LocalDate date, Integer bottleCount, BottleVolume bottleVolume) {}
