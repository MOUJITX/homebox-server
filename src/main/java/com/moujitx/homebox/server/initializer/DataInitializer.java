package com.moujitx.homebox.server.initializer;

import com.moujitx.homebox.server.entity.Role;
import com.moujitx.homebox.server.entity.SystemConfig;
import com.moujitx.homebox.server.entity.User;
import com.moujitx.homebox.server.repository.RoleRepository;
import com.moujitx.homebox.server.repository.SystemConfigRepository;
import com.moujitx.homebox.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.root.username}")
    private String rootUsername;

    @Value("${app.root.password}")
    private String rootPassword;

    @Value("${app.qiniu.access-key:}")
    private String qiniuAccessKey;

    @Value("${app.qiniu.secret-key:}")
    private String qiniuSecretKey;

    @Value("${app.qiniu.bucket:}")
    private String qiniuBucket;

    @Value("${app.qiniu.folder:}")
    private String qiniuFolder;

    @Value("${app.qiniu.domain:}")
    private String qiniuDomain;

    @Override
    @Transactional
    public void run(String... args) {
        Role rootRole = roleRepository.findByName("root").orElseGet(() -> {
            log.info("Creating root role");
            return roleRepository.save(new Role("root", "System administrator role"));
        });

        if (!roleRepository.existsByName("member")) {
            log.info("Creating member role");
            roleRepository.save(new Role("member", "Standard member role"));
        }

        if (!userRepository.existsByUsername(rootUsername)) {
            log.info("Creating root user: {}", rootUsername);
            User rootUser = new User();
            rootUser.setUsername(rootUsername);
            rootUser.setPassword(passwordEncoder.encode(rootPassword));
            rootUser.setDisplayName("Root Administrator");
            rootUser.setRole(rootRole);
            rootUser.setForceChangePassword(true);
            userRepository.save(rootUser);
        }

        seedConfig("qiniu.access-key", qiniuAccessKey, "qiniu", true, "Qiniu Access Key");
        seedConfig("qiniu.secret-key", qiniuSecretKey, "qiniu", true, "Qiniu Secret Key");
        seedConfig("qiniu.bucket", qiniuBucket, "qiniu", false, "Qiniu Bucket Name");
        seedConfig("qiniu.folder", qiniuFolder, "qiniu", false, "Qiniu Folder Path");
        seedConfig("qiniu.domain", qiniuDomain, "qiniu", false, "Qiniu CDN Domain");

        seedConfig("ai.system-prompt", "", "ai", false, "AI System Prompt (for invoice parsing)");
        seedConfig("ai.models", "[]", "ai", false, "AI Models List (JSON)");
        seedConfig("ai.active-model", "", "ai", false, "Active AI Model ID");
        seedConfig("ai.visit-record-prompt", "", "ai", false, "AI Prompt for Visit Record Parsing");

        seedConfig("notification.webhook-enabled", "false", "notification", false, "Enable Webhook Notifications");
        seedConfig("notification.webhook-url", "", "notification", false, "Webhook URL");
        seedConfig("notification.webhook-template", "", "notification", false, "Webhook Payload Template");
        seedConfig("notification.crontab", "0 0 3 * * ?", "notification", false, "Notification Check Cron Expression");
        seedConfig("notification.asset-expiring-soon-days", "30", "notification", false, "Asset Warranty Expiring Soon Days");

        seedConfig("notification.medication-crontab", "0 0 7-20 * * ?", "notification", false, "Medication Reminder Check Cron Expression");
        seedConfig("notification.subscription-crontab", "0 0 8 * * ?", "notification", false, "Subscription Renewal Check Cron Expression");

        seedConfig("elasticsearch.enabled", "false", "elasticsearch", false, "Enable Elasticsearch Search");
    }

    private void seedConfig(String key, String value, String group, boolean sensitive, String description) {
        if (!systemConfigRepository.existsByConfigKey(key)) {
            log.info("Seeding system config: {}", key);
            systemConfigRepository.save(new SystemConfig(key, value, group, sensitive, description));
        }
    }
}
