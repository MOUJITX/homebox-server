package com.moujitx.homebox.server.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberRequest {

    private String displayName;

    private String roleName;

    @Size(min = 8)
    private String password;
}
