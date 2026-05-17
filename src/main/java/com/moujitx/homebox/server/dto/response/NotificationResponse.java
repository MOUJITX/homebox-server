package com.moujitx.homebox.server.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moujitx.homebox.server.entity.Notification;
import com.moujitx.homebox.server.enums.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String content;
    @JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}
