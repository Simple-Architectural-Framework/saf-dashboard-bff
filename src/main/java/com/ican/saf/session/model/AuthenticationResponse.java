package com.ican.saf.session.model;

import com.ican.saf.ui.dsahboard.bff.util.AuthenticationUtils;
import org.springframework.security.core.Authentication;

import java.util.List;

public class AuthenticationResponse extends SessionResponse{
    private boolean success;
    private String message;
    private ErrorInfo error;

    public AuthenticationResponse() {}

    public AuthenticationResponse(String username, boolean authenticated, List<String> roles, Object customData) {
        super(username, authenticated, roles, customData);
    }

    public static AuthenticationResponse ok(Authentication authentication, Object customData) {
        var result = new AuthenticationResponse(authentication.getPrincipal().toString(), true,
                AuthenticationUtils.getAuthoritiesAsText(authentication), customData);
        result.success = true;
        result.message = "OK";
        return result;
    }

    public static AuthenticationResponse loginFail(String message){
        var result = new AuthenticationResponse();
        result.success = false;
        result.error = new ErrorInfo();
        result.error.setErrorCode("login.error.badLogin");
        result.error.setMessage(message);
        return result;
    }
}
