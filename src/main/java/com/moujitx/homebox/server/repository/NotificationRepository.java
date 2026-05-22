package com.moujitx.homebox.server.repository;

import com.moujitx.homebox.server.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Notification> findByIsReadOrderByCreatedAtDesc(boolean isRead, Pageable pageable);

    long countByIsReadFalse();

    @Query("SELECT n FROM Notification n WHERE n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnread(Pageable pageable);

    @Modifying
    @Query(value = """
            INSERT INTO notifications (type, title, content, is_read, created_at, source_type, source_id, notify_date)
            VALUES (:type, :title, :content, false, NOW(), :sourceType, :sourceId, :notifyDate)
            """, nativeQuery = true)
    int insert(@Param("type") String type,
               @Param("title") String title,
               @Param("content") String content,
               @Param("sourceType") String sourceType,
               @Param("sourceId") Long sourceId,
               @Param("notifyDate") LocalDate notifyDate);
}
