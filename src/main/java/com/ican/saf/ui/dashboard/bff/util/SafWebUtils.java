package com.ican.saf.ui.dashboard.bff.util;

import jakarta.servlet.http.HttpServletRequest;

public class SafWebUtils {
    public static String getClientAddress(HttpServletRequest request){
        var clientAddress = request.getHeader("X-Forwarded-For");

        if(clientAddress == null || clientAddress.isEmpty() || "unknown".equalsIgnoreCase(clientAddress)){
            if(request.getRemoteAddr() != null){
                clientAddress = request.getRemoteAddr();
            }
        } else {
            var commandIndex = clientAddress.indexOf(',');
            if(commandIndex != -1){
                clientAddress = clientAddress.substring(0, commandIndex);
            }
        }
        return clientAddress;
    }

    public static String getLookupPathForRequest(HttpServletRequest request){
        return request.getRequestURI();
    }

    public static String getRequestServerName(HttpServletRequest request){
        return request.getRequestURI();
    }
}
