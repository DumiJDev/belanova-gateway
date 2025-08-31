package io.github.dumijdev.belanova.gateway.admin.ui.models;

import java.time.LocalDateTime;

public record BackendHealthStatus(
    String name,
    String healthyUpstreams,
    String status,
    double responseTime,
    LocalDateTime lastChecked
) {
}
