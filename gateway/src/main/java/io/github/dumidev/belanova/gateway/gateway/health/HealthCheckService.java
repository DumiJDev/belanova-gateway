package io.github.dumidev.belanova.gateway.gateway.health;

import io.github.dumijdev.belanova.gateway.common.model.Backend;
import io.github.dumijdev.belanova.gateway.common.model.Upstream;
import io.github.dumijdev.belanova.gateway.common.model.UpstreamHealthStatus;
import io.github.dumidev.belanova.gateway.gateway.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.cache.Cache;
import java.util.List;

@Service
@Slf4j
public class HealthCheckService {

  private final CacheService cacheService;
  private final RestTemplate restTemplate;
  private final Ignite ignite;
  private final String backendCacheName;

  public HealthCheckService(CacheService cacheService,
                            RestTemplate restTemplate,
                            Ignite ignite,
                            @Value("${belanova.gateway.cache.ignite.backend-cache-name:backends}") String backendCacheName) {
    this.cacheService = cacheService;
    this.restTemplate = restTemplate;
    this.ignite = ignite;
    this.backendCacheName = backendCacheName;
  }

  @Scheduled(fixedRateString = "${belanova.gateway.health-check.interval:10000}")
  public void performHealthChecks() {
    List<Backend> backends = cacheService.getBackends();

    for (Backend backend : backends) {
      if (!backend.isEnabled()) continue;

      for (Upstream upstream : backend.getUpstreams()) {
        if (!upstream.isEnabled()) continue;

        checkUpstreamHealth(backend, upstream);
      }
    }
  }

  private void checkUpstreamHealth(Backend backend, Upstream upstream) {
    if (backend.getHealthCheck() == null || backend.getHealthCheck().getHealthPath() == null) {
      return;
    }

    String url = "http://" + upstream.getHost() + ":" + upstream.getPort() + backend.getHealthCheck().getHealthPath();

    try {
      org.springframework.http.ResponseEntity<Void> response =
          restTemplate.getForEntity(url, Void.class);

      if (backend.getHealthCheck().getExpectedStatusCodes() != null &&
          backend.getHealthCheck().getExpectedStatusCodes().contains(response.getStatusCode().value())) {
        updateUpstreamStatus(backend, upstream, UpstreamHealthStatus.HEALTHY);
      } else {
        updateUpstreamStatus(backend, upstream, UpstreamHealthStatus.UNHEALTHY);
      }
    } catch (Exception ex) {
      log.warn("Health check for upstream {} failed: {}", upstream.getHost(), ex.getMessage());
      updateUpstreamStatus(backend, upstream, UpstreamHealthStatus.UNHEALTHY);
    }
  }

  private void updateUpstreamStatus(Backend backend, Upstream upstream, UpstreamHealthStatus status) {
    IgniteCache<String, Backend> cache = ignite.cache(backendCacheName);

    // This is not atomic and can lead to race conditions.
    // A better approach would be to use Ignite's EntryProcessor.
    Backend currentBackend = cache.get(backend.getId());
    if (currentBackend != null) {
      currentBackend.getUpstreams().stream()
          .filter(u -> u.getId().equals(upstream.getId()))
          .findFirst()
          .ifPresent(u -> u.setStatus(status));
      cache.put(backend.getId(), currentBackend);
    }
  }
}
