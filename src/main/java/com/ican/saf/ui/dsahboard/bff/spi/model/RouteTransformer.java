package com.ican.saf.ui.dsahboard.bff.spi.model;

import org.springframework.cloud.gateway.server.mvc.config.GatewayMvcProperties;

import java.util.Optional;

public interface RouteTransformer {
    String to(String url);
    String getAccessToken();
    String getApiKey();
    Optional<String> to(String id, String path, GatewayMvcProperties gatewayProperties);
}
