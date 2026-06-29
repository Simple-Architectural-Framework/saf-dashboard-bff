package com.ican.saf.ui.dashboard.bff.modal;

import com.ican.saf.ui.dashboard.bff.spi.model.RouteTransformer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.Serial;

public class UsernamePasswordAuthenticationModuleToken extends UsernamePasswordAuthenticationToken {
    @Serial
    private static final long serialVersionUID = 1L;
    private String realm;
    private RouteTransformer routeTransformer;
    private HttpSession session;
    private HttpServletRequest request;

    public UsernamePasswordAuthenticationModuleToken(Object principal, Object credentials, String realm, RouteTransformer routeTransformer,
                                                     HttpSession session, HttpServletRequest request) {
        super(principal, credentials);
        this.realm = realm;
        this.routeTransformer = routeTransformer;
        this.session = session;
        this.request = request;
    }

    public String getRealm() {
        return realm;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpSession getSession() {
        return session;
    }

    public RouteTransformer getRouteTransformer() {
        return routeTransformer;
    }
}
