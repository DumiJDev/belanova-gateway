package io.github.dumijdev.belanova.gateway.common.model;

import lombok.Builder;
import lombok.Data;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class GatewayMvcContext {
    private Object request;
    private Object response;
    private Backend backend;
    private Service service;
    private Exception error;
    private Map<String, Object> attributes;
    private Principal principal;

    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return attributes != null ? (T) attributes.get(key) : null;
    }
}