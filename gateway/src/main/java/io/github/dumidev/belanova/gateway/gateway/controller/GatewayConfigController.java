package io.github.dumidev.belanova.gateway.gateway.controller;

import io.github.dumijdev.belanova.gateway.common.model.Backend;
import io.github.dumidev.belanova.gateway.gateway.service.CacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gateway/config")
public class GatewayConfigController {
  private final CacheService cacheService;

  public GatewayConfigController(CacheService cacheService) {
    this.cacheService = cacheService;
  }

  @GetMapping("/backends")
  public ResponseEntity<List<Backend>> getBackends() {
    return ResponseEntity.ok(cacheService.getBackends());
  }
}
