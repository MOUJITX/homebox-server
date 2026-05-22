package com.moujitx.homebox.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionNotificationMigration {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    public void dropDedupConstraint() {
        try {
            var rows = jdbcTemplate.queryForList(
                    "SELECT 1 FROM information_schema.STATISTICS " +
                    "WHERE TABLE_SCHEMA = DATABASE() " +
                    "AND TABLE_NAME = 'notifications' " +
                    "AND INDEX_NAME = 'uk_notify_dedup'"
            );
            if (!rows.isEmpty()) {
                jdbcTemplate.execute("ALTER TABLE notifications DROP INDEX uk_notify_dedup");
                log.info("Dropped uk_notify_dedup constraint for notification dedup");
            } else {
                log.debug("uk_notify_dedup constraint does not exist, skipping");
            }
        } catch (Exception e) {
            log.warn("Failed to drop uk_notify_dedup: {}", e.getMessage());
        }
    }
}
