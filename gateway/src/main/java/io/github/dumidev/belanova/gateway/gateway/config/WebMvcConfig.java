package io.github.dumidev.belanova.gateway.gateway.config;

import io.github.dumidev.belanova.gateway.gateway.loadbalancer.LoadBalancingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final LoadBalancingInterceptor loadBalancingInterceptor;

  public WebMvcConfig(LoadBalancingInterceptor loadBalancingInterceptor) {
    this.loadBalancingInterceptor = loadBalancingInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loadBalancingInterceptor)
        .order(loadBalancingInterceptor.getOrder());
  }
}