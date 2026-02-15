package com.ican.saf.ui.dsahboard.bff.modal;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class UserNameSurnameResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String userName;
    private String surname;
}
