package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    Optional<PaymentMethod> findByName(String name);

    boolean existsByName(String name);

    boolean existsByLogoFileId(Long fileId);
}
