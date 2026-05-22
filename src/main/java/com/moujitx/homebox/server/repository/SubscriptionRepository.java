package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.Subscription;
import com.moujitx.homebox.server.enums.SubscriptionStatus;
import com.moujitx.homebox.server.enums.SubscriptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s JOIN FETCH s.platform WHERE " +
            "(:search IS NULL OR s.name LIKE %:search%) AND " +
            "(:type IS NULL OR s.subscriptionType = :type) AND " +
            "(:status IS NULL OR s.status = :status) AND " +
            "(:platformId IS NULL OR s.platform.id = :platformId)")
    Page<Subscription> findWithFilters(@Param("search") String search,
                                       @Param("type") SubscriptionType type,
                                       @Param("status") SubscriptionStatus status,
                                       @Param("platformId") Long platformId,
                                       Pageable pageable);

    boolean existsByPlatformId(Long platformId);

    @Query("SELECT s FROM Subscription s WHERE s.subscriptionType = 'PERIODIC' AND s.status = 'ACTIVE'")
    List<Subscription> findActivePeriodicSubscriptions();

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE'")
    long countActive();

    @Query("SELECT COALESCE(SUM(sr.amount), 0) FROM SubscriptionRecord sr WHERE sr.recordDate >= :startDate AND sr.recordDate <= :endDate")
    BigDecimal sumAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
