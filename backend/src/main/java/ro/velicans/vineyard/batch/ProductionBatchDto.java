package ro.velicans.vineyard.batch;

import java.util.UUID;

public record ProductionBatchDto(UUID id, UUID parcelId, String parcelName, Integer year, BatchStatus status) {}
