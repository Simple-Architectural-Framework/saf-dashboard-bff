package com.ican.saf.ui.dashboard.bff.modal;

import java.io.Serial;
import java.io.Serializable;

public class OtpRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 10L;
    private String userName;

    public OtpRequest() {
    }

    public OtpRequest(String userName) {
        super();
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "OtpRequest{" +
                "userName='" + userName + '\'' +
                '}';
    }
}
