package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.admin.ui.models.BackendRoute;
import io.github.dumijdev.belanova.gateway.admin.ui.models.RouteInfo;

import java.util.List;

public interface RouteService {
  List<BackendRoute> findAll();

  void save(BackendRoute route);

  void toggle(BackendRoute r);

  void delete(BackendRoute r);

  void refreshAllRoutes();

  void clearCache();

  List<RouteInfo> getAllRoutes();

  void refreshRoute(String routeId);

  boolean testRoute(String routeId);
}
