package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.SubscriptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRecordRepository extends JpaRepository<SubscriptionRecord, Long> {

    List<SubscriptionRecord> findBySubscriptionIdOrderByStartDateDesc(Long subscriptionId);

    Optional<SubscriptionRecord> findFirstBySubscriptionIdOrderByStartDateDesc(Long subscriptionId);

    boolean existsByPaymentMethodId(Long paymentMethodId);

    boolean existsByOrderNo(String orderNo);

    boolean existsByOrderNoAndIdNot(String orderNo, Long id);
}
