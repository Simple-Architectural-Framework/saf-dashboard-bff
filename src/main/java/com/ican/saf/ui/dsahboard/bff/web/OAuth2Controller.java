package com.ican.saf.ui.dsahboard.bff.web;

import com.ican.saf.session.model.AuthenticationDetail;
import com.ican.saf.session.model.AuthenticationResponse;
import com.ican.saf.session.model.User;
import com.ican.saf.ui.dsahboard.bff.gateway.SimpleRouteTransformer;
import com.ican.saf.ui.dsahboard.bff.modal.UsernamePasswordAuthenticationModuleToken;
import com.ican.saf.ui.dsahboard.bff.spi.model.RouteTransformer;
import com.ican.saf.ui.dsahboard.bff.util.AuthenticationUtils;
import com.ican.saf.ui.dsahboard.bff.util.SessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Locale;
import java.util.UUID;

@Controller
public class OAuth2Controller {

    private final RouterFunction<ServerResponse> routeLocator;
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final Environment env;

    @Value("${saf.dashboard.apiKey}")
    private String apiKey;

    @Value("${saf.dashboard.applicationId}")
    private String applicationId;

    @Value("${saf.dashboard.realm}")
    private String realm;

    public OAuth2Controller(RouterFunction<ServerResponse> routeLocator, AuthenticationManager authenticationManager, AuthService authService, Environment env) {
        this.routeLocator = routeLocator;
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.env = env;
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
