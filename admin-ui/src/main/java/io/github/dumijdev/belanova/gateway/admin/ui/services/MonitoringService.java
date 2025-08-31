package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.admin.ui.models.RouteMetrics;

public interface MonitoringService {
  void check(String routeId);
  RouteMetrics fetchMetrics();
}
