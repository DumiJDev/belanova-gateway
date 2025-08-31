package io.github.dumijdev.belanova.gateway.admin.ui.models;

public record RequestMetrics(
    long requestCount,
    double averageResponseTime
) {
}
