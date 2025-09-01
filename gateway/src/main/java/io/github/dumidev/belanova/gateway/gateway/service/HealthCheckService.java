package io.github.dumidev.belanova.gateway.gateway.service;

import io.github.dumijdev.belanova.gateway.common.model.HealthStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class HealthCheckService {

    private final RestClient restClient;
    private final Map<String, HealthStatus> healthStatusMap;

    public HealthCheckService() {
        this.restClient = RestClient.create();
        this.healthStatusMap = new ConcurrentHashMap<>();
    }

    @Scheduled(fixedDelay = 30000) // Run every 30 seconds
    public void performHealthChecks() {
        // In a real implementation, this would iterate through configured backends
        // For now, we'll simulate health checks
        log.debug("Performing health checks...");

        // Simulate health check for a test backend
        checkBackendHealth("test-backend", "http://localhost:8081/health");
    }

    private void checkBackendHealth(String backendId, String healthUrl) {
        try {
            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restClient
                    .get()
                    .uri(healthUrl)
                    .retrieve()
                    .toEntity(String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            boolean isHealthy = isHealthyResponse(response);

            HealthStatus status = HealthStatus.builder()
                    .backendId(backendId)
                    .healthy(isHealthy)
                    .responseTime(responseTime)
                    .statusCode(response.getStatusCode().value())
                    .lastChecked(Instant.now())
                    .message(isHealthy ? "OK" : "Health check failed")
                    .build();

            healthStatusMap.put(backendId, status);

            log.debug("Health check for backend {}: {} ({}ms)",
                     backendId, isHealthy ? "HEALTHY" : "UNHEALTHY", responseTime);

        } catch (Exception e) {
            HealthStatus status = HealthStatus.builder()
                    .backendId(backendId)
                    .healthy(false)
                    .responseTime(-1L)
                    .statusCode(-1)
                    .lastChecked(Instant.now())
                    .message(e.getMessage())
                    .build();

            healthStatusMap.put(backendId, status);

            log.warn("Health check failed for backend {}: {}", backendId, e.getMessage());
        }
    }

    private boolean isHealthyResponse(ResponseEntity<String> response) {
        // Check status code - consider 200-299 as healthy
        return response.getStatusCode().is2xxSuccessful();
    }

    public HealthStatus getBackendHealth(String backendId) {
        return healthStatusMap.get(backendId);
    }

    public Map<String, HealthStatus> getAllHealthStatuses() {
        return new HashMap<>(healthStatusMap);
    }
}