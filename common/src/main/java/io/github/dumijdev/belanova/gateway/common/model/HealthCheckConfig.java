package io.github.dumijdev.belanova.gateway.common.model;

import lombok.Data;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class HealthCheckConfig {
    private String healthPath;
    private int timeoutMs;
    private int intervalMs;
    private List<Integer> expectedStatusCodes;
    private Map<String, String> expectedHeaders;
    private boolean enabled;

    public HealthCheckConfig() {
        this.healthPath = "/health";
        this.timeoutMs = 5000;
        this.intervalMs = 30000;
        this.expectedStatusCodes = List.of(200, 201, 202, 203, 204);
        this.expectedHeaders = Map.of();
        this.enabled = true;
    }

    public HealthCheckConfig(String healthPath, int timeoutMs, int intervalMs,
                           List<Integer> expectedStatusCodes, Map<String, String> expectedHeaders,
                           boolean enabled) {
        this.healthPath = healthPath != null ? healthPath : "/health";
        this.timeoutMs = timeoutMs > 0 ? timeoutMs : 5000;
        this.intervalMs = intervalMs > 0 ? intervalMs : 30000;
        this.expectedStatusCodes = expectedStatusCodes != null ? expectedStatusCodes :
                                 List.of(200, 201, 202, 203, 204);
        this.expectedHeaders = expectedHeaders != null ? expectedHeaders : Map.of();
        this.enabled = enabled;
    }
}
