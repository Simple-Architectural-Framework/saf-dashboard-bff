package com.ican.saf.session.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String number;
    private String name;
    private String description;
    private String email;
    private String status;
    private List<AuthorizationDTO> roles = new ArrayList<>();



}
