package com.ican.saf.ui.dsahboard.bff.spi.client;

import com.ican.saf.ui.dsahboard.bff.gateway.SimpleRouteTransformer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

public class ClientHelper {
    public static RestClient getClientInstance(String url){
        return RestClient.create(url);
    }

    public static RestClient getClientInstanceWithRealm(String url, String realm){
        return RestClient.create(SimpleRouteTransformer.replaceRealm(url, realm));
    }

    public static MultiValueMap<String, String> getHeaders(String accessToken, MultiValueMap<String, Object> additionalHeaders){
        var headers = new LinkedMultiValueMap<String, String>();
        if(null != accessToken) headers.add("Authorization", "Bearer " + accessToken);
        if(null != additionalHeaders) additionalHeaders.forEach(
                (key, value) ->
                        headers.add(key, value.getFirst().toString())
        );
        return headers;
    }
}
