package ro.velicans.vineyard.parcel;

import java.util.UUID;

public record ParcelDto(UUID id, String name, String grapeVariety, Integer areaSqM) {}
