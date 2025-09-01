package io.github.dumijdev.belanova.gateway.common.model;

public class PluginExecutionException extends RuntimeException {
    public PluginExecutionException(String message) {
        super(message);
    }

    public PluginExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}