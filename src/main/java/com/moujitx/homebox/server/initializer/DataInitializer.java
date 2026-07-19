package com.moujitx.homebox.server.initializer;

import com.moujitx.homebox.server.entity.DocumentCategory;
import com.moujitx.homebox.server.entity.BookCategory;
import com.moujitx.homebox.server.entity.Role;
import com.moujitx.homebox.server.entity.SystemConfig;
import com.moujitx.homebox.server.entity.User;
import com.moujitx.homebox.server.repository.BookCategoryRepository;
import com.moujitx.homebox.server.repository.DocumentCategoryRepository;
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
    private final DocumentCategoryRepository documentCategoryRepository;
    private final BookCategoryRepository bookCategoryRepository;
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
        seedConfig("notification.archives-crontab", "0 0 8 * * ?", "notification", false, "Document Expiry Check Cron Expression");

        seedConfig("elasticsearch.enabled", "false", "elasticsearch", false, "Enable Elasticsearch Search");

        seedConfig("douban.api-key", "", "douban", true, "Douban API Key");

        if (documentCategoryRepository.count() == 0) {
            log.info("Seeding default document categories");
            seedDocumentCategory("🪪 身份证件", "身份证、护照、港澳通行证、驾照等");
            seedDocumentCategory("🏠 房产证件", "房产证、土地使用证、购房合同等");
            seedDocumentCategory("💰 金融证件", "银行卡、存折、股票账户等");
            seedDocumentCategory("📋 合同协议", "租房合同、劳动合同、服务协议等");
            seedDocumentCategory("🎓 证书资质", "毕业证、学位证、职业资格证等");
            seedDocumentCategory("👨‍👩‍👧‍👦 家庭证件", "结婚证、出生证、户口本等");
            seedDocumentCategory("🛡️ 保险保单", "车险保单、重疾险、家财险等");
            seedDocumentCategory("🧾 账单收据", "水电费账单、维修收据、购物小票等");
            seedDocumentCategory("🔧 保修售后", "产品保修卡、售后服务单、维修记录等");
            seedDocumentCategory("📁 其他", "说明书、会员卡、培训证书等");
        }

        if (bookCategoryRepository.count() == 0) {
            log.info("Seeding default book categories");
            seedBookCategory("书籍", "BK", false, "普通书籍，如小说、教材等");
            seedBookCategory("杂志", "MG", true, "期刊杂志");
            seedBookCategory("报刊", "NP", true, "报纸刊物");
        }
    }

    private void seedConfig(String key, String value, String group, boolean sensitive, String description) {
        if (!systemConfigRepository.existsByConfigKey(key)) {
            log.info("Seeding system config: {}", key);
            systemConfigRepository.save(new SystemConfig(key, value, group, sensitive, description));
        }
    }

    private void seedDocumentCategory(String name, String description) {
        if (!documentCategoryRepository.existsByName(name)) {
            log.info("Seeding document category: {}", name);
            documentCategoryRepository.save(new DocumentCategory(name, description));
        }
    }

    private void seedBookCategory(String name, String key, boolean serialized, String description) {
        if (!bookCategoryRepository.existsByName(name)) {
            log.info("Seeding book category: {} ({})", name, key);
            bookCategoryRepository.save(new BookCategory(name, key, serialized, description));
        }
    }
}
