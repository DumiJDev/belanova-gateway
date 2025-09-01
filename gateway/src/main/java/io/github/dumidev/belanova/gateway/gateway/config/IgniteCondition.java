package io.github.dumidev.belanova.gateway.gateway.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class IgniteCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Check if Ignite is enabled via environment variable or property
        String igniteEnabled = context.getEnvironment().getProperty("IGNITE_CLIENT_MODE");
        return "true".equalsIgnoreCase(igniteEnabled);
    }
}