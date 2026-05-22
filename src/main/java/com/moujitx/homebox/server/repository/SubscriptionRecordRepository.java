package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.SubscriptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRecordRepository extends JpaRepository<SubscriptionRecord, Long> {

    List<SubscriptionRecord> findBySubscriptionIdOrderByRecordDateDesc(Long subscriptionId);

    Optional<SubscriptionRecord> findFirstBySubscriptionIdOrderByRecordDateDesc(Long subscriptionId);

    boolean existsByPaymentMethodId(Long paymentMethodId);
}
