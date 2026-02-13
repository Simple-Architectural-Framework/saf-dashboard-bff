package com.ican.saf.session.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AuthorizationDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer id;
    private int authorizationTypeId;
    private String name;
    private List<VisualElementDTO> visualElements = new ArrayList<>();
}
