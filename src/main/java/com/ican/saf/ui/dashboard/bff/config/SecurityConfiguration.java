package com.ican.saf.ui.dashboard.bff.config;

import com.ican.saf.ui.dashboard.bff.modal.SSOAuthenticationToken;
import com.ican.saf.ui.dashboard.bff.modal.UsernamePasswordAuthenticationModuleToken;
import com.ican.saf.ui.dashboard.bff.security.OAuth2Service;
import com.ican.saf.ui.dashboard.bff.security.ValidationType;
import com.ican.saf.ui.dashboard.bff.spi.model.RouteTransformer;
import com.ican.saf.ui.dashboard.bff.web.AuthService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import java.util.ArrayList;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Value("${saf.dashboard.api-key}")
    private String apiKey;

    private final AuthService authService;

    private final OAuth2Service oAuth2Service;

    public SecurityConfiguration(AuthService authService, OAuth2Service oAuth2Service){
        this.authService = authService;
        this.oAuth2Service = oAuth2Service;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .build();
    }

    @SneakyThrows
    @Bean
    public AuthenticationManager authenticationManager(){
        return this::authenticate;
    }

    private Authentication authenticate(Authentication authentication){
        UsernamePasswordAuthenticationModuleToken moduleToken = (UsernamePasswordAuthenticationModuleToken) authentication;
        var userName = authentication.getPrincipal().toString();
        var credentials = authentication.getCredentials();
        var password = credentials == null ? null : credentials.toString();
        var session = moduleToken.getSession();

        var routeTransformer = moduleToken.getRouteTransformer();

        var tokenResponse = oAuth2Service.login(userName, password, "password", ValidationType.SSO);

        if(null == tokenResponse || null != tokenResponse.getError() ){
            throw new AuthenticationServiceException("Failure during system entry. Please try again.");
        } else {
            var accessToken = tokenResponse.getAccessToken();

            if(!(authentication instanceof SSOAuthenticationToken)){
                try{
                    var validationResponse = authService.validateUser(routeTransformer, userName, null, accessToken, session);
                    return validationResponse.isSuccess() ? authorizeUser(authentication, routeTransformer, userName, accessToken, password, null) : null;
                } catch (Exception e) {
                    throw new BadCredentialsException("Invalid credentials");
                }
            }
            return authorizeUser(authentication, routeTransformer, userName, accessToken, password, null);
        }
    }

    private UsernamePasswordAuthenticationToken authorizeUser(Authentication authentication, RouteTransformer routeTransformer,
                                                              String userName, String accessToken, String password, Integer groupNo){

        try{
            var userDto = authService.authorizeUser(routeTransformer, userName, authentication, accessToken);
            var authorities = new ArrayList<GrantedAuthority>();
            if(null != userDto && null != userDto.getRoles()){
                for(var role : userDto.getRoles()){
                    for(var ve : role.getVisualElements()){
                        authorities.add(new SimpleGrantedAuthority(ve.getName()));
                    }
                }
            }
            var result = new UsernamePasswordAuthenticationToken(userName, password, authorities);
            result.setDetails(authService.generateAuthenticationDetail(accessToken, groupNo));
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
