package io.github.dumijdev.belanova.gateway.common.model;

public class BackendUnavailableException extends RuntimeException {
    public BackendUnavailableException(String message) {
        super(message);
    }

    public BackendUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}