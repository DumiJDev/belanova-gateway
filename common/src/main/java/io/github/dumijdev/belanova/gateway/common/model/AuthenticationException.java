package io.github.dumijdev.belanova.gateway.common.model;

public class AuthenticationException extends PluginException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}