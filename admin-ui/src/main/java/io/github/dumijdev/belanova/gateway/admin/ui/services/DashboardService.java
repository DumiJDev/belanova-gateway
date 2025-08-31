package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.admin.ui.models.BackendHealthStatus;
import io.github.dumijdev.belanova.gateway.admin.ui.models.GatewayStats;
import io.github.dumijdev.belanova.gateway.admin.ui.models.RequestMetrics;

import java.util.List;

public interface DashboardService {
  GatewayStats getGatewayStats();

  List<RequestMetrics> getRequestMetrics();

  BackendHealthStatus getBackendHealthStatus();
}
