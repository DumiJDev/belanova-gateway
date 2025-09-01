package io.github.dumijdev.belanova.gateway.admin.ui.services.impl;

import io.github.dumijdev.belanova.gateway.admin.ui.models.BackendHealthStatus;
import io.github.dumijdev.belanova.gateway.admin.ui.models.GatewayStats;
import io.github.dumijdev.belanova.gateway.admin.ui.models.RequestMetrics;
import io.github.dumijdev.belanova.gateway.admin.ui.services.DashboardService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DashboardServiceImpl implements DashboardService {
  @Override
  public GatewayStats getGatewayStats() {
    return new GatewayStats(
        4,
        5,
        400.0,
        100.0
    );
  }

  @Override
  public List<RequestMetrics> getRequestMetrics() {
    return List.of();
  }

  @Override
  public BackendHealthStatus getBackendHealthStatus() {
    return new BackendHealthStatus(
        "test-backend",
        "Test Backend",
        "healthy",
        100L,
        2,
        2,
        LocalDateTime.now()
    );
  }
}
