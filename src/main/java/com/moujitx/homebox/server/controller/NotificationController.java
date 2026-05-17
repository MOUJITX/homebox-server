package com.moujitx.homebox.server.controller;

import com.moujitx.homebox.server.dto.response.NotificationResponse;
import com.moujitx.homebox.server.dto.response.TestConnectionResponse;
import com.moujitx.homebox.server.entity.Notification;
import com.moujitx.homebox.server.enums.NotificationType;
import com.moujitx.homebox.server.service.NotificationService;
import com.moujitx.homebox.server.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final WebhookService webhookService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Notification> notifications = notificationService.list(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(notifications.map(NotificationResponse::from));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-webhook")
    public ResponseEntity<TestConnectionResponse> testWebhook() {
        try {
            Notification testNotification = new Notification();
            testNotification.setType(NotificationType.ITEM_EXPIRING);
            testNotification.setTitle("Test Notification");
            testNotification.setContent("This is a test notification from Homebox webhook configuration.");
            testNotification.setCreatedAt(LocalDateTime.now());
            webhookService.sendTest(testNotification);
            return ResponseEntity.ok(new TestConnectionResponse(true, "Webhook test request sent"));
        } catch (Exception e) {
            return ResponseEntity.ok(new TestConnectionResponse(false, "Webhook test failed: " + e.getMessage()));
        }
    }
}
