package io.github.dumijdev.belanova.gateway.common.plugin;

import io.github.dumijdev.belanova.gateway.common.model.ConfigurationException;
import io.github.dumijdev.belanova.gateway.common.model.GatewayMvcContext;
import io.github.dumijdev.belanova.gateway.common.model.PluginException;

import java.util.Map;

public interface GatewayPlugin {

    /**
     * Plugin name
     */
    String getName();

    /**
     * Plugin execution order (lower values execute first)
     */
    int getOrder();

    /**
     * Whether plugin is enabled
     */
    boolean isEnabled();

    /**
     * Plugin execution phase
     */
    PluginPhase getPhase();

    /**
     * Execute plugin logic
     */
    void execute(GatewayMvcContext context) throws PluginException;

    /**
     * Plugin configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Validate plugin configuration
     */
    void validateConfiguration(Map<String, Object> config) throws ConfigurationException;
}