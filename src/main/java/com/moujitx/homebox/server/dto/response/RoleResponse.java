package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Role;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RoleResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RoleResponse from(Role role) {
        RoleResponse response = new RoleResponse();
        response.id = role.getId();
        response.name = role.getName();
        response.description = role.getDescription();
        response.createdAt = role.getCreatedAt();
        response.updatedAt = role.getUpdatedAt();
        return response;
    }
}
