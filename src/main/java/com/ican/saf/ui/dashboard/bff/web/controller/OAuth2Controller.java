package com.ican.saf.ui.dashboard.bff.web.controller;

import com.ican.saf.session.model.AuthenticationDetail;
import com.ican.saf.session.model.AuthenticationResponse;
import com.ican.saf.session.model.User;
import com.ican.saf.ui.dashboard.bff.gateway.SimpleRouteTransformer;
import com.ican.saf.ui.dashboard.bff.modal.UsernamePasswordAuthenticationModuleToken;
import com.ican.saf.ui.dashboard.bff.properties.SSOProperties;
import com.ican.saf.ui.dashboard.bff.spi.model.RouteTransformer;
import com.ican.saf.ui.dashboard.bff.util.AuthenticationUtils;
import com.ican.saf.ui.dashboard.bff.util.SessionUtils;
import com.ican.saf.ui.dashboard.bff.web.service.AuthService;
import com.ican.saf.ui.dashboard.bff.web.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.*;

@RestController
@RequestMapping("/auth")
@EnableConfigurationProperties(SSOProperties.class)
public class OAuth2Controller {

    private final RouterFunction<ServerResponse> routeLocator;
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final UrlService urlService;
    private final Environment env;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${saf.dashboard.api-key}")
    private String apiKey;

    @Value("${saf.dashboard.applicationId}")
    private String applicationId;

    @Value("${saf.dashboard.authorizationUrl}")
    private String authorizationUrl;

    @Value("${saf.dashboard.realm}")
    private String realm;

    public OAuth2Controller(RouterFunction<ServerResponse> routeLocator,
                            AuthenticationManager authenticationManager,
                            AuthService authService,
                            UrlService urlService,
                            ClientRegistrationRepository clientRegistrationRepository,
                            Environment env
    ) {
        this.routeLocator = routeLocator;
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.urlService = urlService;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.env = env;
    }

    @GetMapping(value = "/login", produces = "application/json; charset=utf-8")
    public ResponseEntity<Map<String, String>> oauth2Login(){
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("authentik");

        if(registration == null){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Auhtentik client config not found!"));
        }

        String authzUrl = authorizationUrl + registration.getRegistrationId();

        return ResponseEntity.ok(Map.of("redirectUrl", authzUrl));
    }

//    @GetMapping(value = "/login/oauth2/code/authentik", produces = "application/json; charset=utf-8")
//    public ResponseEntity<String> oauth2Code(@AuthenticationPrincipal OAuth2User principal){
//        if(principal == null){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found!");
//        }
//        //Extract the user's name or attributes provided by Authentik
//        String name = principal.getAttribute("name");
//        String email = principal.getAttribute("email");
//        return ResponseEntity.ok("Hello " + name + "! Your email is: " +  email);
//    }

//    @GetMapping(value = "/home", produces = "application/json; charset=utf-8")
//    public ResponseEntity<String> home(@AuthenticationPrincipal OAuth2User principal){
//        if(principal == null){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found!");
//        }
//        //Extract the user's name or attributes provided by Authentik
//        String name = principal.getAttribute("name");
//        String email = principal.getAttribute("email");
//        return ResponseEntity.ok("Hello " + name + "! Your email is: " +  email);
//    }

    @GetMapping(value = "/redirect-url", produces = "application/json; charset=utf-8")
    public ResponseEntity<?> getRedirectUrl(@RequestParam String appName){
        String appUrl = urlService.getRelatedUrl(appName);

        return (appUrl != null) ?
                ResponseEntity.ok(Map.of("url", appUrl))
                :
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Url not found!"));
    }

    @PostMapping(value = "login", produces = "application/json; charset=utf-8")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestParam("username") String userName,
            @RequestParam("password") String password,
            @RequestParam("jss") String jsSessionID,
            HttpServletRequest request, HttpSession session){
        session.setAttribute("JS_SESSION_ID", jsSessionID);
        RouteTransformer routeTransformer = new SimpleRouteTransformer(routeLocator, realm, session, apiKey);
        return doAuthenticate(new UsernamePasswordAuthenticationModuleToken(toLowerCaseEng(userName), password, realm, routeTransformer, session, request), session);
    }

    private ResponseEntity<AuthenticationResponse> doAuthenticate(UsernamePasswordAuthenticationModuleToken authenticationToken, HttpSession session) {
        try {
            var authentication = authenticationManager.authenticate(authenticationToken);
            if (null != authentication) {
                var jsSessionID = session.getAttribute("JS_SESSION_ID") + "";

                var newSession = SessionUtils.clearSessionAndChangeSessionId(session);
                var securityContext = new SecurityContextImpl();
                securityContext.setAuthentication(authentication);
                newSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
                SecurityContextHolder.setContext(securityContext);

                var authenticationDetail = (AuthenticationDetail) authentication.getDetails();

                var routeTransformer = new SimpleRouteTransformer(routeLocator, authenticationToken.getRealm(), newSession, apiKey);
                var user = new User((String) authentication.getPrincipal(), authenticationDetail.getAccessToken(),
                        AuthenticationUtils.getAuthoritiesAsText(authentication));

                try {
                    var nameSurnameResponse = authService.getUserNameSurname(routeTransformer, authentication.getName());
                    if (null != nameSurnameResponse) {
                        var userDto = authService.getUserDetails(routeTransformer, authenticationDetail.getAccessToken(), user.getUsername());
                        if (null != userDto) {
                            newSession.setAttribute("__user__", user);
                            newSession.setAttribute("customerInfo", userDto);
                            newSession.setAttribute("JS_SESSION_ID", jsSessionID);
                            newSession.setAttribute("TOKEN", authenticationDetail.getAccessToken());
                            newSession.setAttribute("REALM", authenticationDetail.getRealm());
                            newSession.setAttribute("X-sessionId", UUID.randomUUID().toString());
                            newSession.setAttribute("X-applicationId", applicationId);
                            newSession.setAttribute("X-api-key", apiKey);
                            newSession.setAttribute("saf.allowMultipleLogin", env.getProperty("saf.allowMultipleLogin", "false"));
                            newSession.setAttribute("X-firstname", nameSurnameResponse.getFirstName());
                            newSession.setAttribute("X-lastname", nameSurnameResponse.getLastName());

                            var authenticationResponse = AuthenticationResponse.ok(authentication, null);
                            newSession.setAttribute("X-doAuthenticate", authenticationResponse);
                            return ResponseEntity.ok(AuthenticationResponse.ok(authentication, nameSurnameResponse));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            return ResponseEntity.ok(AuthenticationResponse.loginFail(e.getMessage()));
        }
        return null;
    }

    private static String toLowerCaseEng(String text){
        return (text != null) ? text.toLowerCase(Locale.ENGLISH) : null;
    }
}
