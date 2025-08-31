package io.github.dumijdev.belanova.gateway.admin.ui.models;

public record GatewayStats(
    int totalBackends,
    int activeServices,
    double requestsPerMinute,
    double averageResponseTime
) {
}
