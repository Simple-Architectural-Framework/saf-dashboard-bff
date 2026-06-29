package com.ican.saf.ui.dashboard.bff.security;

public enum ValidationType {
    PASSWORD("PWD"),
    PASSWORD_OTP("PWD_OTP"),
    OTP("OTP"),
    SSO("SSO");

    private final String code;

    ValidationType(String code){this.code = code;}

    public String getCode(){return code;}
}
