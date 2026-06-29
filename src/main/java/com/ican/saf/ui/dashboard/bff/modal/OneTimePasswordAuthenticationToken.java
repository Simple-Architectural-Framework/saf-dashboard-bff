package com.ican.saf.ui.dashboard.bff.modal;

import com.ican.saf.ui.dashboard.bff.spi.model.RouteTransformer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.Serial;

public class OneTimePasswordAuthenticationToken extends UsernamePasswordAuthenticationModuleToken{
    @Serial
    private static final long serialVersionUID = 1L;

    public OneTimePasswordAuthenticationToken(Object principal, Object credentials, String realm, RouteTransformer routeTransformer, HttpSession session, HttpServletRequest request) {
        super(principal, credentials, realm, routeTransformer, session, request);
    }
}
