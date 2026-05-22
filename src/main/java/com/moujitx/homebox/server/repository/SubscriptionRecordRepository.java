package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.SubscriptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRecordRepository extends JpaRepository<SubscriptionRecord, Long> {

    List<SubscriptionRecord> findBySubscriptionIdOrderByRecordDateDesc(Long subscriptionId);

    @Query("SELECT sr FROM SubscriptionRecord sr WHERE sr.subscription.id = :subscriptionId ORDER BY sr.recordDate DESC")
    Optional<SubscriptionRecord> findLatestBySubscriptionId(@Param("subscriptionId") Long subscriptionId);
}
