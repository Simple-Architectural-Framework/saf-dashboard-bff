package com.ican.saf.session.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class VisualElementDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer elementId;
    private Integer applicationId;
    private Short kind;
    private String number;
    private String name;
    private String description;
}
