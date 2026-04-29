package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.User;

import java.time.LocalDateTime;

public class ProfileResponse {

    private Long id;
    private String username;
    private String displayName;
    private String roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProfileResponse from(User user) {
        ProfileResponse response = new ProfileResponse();
        response.id = user.getId();
        response.username = user.getUsername();
        response.displayName = user.getDisplayName();
        response.roleName = user.getRole().getName();
        response.createdAt = user.getCreatedAt();
        response.updatedAt = user.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRoleName() {
        return roleName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
