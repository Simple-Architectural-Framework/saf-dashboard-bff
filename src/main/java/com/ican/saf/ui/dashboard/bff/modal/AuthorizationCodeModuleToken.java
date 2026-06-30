package com.ican.saf.ui.dashboard.bff.modal;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;

import java.util.Map;

public class AuthorizationCodeModuleToken extends OAuth2AuthorizationCodeAuthenticationToken {
    public AuthorizationCodeModuleToken(ClientRegistration clientRegistration, OAuth2AuthorizationExchange authorizationExchange) {
        super(clientRegistration, authorizationExchange);
    }

    public AuthorizationCodeModuleToken(ClientRegistration clientRegistration, OAuth2AuthorizationExchange authorizationExchange, OAuth2AccessToken accessToken) {
        super(clientRegistration, authorizationExchange, accessToken);
    }

    public AuthorizationCodeModuleToken(ClientRegistration clientRegistration, OAuth2AuthorizationExchange authorizationExchange, OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken) {
        super(clientRegistration, authorizationExchange, accessToken, refreshToken);
    }

    public AuthorizationCodeModuleToken(ClientRegistration clientRegistration, OAuth2AuthorizationExchange authorizationExchange, OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken, Map<String, Object> additionalParameters) {
        super(clientRegistration, authorizationExchange, accessToken, refreshToken, additionalParameters);
    }
}
