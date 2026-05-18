package ro.velicans.vineyard.reports;

import ro.velicans.vineyard.bottling.BottleVolume;

public record BottlingReportDto(Integer year, BottleVolume bottleVolume, Integer totalBottles) {}
