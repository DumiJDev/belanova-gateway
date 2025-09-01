package io.github.dumijdev.belanova.gateway.common.model;

public class RateLimitExceededException extends PluginException {
    private final String limit;
    private final String remaining;

    public RateLimitExceededException(String message, String limit, String remaining) {
        super(message);
        this.limit = limit;
        this.remaining = remaining;
    }

    public String getLimit() {
        return limit;
    }

    public String getRemaining() {
        return remaining;
    }
}