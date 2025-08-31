
package io.github.dumidev.belanova.gateway.gateway.service.impl;

import io.github.dumijdev.belanova.gateway.common.model.Backend;
import io.github.dumidev.belanova.gateway.gateway.service.CacheService;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class IgniteCacheService implements CacheService {

    private final Ignite ignite;
    private final String backendCacheName;

    public IgniteCacheService(Ignite ignite,
                              @Value("${belanova.gateway.cache.ignite.backend-cache-name:backends}") String backendCacheName) {
        this.ignite = ignite;
        this.backendCacheName = backendCacheName;
    }

    @Override
    public List<Backend> getBackends() {
        IgniteCache<String, Backend> cache = ignite.cache(backendCacheName);
        return StreamSupport.stream(cache.spliterator(), false)
                .map(Cache.Entry::getValue)
                .collect(Collectors.toList());
    }
}
