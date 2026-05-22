package com.moujitx.homebox.server.dto.response;

import com.moujitx.homebox.server.entity.Platform;
import com.moujitx.homebox.server.util.OssUrlBuilder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PlatformResponse {

    private Long id;
    private String name;
    private String logoUrl;
    private String website;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PlatformResponse from(Platform platform) {
        PlatformResponse r = new PlatformResponse();
        r.id = platform.getId();
        r.name = platform.getName();
        if (platform.getLogoFile() != null) {
            r.logoUrl = OssUrlBuilder.build(
                    platform.getLogoFile().getStoredFilename(),
                    platform.getLogoFile().getOriginalFilename());
        }
        r.website = platform.getWebsite();
        r.createdAt = platform.getCreatedAt();
        r.updatedAt = platform.getUpdatedAt();
        return r;
    }
}
