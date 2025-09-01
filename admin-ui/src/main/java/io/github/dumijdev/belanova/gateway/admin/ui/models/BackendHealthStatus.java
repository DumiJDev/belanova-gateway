package io.github.dumijdev.belanova.gateway.admin.ui.models;

import java.time.LocalDateTime;

public record BackendHealthStatus(
    String backendId,
    String name,
    String status,
    long responseTime,
    int healthyUpstreams,
    int totalUpstreams,
    LocalDateTime lastChecked
) {
    public static BackendHealthStatus create(String backendId, String name) {
        return new BackendHealthStatus(backendId, name, "healthy", 150, 3, 3, LocalDateTime.now());
    }

    public BackendHealthStatus withStatus(String newStatus) {
        return new BackendHealthStatus(backendId, name, newStatus, responseTime, healthyUpstreams, totalUpstreams, LocalDateTime.now());
    }
}
