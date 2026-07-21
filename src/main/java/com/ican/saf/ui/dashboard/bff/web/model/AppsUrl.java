package com.ican.saf.ui.dashboard.bff.web.model;

import jakarta.persistence.*;

@Entity
@Table(name = "app_url")
public class AppsUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_name", nullable = false)
    private String appName;

    @Column(name = "url", nullable = false)
    private String appsUrl;

    public AppsUrl(){}

    public AppsUrl(
            String appName,
            String appsUrl
    ){
        this.appName = appName;
        this.appsUrl = appsUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppsUrl() {
        return appsUrl;
    }

    public void setAppsUrl(String appsUrl) {
        this.appsUrl = appsUrl;
    }
}
