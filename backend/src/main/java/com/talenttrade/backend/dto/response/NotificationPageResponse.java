package com.talenttrade.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wraps a Spring Data Page into a clean, frontend-friendly shape.
 * Returning the raw Page object directly is discouraged in production APIs
 * because its JSON structure is tied to the Spring Data version and leaks
 * internal pagination implementation details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPageResponse {

    private List<NotificationResponse> notifications;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean isLast;

    public static NotificationPageResponse fromPage(Page<NotificationResponse> page) {
        return NotificationPageResponse.builder()
                .notifications(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isLast(page.isLast())
                .build();
    }
}
