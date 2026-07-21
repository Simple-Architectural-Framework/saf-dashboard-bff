package com.ican.saf.ui.dashboard.bff.web.service;

import com.ican.saf.ui.dashboard.bff.web.model.AppsUrl;
import com.ican.saf.ui.dashboard.bff.web.repository.OAuth2Repository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UrlService {
    private final OAuth2Repository oAuth2Repository;

    public UrlService(OAuth2Repository oAuth2Repository){
        this.oAuth2Repository = oAuth2Repository;
    }

    public String getRelatedUrl(String appName){
        Optional<AppsUrl> url = oAuth2Repository.findByAppName(appName);

        return url.map(AppsUrl::getAppsUrl).orElse(null);
    }
}
