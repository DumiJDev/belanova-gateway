package io.github.dumidev.belanova.gateway.gateway.loadbalancer;

import io.github.dumijdev.belanova.gateway.common.model.Upstream;
import io.github.dumijdev.belanova.gateway.common.model.UpstreamHealthStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component("randomLoadBalancer")
public class RandomLoadBalancer implements LoadBalancer {
    private final Random random = new Random();

    @Override
    public Optional<Upstream> select(List<Upstream> upstreams, HttpServletRequest exchange) {
        List<Upstream> healthyUpstreams = upstreams.stream()
                .filter(u -> u.isEnabled() && u.getStatus() == UpstreamHealthStatus.HEALTHY)
                .toList();
        if (healthyUpstreams.isEmpty()) {
            return Optional.empty();
        }
        int index = random.nextInt(healthyUpstreams.size());
        return Optional.of(healthyUpstreams.get(index));
    }
}
