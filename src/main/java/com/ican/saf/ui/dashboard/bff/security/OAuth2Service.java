package com.ican.saf.ui.dashboard.bff.security;

import com.ican.saf.session.model.OAuth2TokenResponse;
import com.ican.saf.ui.dashboard.bff.crypto.StringEncryptorDesede;
import com.ican.saf.ui.dashboard.bff.spi.client.ClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.config.GatewayMvcProperties;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;

@Service
public class OAuth2Service {

    @Value("${crypto.sso_token_key}")
    private String ssoTokenKey;

    @Value("${crypto.otp_token_delimiter}")
    private String otpTokenDelimiter;

    @Value("${crypto.otp_encryption_key}")
    private String otpEncryptionKey;

    @Value("${saf.dashboard.api-key}")
    private String apiKey;

    @Value("${saf.dashboard.realm}")
    private String realm;

    @Value("${authentication.url}")
    private String authenticationUrl;

    private final Logger logger = LoggerFactory.getLogger(OAuth2Service.class);

    private final GatewayMvcProperties gatewayProperties;
    private final StringEncryptorDesede stringEncryptorDesede;

    public OAuth2Service(GatewayMvcProperties gatewayProperties, StringEncryptorDesede stringEncryptorDesede){
        this.gatewayProperties = gatewayProperties;
        this.stringEncryptorDesede = stringEncryptorDesede;
    }

    public OAuth2TokenResponse login(String username, String password,
                                     String grantType, ValidationType validationType) {
        try {
            var formData = new LinkedMultiValueMap<String, Object>();
            formData.add("username", username);
            formData.add("password", password);
            formData.add("grant_type", grantType);
            formData.add("client_id", realm);
            formData.add("validation_type", validationType.getCode());


            return ClientHelper.getClientInstanceWithRealm(authenticationUrl, realm)
                    .post()
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .header("x-identity", username)
                    .header("x-api-key", apiKey)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(formData)
                    .retrieve()
                    .body(OAuth2TokenResponse.class);
        } catch (Exception e) {
            logger.error("Failure during system entry");
            throw new AuthenticationServiceException("Failure during system entry. Please try again later.", e);
        }
    }

}
