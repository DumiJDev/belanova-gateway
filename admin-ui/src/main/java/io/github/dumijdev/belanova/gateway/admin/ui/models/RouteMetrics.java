package io.github.dumijdev.belanova.gateway.admin.ui.models;

import java.time.LocalDateTime;

public record RouteMetrics(
    String routeId,
    String routeName,
    String status,
    Integer responseTime,
    Double successRate,
    LocalDateTime lastCheck,
    Double uptime) {
}