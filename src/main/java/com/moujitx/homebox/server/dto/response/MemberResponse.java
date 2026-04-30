package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberResponse {

    private Long id;
    private String username;
    private String displayName;
    private String roleName;
    private boolean forceChangePassword;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberResponse from(User user) {
        MemberResponse response = new MemberResponse();
        response.id = user.getId();
        response.username = user.getUsername();
        response.displayName = user.getDisplayName();
        response.roleName = user.getRole().getName();
        response.forceChangePassword = user.isForceChangePassword();
        response.createdAt = user.getCreatedAt();
        response.updatedAt = user.getUpdatedAt();
        return response;
    }
}
