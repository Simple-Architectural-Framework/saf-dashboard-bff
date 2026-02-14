package com.ican.saf.ui.dsahboard.bff.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationUtils {
    public static List<String> getAuthoritiesAsText(Authentication authentication){
        var result = new ArrayList<String>();
        for(authentication.getAuthorities() != null){
            for(GrantedAuthority authority : authentication.getAuthorities()){
                result.add(authority.getAuthority());
            }
        }
        return result;
    }
}
