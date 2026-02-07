package com.ican.saf.ui.dsahboard.bff.spi.model;

import org.springframework.cloud.gateway.config.GatewayProperties;

import java.util.Optional;

public interface RouteTransformer {
    String to(String url);
    String getAccessToken();
    String getApiKey();
    Optional<String> to(String id, String path, GatewayProperties gatewayProperties);
}
