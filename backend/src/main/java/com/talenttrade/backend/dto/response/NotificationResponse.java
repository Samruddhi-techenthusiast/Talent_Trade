package com.talenttrade.backend.dto.response;

import com.talenttrade.backend.model.entity.Notification;
import com.talenttrade.backend.model.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType notificationType;
    private boolean isRead;
    private LocalDateTime createdAt;

    /**
     * Converts a Notification entity to its response DTO.
     * Note: recipientUser is intentionally NOT exposed — the caller already
     * knows it's their own notification (enforced at the query level), so
     * echoing it back is unnecessary payload.
     */
    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notificationType(notification.getNotificationType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
