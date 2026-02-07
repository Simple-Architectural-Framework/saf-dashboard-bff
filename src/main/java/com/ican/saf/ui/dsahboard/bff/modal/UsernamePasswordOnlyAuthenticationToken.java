package com.ican.saf.ui.dsahboard.bff.modal;

import com.ican.saf.ui.dsahboard.bff.spi.model.RouteTransformer;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.server.WebSession;

import java.io.Serial;

public class UsernamePasswordOnlyAuthenticationToken extends UsernamePasswordAuthenticationModuleToken{
    @Serial
    private static final long serialVersionUID = 1L;

    public UsernamePasswordOnlyAuthenticationToken(Object principal, Object credentials, String realm, RouteTransformer routeTransformer, WebSession webSession, ServerHttpRequest request) {
        super(principal, credentials, realm, routeTransformer, webSession, request);
    }
}
