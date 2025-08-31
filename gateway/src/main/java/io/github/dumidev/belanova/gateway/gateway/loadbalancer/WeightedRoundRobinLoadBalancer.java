package io.github.dumidev.belanova.gateway.gateway.loadbalancer;

import io.github.dumijdev.belanova.gateway.common.model.Upstream;
import io.github.dumijdev.belanova.gateway.common.model.UpstreamHealthStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component("weightedRoundRobinLoadBalancer")
public class WeightedRoundRobinLoadBalancer implements LoadBalancer {
    private final Map<String, AtomicInteger> counters = new HashMap<>();

    @Override
    public Optional<Upstream> select(List<Upstream> upstreams, HttpServletRequest exchange) {
        List<Upstream> healthyUpstreams = upstreams.stream()
                .filter(u -> u.isEnabled() && u.getStatus() == UpstreamHealthStatus.HEALTHY)
                .toList();
        if (healthyUpstreams.isEmpty()) {
            return Optional.empty();
        }
        List<Upstream> weightedList = new ArrayList<>();
        for (Upstream u : healthyUpstreams) {
            for (int i = 0; i < Math.max(1, u.getWeight()); i++) {
                weightedList.add(u);
            }
        }
        String key = exchange.getServletPath();
        counters.putIfAbsent(key, new AtomicInteger(0));
        int index = counters.get(key).getAndIncrement() % weightedList.size();
        return Optional.of(weightedList.get(index));
    }
}
