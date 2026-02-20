package com.aegis.api_platform.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayFilterConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Bean
    public FilterRegistrationBean<ApiKeyAuthenticationFilter> apiKeyFilter() {
        FilterRegistrationBean<ApiKeyAuthenticationFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(apiKeyAuthenticationFilter);
        registration.addUrlPatterns("/gateway/*");
        registration.setOrder(1);

        return registration;
    }
}
