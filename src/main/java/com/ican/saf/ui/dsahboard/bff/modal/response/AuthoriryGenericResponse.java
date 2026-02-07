package com.ican.saf.ui.dsahboard.bff.modal.response;

import java.io.Serial;
import java.io.Serializable;

public class AuthoriryGenericResponse implements Serializable {
    @Serial
    private static long serialVersionUID = 9L;

    private Object data;
    private boolean success;
    private Object error;
    private Object validations;

    public AuthoriryGenericResponse() {
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public static void setSerialVersionUID(long serialVersionUID) {
        AuthoriryGenericResponse.serialVersionUID = serialVersionUID;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getValidations() {
        return validations;
    }

    public void setValidations(Object validations) {
        this.validations = validations;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
