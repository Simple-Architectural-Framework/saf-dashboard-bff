package com.ican.saf.session.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class AuthenticationDetail implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String accessToken;
    private String realm;
}
