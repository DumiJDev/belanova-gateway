package io.github.dumijdev.belanova.gateway.admin.ui.services;

import io.github.dumijdev.belanova.gateway.admin.ui.models.BackendRoute;

import java.util.List;

public interface RouteService {
  List<BackendRoute> findAll();

  void save(BackendRoute route);

  void toggle(BackendRoute r);

  void delete(BackendRoute r);
}
