package com.ican.saf.ui.dsahboard.bff.modal;

import com.ican.saf.ui.dsahboard.bff.spi.model.RouteTransformer;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.WebSession;

import java.io.Serial;

public class UsernamePasswordAuthenticationModuleToken extends UsernamePasswordAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 1L;
    private String realm;
    private RouteTransformer routeTransformer;
    private WebSession webSession;
    private ServerHttpRequest request;

    public UsernamePasswordAuthenticationModuleToken(Object principal, Object credentials, String realm, RouteTransformer routeTransformer,
                                                     WebSession webSession, ServerHttpRequest request) {
        super(principal, credentials);
        this.realm = realm;
        this.routeTransformer = routeTransformer;
        this.webSession = webSession;
        this.request = request;
    }

    public String getRealm() {
        return realm;
    }

    public ServerHttpRequest getRequest() {
        return request;
    }

    public WebSession getWebSession() {
        return webSession;
    }

    public RouteTransformer getRouteTransformer() {
        return routeTransformer;
    }
}
