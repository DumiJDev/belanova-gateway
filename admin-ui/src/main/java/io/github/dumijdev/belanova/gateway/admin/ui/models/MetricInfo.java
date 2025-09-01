package io.github.dumijdev.belanova.gateway.admin.ui.models;

import java.time.LocalDateTime;

public record MetricInfo(
    String name,
    double value,
    String unit,
    String description,
    LocalDateTime lastUpdated
) {
    public static MetricInfo create(String name, double value, String unit, String description) {
        return new MetricInfo(name, value, unit, description, LocalDateTime.now());
    }
}
