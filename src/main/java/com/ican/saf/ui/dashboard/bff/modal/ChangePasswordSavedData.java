package com.ican.saf.ui.dashboard.bff.modal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordSavedData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String accessToken;
    private String username;
    private String taxNumber;

}
