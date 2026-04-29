package com.moujitx.homebox.server.dto.response;

public class LoginResponse {

    private String token;
    private boolean forceChangePassword;

    public LoginResponse(String token, boolean forceChangePassword) {
        this.token = token;
        this.forceChangePassword = forceChangePassword;
    }

    public String getToken() {
        return token;
    }

    public boolean isForceChangePassword() {
        return forceChangePassword;
    }
}
