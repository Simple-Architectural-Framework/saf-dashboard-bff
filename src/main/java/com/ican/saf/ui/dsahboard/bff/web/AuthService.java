package com.ican.saf.ui.dsahboard.bff.web;

import com.ican.saf.session.model.AuthenticationServiceResponse;
import com.ican.saf.ui.dsahboard.bff.crypto.StringEncryptorDesede;
import com.ican.saf.ui.dsahboard.bff.crypto.StringEncryptorInt;
import com.ican.saf.ui.dsahboard.bff.security.ValidationType;
import com.ican.saf.ui.dsahboard.bff.spi.client.ClientHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Date;

@Service
public class AuthService {
    @Value("${crypto.sso_token_key}")
    private String ssoTokenKey;

    @Value("${logi.dashboard.realm}")
    private String realm;

    @Value("${authentication.url}")
    private String authenticationUrl;

    private final GatewayProperties gatewayProperties;
    private final StringEncryptorDesede stringEncryptorDesede;

    public AuthService(GatewayProperties gatewayProperties, StringEncryptorDesede stringEncryptorDesede){
        this.gatewayProperties = gatewayProperties;
        this.stringEncryptorDesede = stringEncryptorDesede;
    }

    public AuthenticationServiceResponse callLoginService(String userName, String password,
                                                          String grantType, ValidationType validationType) {
        try {
            var formData = new LinkedMultiValueMap<String, Object>();
            formData.add("username", userName);
            formData.add("password", password);
            formData.add("grant_type", grantType);
            formData.add("client_id", realm);
            formData.add("validation_type", validationType.getCode());

            if(validationType.equals(ValidationType.SSO)){
                var ssoToken = stringEncryptorDesede.encrypt(Long.toString(new Date().getTime()), ssoTokenKey);
                formData.add("sso", "1");
                formData.add("sso_token", ssoToken);
            } else {
                formData.add("sso", "0");
            }

            return ClientHelper.getClientInstanceWithRealm(authenticationUrl, realm)
                    .post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("x-identity", userName)

        }
    }
}
