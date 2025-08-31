package io.github.dumijdev.belanova.gateway.common.plugin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public interface GatewayPlugin {
    String getName();
    int getOrder();
    boolean isEnabled();

    /**
     * Processa a requisição no contexto do Spring MVC
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return true se o processamento deve continuar, false para interromper
     */
    boolean apply(HttpServletRequest request, HttpServletResponse response);

    Map<String, Object> getConfiguration();
}