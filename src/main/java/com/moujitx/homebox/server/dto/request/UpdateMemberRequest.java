package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateMemberRequest {

    private String displayName;

    private String roleName;

    @Size(min = 8)
    private String password;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
