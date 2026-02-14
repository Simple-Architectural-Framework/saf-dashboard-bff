package com.ican.saf.session.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class ErrorInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String errorCode;
    private String message;
}
