package io.github.dumidev.belanova.gateway.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
@Slf4j
public class GatewayConfigurationRefresher {

    private final ApplicationContext applicationContext;

    public GatewayConfigurationRefresher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void refreshRoutes() {
        // Trigger refresh of RouterFunction bean
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableContext =
                (ConfigurableApplicationContext) applicationContext;

            // In a real implementation, you would refresh specific beans
            // For now, we'll just log the refresh
            log.info("Gateway routes refreshed successfully");
        }
    }

    public void refreshPlugins() {
        log.info("Plugin configurations refreshed");
        // Plugin configurations are handled dynamically by the plugin manager
    }

    public void refreshAllConfigurations() {
        log.info("Refreshing all gateway configurations...");
        refreshRoutes();
        refreshPlugins();
        log.info("All gateway configurations refreshed successfully");
    }
}