package com.ican.saf.ui.dsahboard.bff.util;

import org.springframework.http.server.ServerHttpRequest;

public class SafWebUtils {
    public static String getClientAddress(ServerHttpRequest request){
        var clientAddress = request.getHeaders().getFirst("X-Forwarded-For");

        if(clientAddress == null || clientAddress.isEmpty() || "unknown".equalsIgnoreCase(clientAddress)){
            if(request.getRemoteAddress() != null){
                clientAddress = request.getRemoteAddress().getAddress().getHostAddress();
            }
        } else {
            var commandIndex = clientAddress.indexOf(',');
            if(commandIndex != -1){
                clientAddress = clientAddress.substring(0, commandIndex);
            }
        }
        return clientAddress;
    }

    public static String getLookupPathForRequest(ServerHttpRequest request){
        return request.getURI().getPath();
    }

    public static String getRequestServerName(ServerHttpRequest request){
        return request.getURI().getHost();
    }
}
