package io.github.dumijdev.belanova.gateway.common.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class HealthStatus {
    private String backendId;
    private boolean healthy;
    private long responseTime;
    private int statusCode;
    private Instant lastChecked;
    private String message;
}