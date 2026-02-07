package com.ican.saf.ui.dsahboard.bff.spi.client;

import com.ican.saf.ui.dsahboard.bff.gateway.SimpleRouteTransformer;
import org.springframework.web.client.RestClient;

public class ClientHelper {
    public static RestClient getClientInstance(String url){
        return RestClient.create(url);
    }

    public static RestClient getClientInstanceWithRealm(String url, String realm){
        return RestClient.create(SimpleRouteTransformer.replaceRealm(url, realm));
    }
}
