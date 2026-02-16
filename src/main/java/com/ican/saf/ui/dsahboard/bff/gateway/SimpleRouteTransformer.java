package com.ican.saf.ui.dsahboard.bff.gateway;

import com.ican.saf.ui.dsahboard.bff.spi.model.RouteTransformer;
import jakarta.servlet.http.HttpSession;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.server.mvc.config.GatewayMvcProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Optional;

@Component
@NoArgsConstructor
public class SimpleRouteTransformer implements RouteTransformer {
    private RouterFunction<ServerResponse> routeLocator;
    private String realm;
    private String accessToken;
    private String apiKey;

    private final Logger logger = LoggerFactory.getLogger(SimpleRouteTransformer.class);

    public SimpleRouteTransformer(RouterFunction<ServerResponse> routeLocator, HttpSession session, String apiKey){
        this(routeLocator, session.getAttribute("REALM") + "", session.getAttribute("TOKEN") + "", apiKey);
    }

    public SimpleRouteTransformer(RouterFunction<ServerResponse> routeLocator, String realm, HttpSession session, String apiKey){
        this(routeLocator, realm, session.getAttribute("TOKEN") + "", apiKey);
    }

    public SimpleRouteTransformer(RouterFunction<ServerResponse> routeLocator, String realm, String accessToken, String apiKey){
        this.routeLocator = routeLocator;
        this.realm = realm;
        this.accessToken = accessToken;
        this.apiKey = apiKey;
    }

    @Override
    public String to(String url) {
        logger.info("inside to {}", url);
        return "";
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public Optional<String> to(String id, String path, GatewayMvcProperties gatewayProperties) {
        List<String> mutatePath = gatewayProperties.getRoutes()
                .stream()
                .filter(route -> route.getId().equals(id))
                .map(p->p.getUri().toString() + path)
                .map(r->r.replace("|realm|", realm))
                .toList();
        if(mutatePath.size() == 1){
            return mutatePath.stream().findFirst();
        } else {
            throw new RuntimeException("Route transformation not found for path: " + path);
        }
    }

    public static String replaceRealm(String url, String keycloakRealm){
        if(keycloakRealm != null){
            return url.replace("|realm|", keycloakRealm);
        }
        return url;
    }
}
