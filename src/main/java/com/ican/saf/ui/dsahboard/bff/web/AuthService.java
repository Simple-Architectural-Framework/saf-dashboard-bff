package com.ican.saf.ui.dsahboard.bff.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ican.saf.exception.CredentialsFirstLoginException;
import com.ican.saf.session.model.AuthenticationDetail;
import com.ican.saf.session.model.OAuth2TokenResponse;
import com.ican.saf.session.model.UserDTO;
import com.ican.saf.ui.dsahboard.bff.crypto.StringEncryptorDesede;
import com.ican.saf.ui.dsahboard.bff.crypto.StringEncryptorInt;
import com.ican.saf.ui.dsahboard.bff.modal.*;
import com.ican.saf.ui.dsahboard.bff.security.ValidationType;
import com.ican.saf.ui.dsahboard.bff.spi.client.ClientHelper;
import com.ican.saf.ui.dsahboard.bff.spi.model.RouteTransformer;
import com.ican.saf.ui.dsahboard.bff.spi.model.response.ws.BaseResponse;
import com.ican.saf.ui.dsahboard.bff.util.SafWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.config.GatewayMvcProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.WebSession;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.ican.saf.ui.dsahboard.bff.spi.client.ClientHelper.getClientInstance;
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

    @Value("${saf.dashboard.api-key}")
    private String apiKey;

    @Value("${saf.dashboard.forceOtp:false}")
    private boolean forceOtp;

    @Value("#{'${saf.dashboard.intranetServerNames:}'.split('\\s*,\\s*').?[!#this.isEmpty()]}")
    private List<String> intranetServerNames;

    private final GatewayMvcProperties gatewayProperties;
    private final StringEncryptorDesede stringEncryptorDesede;

    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(GatewayMvcProperties gatewayProperties, StringEncryptorDesede stringEncryptorDesede){
        this.gatewayProperties = gatewayProperties;
        this.stringEncryptorDesede = stringEncryptorDesede;
    }

    public OAuth2TokenResponse callLoginService(String userName, String password,
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
                    .body(OAuth2TokenResponse.class);
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

    public BaseResponse<String> validateUser(RouteTransformer routeTransformer, String userName, String taxNumber,
                                             String accessToken, WebSession webSession){
        var additionalHeaders = new LinkedMultiValueMap<String, Object>();
        additionalHeaders.add("x-username", userName);
        additionalHeaders.add("x-api-key", apiKey);

        var validateUserUrl = routeTransformer.to("dashboard", "users/validate", gatewayProperties);

        if(validateUserUrl.isEmpty()) throw new AuthenticationServiceException("validateUserUrl is empty");

        return getClientInstance(validateUserUrl.get())
                .get()
                .acceptCharset(StandardCharsets.UTF_8)
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders(accessToken, additionalHeaders)))
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    var mapper = new ObjectMapper();
                    BaseResponse<String> errorBody = mapper.readValue(response.getBody(),
                            new TypeReference<>() {});

                    var info = Objects.requireNonNull(errorBody.getError());
                    logger.warn("validate user failed\r\n {}:{}", info.getMessage(), info.getDetailedMessage());

                    var splittedString = info.getMessage().split(":");

                    if(splittedString[0].equalsIgnoreCase("PASSWORD_EXPIRED")){
                        webSession.getAttributes().put("changePasswordSavedData", new ChangePasswordSavedData(accessToken, userName, taxNumber));
                        throw new CredentialsExpiredException(splittedString[1]);
                    } else if(splittedString[0].equalsIgnoreCase("USER_STATUS_ERROR")){
                        webSession.getAttributes().put("changePasswordSavedData", new ChangePasswordSavedData(accessToken, userName, taxNumber));
                        throw new CredentialsFirstLoginException(splittedString[1]);
                    }
                    throw new RuntimeException("Internal Server Error");
                }))
                .onStatus( response -> {
                    if(!response.getStatusCode().is2xxSuccessful()) {
                        ObjectMapper mapper = new ObjectMapper();
                        var errorBody = mapper.readValue(response.getBody(), BaseResponse.class);

                        var errorMessage = (errorBody.getError().getDetailedMessage() == null || errorBody.getError().getDetailedMessage().isEmpty()) ?
                                errorBody.getError().getMessage() : errorBody.getError().getDetailedMessage();
                        throw new AuthenticationServiceException(errorMessage);
                    }
                    return true;
                })
                .body(new ParameterizedTypeReference<>() {});

    }

    public UserDTO authorizeUser(RouteTransformer routeTransformer, String userName, Authentication authentication,
                                 String accessToken){
        var additionalHeaders = new LinkedMultiValueMap<String, String>();
        additionalHeaders.add("x-username", userName);
        additionalHeaders.add("x-api-key", apiKey);

        var authorizeUserUrl = routeTransformer.to("dashboard", "users/currentUser/allAuthorizations", gatewayProperties);
        return null;
    }

    public AuthenticationDetail generateAuthenticationDetail(String accessToken, Object ...arguments){
        return new AuthenticationDetail(accessToken, "ican");
    }

    public UserNameSurnameResponse getUserNameSurname(RouteTransformer routeTransformer, String username) throws Exception {
        var generateTokenUrl = routeTransformer.to("dashboard-public", "users/validate", gatewayProperties);

        if(generateTokenUrl.isEmpty()) throw new Exception("generateTokenUrl is empty");

        var additionalHeaders = new LinkedMultiValueMap<String, Object>();
        additionalHeaders.add("x-username", username);
        additionalHeaders.add("x-api-key", apiKey);

        return ClientHelper.getClientInstanceWithRealm(generateTokenUrl.get(), realm)
                .get()
                .acceptCharset(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders(null, additionalHeaders)))
                .retrieve()
                .onStatus( response -> {
                    if(!response.getStatusCode().is2xxSuccessful()){
                        var mapper = new ObjectMapper();
                        var errorBody = mapper.readValue(response.getBody(), BaseResponse.class);

                        var errorMessage = (errorBody.getError().getDetailedMessage() == null || errorBody.getError().getDetailedMessage().isEmpty()) ?
                                errorBody.getError().getMessage() : errorBody.getError().getDetailedMessage();
                        throw new AuthenticationServiceException(errorMessage);
                    }
                    return true;
                })
                .body(UserNameSurnameResponse.class);
    }

    public UserDTO getUserDetails(RouteTransformer routeTransformer, String accessToken, String username){
        var headers = new LinkedMultiValueMap<String, String>();
        headers.add("x-username", username);
        headers.add("x-api-key", apiKey);

        var url = routeTransformer.to("dashboard", "users/currentUser/allAuthorizations", gatewayProperties);
        if(url.isEmpty())
            try{
                throw new Exception("url is not present");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        return ClientHelper.getClientInstance(url.get())
                .get()
                .acceptCharset(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> httpHeaders.addAll(getHeaders(accessToken, headers)))
                .retrieve()
                .onStatus(response -> {
                    if(!response.getStatusCode().is2xxSuccessful()){
                        var mapper = new ObjectMapper();
                        var errorBody = mapper.readValue(response.getBody(), BaseResponse.class);

                        var errorMessage = (errorBody.getError().getDetailedMessage() == null || errorBody.getError().getDetailedMessage().isEmpty()) ?
                                errorBody.getError().getMessage() : errorBody.getError().getDetailedMessage();
                        throw new AuthenticationServiceException(errorMessage);
                    }
                    return true;
                })
                .body(UserDTO.class);
    }

/*    public AuthorityGenericResponse sendOtp(RouteTransformer routeTransformer, OtpRequest otpRequest) throws Exception {
        var sendOtpUrl = routeTransformer.to("dashboard-public", "login/sendOtp", gatewayProperties);

        if(sendOtpUrl.isEmpty()) throw new Exception("sendOtpUrl is empty");

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
                .onStatus(clientResponse ->{
                    String errorMessage = "Unknown error";
                    if(!clientResponse.getStatusCode().is2xxSuccessful()) {
                            try {
                                BaseResponse<?> errorBody = new ObjectMapper().readValue(clientResponse.getBody(), BaseResponse.class);

                                if (errorBody != null && errorBody.getError() != null) {
                                    var err = errorBody.getError();
                                    errorMessage = (err.getDetailedMessage() != null && !err.getDetailedMessage().isEmpty())
                                            ? err.getDetailedMessage()
                                            : err.getMessage();
                                }
                                throw new AuthenticationServiceException(errorMessage);
                            } catch (IOException e) {
                                throw new AuthenticationServiceException("Error while parsing error response", e);
                            }
                    }
                    return true;
                })
                .toEntity(AuthorityGenericResponse.class);

    }*/

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
