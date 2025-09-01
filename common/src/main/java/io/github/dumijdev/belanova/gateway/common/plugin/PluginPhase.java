package io.github.dumijdev.belanova.gateway.common.plugin;

public enum PluginPhase {
    PRE_AUTH,
    AUTH,
    POST_AUTH,
    PRE_REQUEST,
    ROUTING,
    POST_ROUTING,
    POST_REQUEST,
    ERROR
}