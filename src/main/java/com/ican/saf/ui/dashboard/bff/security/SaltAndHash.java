package com.ican.saf.ui.dsahboard.bff.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaltAndHash {
    private String saltHex;
    private String hashHex;
}
