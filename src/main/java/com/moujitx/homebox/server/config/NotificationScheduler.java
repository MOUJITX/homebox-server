package com.moujitx.homebox.server.config;

import com.moujitx.homebox.server.event.ConfigChangedEvent;
import com.moujitx.homebox.server.service.NotificationService;
import com.moujitx.homebox.server.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final SystemConfigService systemConfigService;

    private final ThreadPoolTaskScheduler taskScheduler = createScheduler();
    private ScheduledFuture<?> scheduledTask;

    private static ThreadPoolTaskScheduler createScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("notification-");
        scheduler.initialize();
        return scheduler;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        scheduleFromConfig();
    }

    @EventListener
    public void onConfigChanged(ConfigChangedEvent event) {
        if ("notification".equals(event.getGroup())) {
            scheduleFromConfig();
        }
    }

    public void scheduleFromConfig() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }

        String cron = systemConfigService.get("notification.crontab");
        if (cron.isEmpty()) {
            cron = "0 0 3 * * ?";
        }

        // Normalize 5-field cron (without seconds) to 6-field
        cron = normalizeCron(cron);

        try {
            scheduledTask = taskScheduler.schedule(
                    this::runCheck,
                    new CronTrigger(cron, java.util.TimeZone.getDefault())
            );
            log.info("Notification check scheduled with cron: {}", cron);
        } catch (Exception e) {
            log.error("Failed to schedule notification check with cron: {}", cron, e);
        }
    }

    static String normalizeCron(String cron) {
        if (cron == null || cron.isEmpty()) return "0 0 3 * * ?";
        String trimmed = cron.trim();
        // Count fields by whitespace
        int fields = trimmed.split("\\s+").length;
        if (fields == 5) {
            return "0 " + trimmed;
        }
        return trimmed;
    }

    public void runCheck() {
        try {
            notificationService.checkAndNotify();
        } catch (Exception e) {
            log.error("Notification check failed", e);
        }
    }
}
