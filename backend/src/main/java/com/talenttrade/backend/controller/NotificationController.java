package com.talenttrade.backend.controller;

import com.talenttrade.backend.dto.response.MarkAllReadResponse;
import com.talenttrade.backend.dto.response.NotificationPageResponse;
import com.talenttrade.backend.dto.response.NotificationResponse;
import com.talenttrade.backend.security.UserPrincipal;
import com.talenttrade.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    /**
     * GET /api/notifications?page=0&size=20
     * Returns all notifications for the authenticated user, newest first, paginated.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationPageResponse> getNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/notifications by user id={} page={} size={}", currentUser.getId(), page, size);
        Pageable pageable = buildPageable(page, size);
        return ResponseEntity.ok(notificationService.getNotifications(currentUser, pageable));
    }

    /**
     * GET /api/notifications/unread?page=0&size=20
     * Returns unread notifications for the authenticated user, newest first, paginated.
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationPageResponse> getUnreadNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("GET /api/notifications/unread by user id={} page={} size={}", currentUser.getId(), page, size);
        Pageable pageable = buildPageable(page, size);
        return ResponseEntity.ok(notificationService.getUnreadNotifications(currentUser, pageable));
    }

    /**
     * PUT /api/notifications/{id}/read
     * Marks a single notification as read. Only the recipient may do this.
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("PUT /api/notifications/{}/read by user id={}", id, currentUser.getId());
        return ResponseEntity.ok(notificationService.markAsRead(id, currentUser));
    }

    /**
     * PUT /api/notifications/read-all
     * Marks every unread notification belonging to the authenticated user as read.
     */
    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MarkAllReadResponse> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("PUT /api/notifications/read-all by user id={}", currentUser.getId());
        return ResponseEntity.ok(notificationService.markAllAsRead(currentUser));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Builds a Pageable with sane bounds — caps page size so a client can't
     * request, say, size=100000 and force a huge in-memory result set.
     */
    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize == 0 ? DEFAULT_PAGE_SIZE : safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
