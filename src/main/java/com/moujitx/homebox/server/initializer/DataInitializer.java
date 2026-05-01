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

    @Value("${app.ai.api-url:}")
    private String aiApiUrl;

    @Value("${app.ai.api-key:}")
    private String aiApiKey;

    @Value("${app.ai.model:gpt-4o}")
    private String aiModel;

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

        seedConfig("ai.api-url", aiApiUrl, "ai", false, "AI API URL");
        seedConfig("ai.api-key", aiApiKey, "ai", true, "AI API Key");
        seedConfig("ai.model", aiModel, "ai", false, "AI Model Name");
    }

    private void seedConfig(String key, String value, String group, boolean sensitive, String description) {
        if (!systemConfigRepository.existsByConfigKey(key)) {
            log.info("Seeding system config: {}", key);
            systemConfigRepository.save(new SystemConfig(key, value, group, sensitive, description));
        }
    }
}
