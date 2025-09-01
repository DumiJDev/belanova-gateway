package io.github.dumidev.belanova.gateway.gateway.loadbalancer;

import io.github.dumijdev.belanova.gateway.common.model.Upstream;
import io.github.dumijdev.belanova.gateway.common.model.UpstreamHealthStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Component("roundRobinLoadBalancer")
@Primary
public class RoundRobinLoadBalancer implements LoadBalancer {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Optional<Upstream> select(List<Upstream> upstreams, HttpServletRequest exchange) {
        List<Upstream> healthyUpstreams = upstreams.stream()
                .filter(u -> u.isEnabled() && u.getStatus() == UpstreamHealthStatus.HEALTHY)
                .toList();

        if (healthyUpstreams.isEmpty()) {
            return Optional.empty();
        }

        int index = counter.getAndIncrement() % healthyUpstreams.size();
        return Optional.of(healthyUpstreams.get(index));
    }
}
