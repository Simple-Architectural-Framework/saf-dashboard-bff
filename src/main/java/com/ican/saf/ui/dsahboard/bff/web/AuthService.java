package com.ican.saf.ui.dsahboard.bff.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ican.saf.session.model.AuthenticationServiceResponse;
import com.ican.saf.ui.dsahboard.bff.crypto.StringEncryptorDesede;
import com.ican.saf.ui.dsahboard.bff.crypto.StringEncryptorInt;
import com.ican.saf.ui.dsahboard.bff.modal.OneTimePasswordAuthenticationToken;
import com.ican.saf.ui.dsahboard.bff.modal.OtpRequest;
import com.ican.saf.ui.dsahboard.bff.modal.SSOAuthenticationToken;
import com.ican.saf.ui.dsahboard.bff.modal.UsernamePasswordOnlyAuthenticationToken;
import com.ican.saf.ui.dsahboard.bff.modal.response.AuthoriryGenericResponse;
import com.ican.saf.ui.dsahboard.bff.security.ValidationType;
import com.ican.saf.ui.dsahboard.bff.spi.client.ClientHelper;
import com.ican.saf.ui.dsahboard.bff.spi.model.RouteTransformer;
import com.ican.saf.ui.dsahboard.bff.spi.model.response.ws.BaseResponse;
import com.ican.saf.ui.dsahboard.bff.util.SafWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static com.ican.saf.ui.dsahboard.bff.spi.client.ClientHelper.getHeaders;

@Service
public class AuthService {
    @Value("${crypto.sso_token_key}")
    private String ssoTokenKey;

    @Value("${crypto.otp_token_delimiter}")
    private String otpTokenDelimiter;

    @Value("${crypto.otp_encryption_key}")
    private String otpEncryptionKey;

    @Value("${saf.dashboard.realm}")
    private String realm;

    @Value("${authentication.url}")
    private String authenticationUrl;

    @Value("${saf.dashboard.api_key}")
    private String apiKey;

    @Value("${saf.dashboard.forceOtp:false}")
    private boolean forceOtp;

    @Value("#{'${saf.dashboard.intranetServerNames:}'.split('\\s*,\\s*').?[!#this.isEmpty()]}")
    private List<String> intranetServerNames;

    private final ObjectMapper objectMapper;

    private final GatewayProperties gatewayProperties;
    private final StringEncryptorDesede stringEncryptorDesede;

    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

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

            return  ClientHelper.getClientInstanceWithRealm(authenticationUrl, realm)
                    .post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("x-identity", userName)
                    .header("x-api-key", apiKey)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(formData)
                    .retrieve()
                    .body(AuthenticationServiceResponse.class);
        } catch (Exception e) {
            logger.error("Error while calling login service", e);
            throw new AuthenticationServiceException("Failure during system entry. Please try again.", e);
        }
    }

    public ValidationType getValidationType(Authentication authentication, ServerHttpRequest request){
        if(authentication instanceof OneTimePasswordAuthenticationToken) return ValidationType.OTP;

        if(authentication instanceof UsernamePasswordOnlyAuthenticationToken){
            if(!forceOtp && isIntranetLogin(request)){
                return ValidationType.PASSWORD;
            }
            return ValidationType.PASSWORD_OTP;
        }

        if(authentication instanceof SSOAuthenticationToken){
            return ValidationType.SSO;
        }
        return ValidationType.PASSWORD;
    }

    public AuthoriryGenericResponse sendOtp(RouteTransformer routeTransformer, OtpRequest otpRequest) throws Exception {
        var sendOtpUrl = routeTransformer.to("dashboard-public", "login/sendOtp", gatewayProperties);

        if(sendOtpUrl.isEmpty()) throw new Exception("sendOtpUrl is not empty");

        var additionalHeaders = new LinkedMultiValueMap<String, String>();
        additionalHeaders.add("x-api-key", apiKey);
        additionalHeaders.add("x-token", getOtpToken(otpRequest.getUserName(), new Date()));

        return ClientHelper.getClientInstanceWithRealm(sendOtpUrl.get(), realm)
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .acceptCharset(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders(null, additionalHeaders)))
                .body(otpRequest)
                .retrieve()
                //Handle non-2xx status code
                .onStatus(HttpStatusCode::isError, ((request, response) -> {
                    try {
                        BaseResponse<?> errorBody = objectMapper.readValue(response.getBody(), BaseResponse.class);

                        String errorMessage = "Unknown error";
                        if (errorBody != null && errorBody.getError() != null) {
                            var err = errorBody.getError();
                            errorMessage = (err.getDetailedMessage() != null && err.getDetailedMessage().isEmpty())
                                    ? err.getDetailedMessage()
                                    : err.getMessage();
                        }
                        throw new AuthenticationServiceException(errorMessage);
                    } catch (IOException e){
                        throw new AuthenticationServiceException("Error while parsing error response", e);
                    }
                }))
                .toEntity(AuthoriryGenericResponse.class);

    }

    private String getOtpToken(String username, Date date){
        try {
            var token = String.join(otpTokenDelimiter, username, Long.toString(date.getTime()));
            return stringEncryptorDesede.encrypt(token, otpEncryptionKey);
        } catch (StringEncryptorInt.EncryptionException e) {
            throw new AuthenticationServiceException("Encryption error.", e);
        }
    }

    private boolean isIntranetLogin(ServerHttpRequest request){
        logger.debug("SafAuthenticationProvider.isIntranetLogin");

        var serverName = SafWebUtils.getRequestServerName(request);

        logger.debug("serverName: {}", serverName);
        logger.debug("intranetServerNames {}", intranetServerNames);
        logger.debug("intranetServerNames.contains(serverName): {}", intranetServerNames.contains(serverName));

        return intranetServerNames.contains(serverName);
    }
}
