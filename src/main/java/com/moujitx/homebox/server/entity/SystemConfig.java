package com.moujitx.homebox.server.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_config")
@Getter
@Setter
@NoArgsConstructor
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String configKey;

    @Column(columnDefinition = "TEXT")
    private String configValue;

    @Column(nullable = false, length = 50)
    private String configGroup;

    @Column(nullable = false)
    private boolean isSensitive;

    @Column(length = 255)
    private String description;

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public SystemConfig(String configKey, String configValue, String configGroup, boolean isSensitive, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configGroup = configGroup;
        this.isSensitive = isSensitive;
        this.description = description;
    }
}
