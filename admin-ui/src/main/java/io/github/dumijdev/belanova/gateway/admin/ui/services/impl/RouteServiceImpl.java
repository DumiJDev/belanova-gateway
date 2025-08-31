package io.github.dumijdev.belanova.gateway.admin.ui.services.impl;

import io.github.dumijdev.belanova.gateway.admin.ui.models.BackendRoute;
import io.github.dumijdev.belanova.gateway.admin.ui.models.RouteInfo;
import io.github.dumijdev.belanova.gateway.admin.ui.services.RouteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteServiceImpl implements RouteService {
  @Override
  public List<BackendRoute> findAll() {
    return List.of();
  }

  @Override
  public void save(BackendRoute route) {

  }

  @Override
  public void toggle(BackendRoute r) {

  }

  @Override
  public void delete(BackendRoute r) {

  }

  @Override
  public void refreshAllRoutes() {

  }

  @Override
  public void clearCache() {

  }

  @Override
  public List<RouteInfo> getAllRoutes() {
    return List.of();
  }

  @Override
  public void refreshRoute(String routeId) {

  }

  @Override
  public boolean testRoute(String routeId) {
    return false;
  }
}
