package com.moujitx.homebox.server.initializer;

import com.moujitx.homebox.server.entity.Role;
import com.moujitx.homebox.server.entity.User;
import com.moujitx.homebox.server.repository.RoleRepository;
import com.moujitx.homebox.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.root.username}")
    private String rootUsername;

    @Value("${app.root.password}")
    private String rootPassword;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
    }
}
