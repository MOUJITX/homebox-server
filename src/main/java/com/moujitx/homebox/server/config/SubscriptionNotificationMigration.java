package com.moujitx.homebox.server.config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionNotificationMigration {

    private final EntityManager entityManager;

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    @Transactional
    public void dropDedupConstraint() {
        try {
            entityManager.createNativeQuery(
                    "ALTER TABLE notifications DROP INDEX uk_notify_dedup"
            ).executeUpdate();
            log.info("Dropped uk_notify_dedup constraint for notification dedup");
        } catch (Exception e) {
            log.debug("uk_notify_dedup constraint may already be removed: {}", e.getMessage());
        }
    }
}
