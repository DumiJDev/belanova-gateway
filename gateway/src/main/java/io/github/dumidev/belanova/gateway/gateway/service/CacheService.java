package io.github.dumidev.belanova.gateway.gateway.service;

import io.github.dumijdev.belanova.gateway.common.model.Backend;

import java.util.List;

public interface CacheService {
  List<Backend> getBackends();
}
