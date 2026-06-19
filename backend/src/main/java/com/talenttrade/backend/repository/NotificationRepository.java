package com.talenttrade.backend.repository;

import com.talenttrade.backend.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * All notifications for a user, newest first, paginated.
     * Used by GET /api/notifications.
     */
    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);

    /**
     * Unread notifications for a user, newest first, paginated.
     * Used by GET /api/notifications/unread.
     */
    Page<Notification> findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);

    /**
     * Count of unread notifications — useful for a badge counter on the frontend.
     */
    long countByRecipientUserIdAndIsReadFalse(Long recipientUserId);

    /**
     * Fetch a single notification together with its owning user id,
     * used by the "mark as read" ownership check.
     */
    Optional<Notification> findByIdAndRecipientUserId(Long id, Long recipientUserId);

    /**
     * Bulk update: marks every unread notification for a user as read in one
     * statement, instead of loading N entities into memory and saving each one.
     * Used by PUT /api/notifications/read-all.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true " +
            "WHERE n.recipientUser.id = :userId AND n.isRead = false")
    int markAllAsReadForUser(@Param("userId") Long userId);

    /**
     * Used internally by the matchmaking integration to avoid re-notifying
     * the same pair of users on every recommendation refresh (see Section 10).
     */
    List<Notification> findByRecipientUserIdAndNotificationTypeAndCreatedAtAfter(
            Long recipientUserId,
            com.talenttrade.backend.model.entity.NotificationType notificationType,
            java.time.LocalDateTime after);
}
