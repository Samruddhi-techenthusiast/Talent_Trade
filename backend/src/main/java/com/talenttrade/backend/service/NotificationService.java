package com.talenttrade.backend.service;

import com.talenttrade.backend.dto.response.MarkAllReadResponse;
import com.talenttrade.backend.dto.response.NotificationPageResponse;
import com.talenttrade.backend.dto.response.NotificationResponse;
import com.talenttrade.backend.model.entity.NotificationType;
import com.talenttrade.backend.model.entity.User;
import com.talenttrade.backend.security.UserPrincipal;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    // ── Internal API — called by OTHER services, never exposed via a controller ──

    /**
     * Creates and persists a notification for a single recipient.
     * This is the single entry point every other module (Trade, Matchmaking,
     * Rating) calls to notify a user of an event. There is intentionally no
     * public "POST /api/notifications" endpoint — notifications are always
     * system-generated, never client-submitted.
     *
     * @param recipient        the user who should receive the notification
     * @param title             short headline (e.g. "New Trade Request")
     * @param message           full notification body
     * @param notificationType  category, used for client-side icon/routing logic
     */
    void createNotification(User recipient, String title, String message, NotificationType notificationType);

    // ── Public API — backs the REST controller ───────────────────────────────

    /**
     * Returns a paginated list of all notifications for the authenticated user,
     * newest first.
     */
    NotificationPageResponse getNotifications(UserPrincipal currentUser, Pageable pageable);

    /**
     * Returns a paginated list of unread notifications for the authenticated user,
     * newest first.
     */
    NotificationPageResponse getUnreadNotifications(UserPrincipal currentUser, Pageable pageable);

    /**
     * Marks a single notification as read.
     * Only the recipient of the notification may perform this action.
     *
     * @throws com.talenttrade.backend.exception.NotificationNotFoundException
     *         if no notification with that id exists for this user
     */
    NotificationResponse markAsRead(Long notificationId, UserPrincipal currentUser);

    /**
     * Marks every unread notification belonging to the authenticated user as read.
     * Uses a single bulk UPDATE statement rather than loading each row individually.
     */
    MarkAllReadResponse markAllAsRead(UserPrincipal currentUser);
}
