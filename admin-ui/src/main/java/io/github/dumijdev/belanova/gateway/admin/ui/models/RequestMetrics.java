package io.github.dumijdev.belanova.gateway.admin.ui.models;

import java.time.LocalDateTime;

public record RequestMetrics(
    LocalDateTime timestamp,
    int requestCount,
    double averageResponseTime,
    int errorCount
) {
}
