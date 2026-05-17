package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.Notification;
import com.moujitx.homebox.server.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByIsReadFalse();

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id AND n.isRead = false")
    int markRead(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.isRead = false")
    int markAllRead();

    boolean existsByTypeAndTitleAndContent(NotificationType type, String title, String content);
}
