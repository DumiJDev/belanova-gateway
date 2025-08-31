package io.github.dumidev.belanova.gateway.gateway.loadbalancer;

import io.github.dumijdev.belanova.gateway.common.model.Upstream;
import io.github.dumijdev.belanova.gateway.common.model.UpstreamHealthStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component("leastConnectionsLoadBalancer")
public class LeastConnectionsLoadBalancer implements LoadBalancer {
  private final ConcurrentHashMap<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();

  @Override
  public Optional<Upstream> select(List<Upstream> upstreams, HttpServletRequest exchange) {
    List<Upstream> healthyUpstreams = upstreams.stream()
        .filter(u -> u.isEnabled() && u.getStatus() == UpstreamHealthStatus.HEALTHY)
        .toList();
    if (healthyUpstreams.isEmpty()) {
      return Optional.empty();
    }
    Upstream selected = healthyUpstreams.stream()
        .min(Comparator.comparingInt(u -> connectionCounts.computeIfAbsent(u.getId(), k -> new AtomicInteger(0)).get()))
        .orElse(healthyUpstreams.getFirst());
    connectionCounts.computeIfAbsent(selected.getId(), k -> new AtomicInteger(0)).incrementAndGet();
    return Optional.of(selected);
  }

  public void releaseConnection(String upstreamId) {
    connectionCounts.computeIfPresent(upstreamId, (k, v) -> {
      v.decrementAndGet();
      return v;
    });
  }
}
