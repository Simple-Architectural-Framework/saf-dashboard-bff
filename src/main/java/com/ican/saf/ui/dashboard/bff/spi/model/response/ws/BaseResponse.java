package com.ican.saf.ui.dashboard.bff.spi.model.response.ws;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ican.saf.exception.ExceptionInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponse<T> {
    private T data;
    private boolean success;
    private ExceptionInfo error;
    private List<ValidationInfo> validations;
    private String responseCode;
    private String message;

    public BaseResponse() {
    }

    public BaseResponse(T data) {
        this.data = data;
    }

    public BaseResponse(T data, boolean success) {
        this.data = data;
        this.success = success;
    }

    public BaseResponse(boolean success, ExceptionInfo error) {
        super();
        this.success = success;
        this.error = error;
    }

}
