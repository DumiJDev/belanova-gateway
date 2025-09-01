package io.github.dumidev.belanova.gateway.gateway.service.impl;

import io.github.dumijdev.belanova.gateway.common.model.Backend;
import io.github.dumijdev.belanova.gateway.common.model.Service;
import io.github.dumidev.belanova.gateway.gateway.service.CacheService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@org.springframework.stereotype.Service
public class InMemoryCacheService implements CacheService {

    private final ConcurrentMap<String, Backend> backendCache = new ConcurrentHashMap<>();

    // Initialize with some sample data for testing
    public InMemoryCacheService() {
        // Add some sample backends for testing
        Backend sampleBackend = new Backend();
        sampleBackend.setId("sample-backend");
        sampleBackend.setName("Sample Backend");
        sampleBackend.setBaseUrl("http://httpbin.org");
        sampleBackend.setEnabled(true);
        sampleBackend.setUseServiceDiscovery(false);

        // Create sample services
        Service sampleService = new Service();
        sampleService.setId("sample-service");
        sampleService.setName("Sample Service");
        sampleService.setPath("/api/**");
        sampleService.setEnabled(true);
        sampleService.setMethods(Arrays.asList("GET", "POST"));
        sampleService.setBackend(sampleBackend);

        // Ensure services list is initialized
        sampleBackend.setServices(new ArrayList<>(Arrays.asList(sampleService)));

        backendCache.put(sampleBackend.getId(), sampleBackend);
    }

    @Override
    public List<Backend> getBackends() {
        return new ArrayList<>(backendCache.values());
    }

    public void addBackend(Backend backend) {
        backendCache.put(backend.getId(), backend);
    }

    public void removeBackend(String backendId) {
        backendCache.remove(backendId);
    }

    public Backend getBackend(String backendId) {
        return backendCache.get(backendId);
    }
}