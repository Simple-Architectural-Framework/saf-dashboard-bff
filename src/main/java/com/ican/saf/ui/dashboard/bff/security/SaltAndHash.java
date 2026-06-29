package com.ican.saf.ui.dashboard.bff.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaltAndHash {
    private String saltHex;
    private String hashHex;
}
