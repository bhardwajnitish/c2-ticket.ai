package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AppCorsProperties corsProperties;

    public WebConfig(AppCorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(
                "/api/v1",
                handlerType -> handlerType.isAnnotationPresent(RestController.class)
                        && (handlerType.getPackageName().startsWith("com.example.ticket.api")
                                || handlerType.getPackageName().startsWith("com.example.rag.api")));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOrigins(corsProperties.allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}