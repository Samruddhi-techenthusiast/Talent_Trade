package com.talenttrade.backend.service.impl;

import com.talenttrade.backend.dto.response.MarkAllReadResponse;
import com.talenttrade.backend.dto.response.NotificationPageResponse;
import com.talenttrade.backend.dto.response.NotificationResponse;
import com.talenttrade.backend.exception.NotificationNotFoundException;
import com.talenttrade.backend.model.entity.Notification;
import com.talenttrade.backend.model.entity.NotificationType;
import com.talenttrade.backend.model.entity.User;
import com.talenttrade.backend.repository.NotificationRepository;
import com.talenttrade.backend.security.UserPrincipal;
import com.talenttrade.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // ── Internal creation API ────────────────────────────────────────────────

    @Override
    @Transactional
    public void createNotification(User recipient, String title, String message,
                                   NotificationType notificationType) {

        Notification notification = Notification.builder()
                .recipientUser(recipient)
                .title(title)
                .message(message)
                .notificationType(notificationType)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notification created: type={} recipient={} title='{}'",
                notificationType, recipient.getId(), title);

        // NOTE: This is a same-transaction, synchronous write. If notification
        // volume grows large, this is the natural seam to swap in an async
        // event (e.g. Spring ApplicationEventPublisher + @Async / a message
        // queue) without touching any of the calling services — they only
        // depend on this method's signature, not its execution model.
    }

    // ── Public query API ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(UserPrincipal currentUser, Pageable pageable) {
        Page<NotificationResponse> page = notificationRepository
                .findByRecipientUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(NotificationResponse::fromEntity);

        return NotificationPageResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getUnreadNotifications(UserPrincipal currentUser, Pageable pageable) {
        Page<NotificationResponse> page = notificationRepository
                .findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(NotificationResponse::fromEntity);

        return NotificationPageResponse.fromPage(page);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, UserPrincipal currentUser) {
        // Ownership is enforced AT THE QUERY LEVEL: the lookup itself is scoped
        // to (id AND recipientUserId), so a user can never even discover whether
        // another user's notification id exists — they just get a clean 404.
        Notification notification = notificationRepository
                .findByIdAndRecipientUserId(notificationId, currentUser.getId())
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found with id: " + notificationId));

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("Notification id={} marked as read by user id={}", notificationId, currentUser.getId());
        }

        return NotificationResponse.fromEntity(notification);
    }

    @Override
    @Transactional
    public MarkAllReadResponse markAllAsRead(UserPrincipal currentUser) {
        int updatedCount = notificationRepository.markAllAsReadForUser(currentUser.getId());
        log.info("Marked {} notifications as read for user id={}", updatedCount, currentUser.getId());

        return MarkAllReadResponse.builder()
                .updatedCount(updatedCount)
                .message(updatedCount == 0
                        ? "No unread notifications to update."
                        : updatedCount + " notification(s) marked as read.")
                .build();
    }
}
