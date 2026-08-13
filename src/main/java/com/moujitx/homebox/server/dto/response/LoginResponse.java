package com.moujitx.homebox.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private long expiresIn;
    private boolean forceChangePassword;
}
