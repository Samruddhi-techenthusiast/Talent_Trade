package com.talenttrade.backend.controller;

import com.talenttrade.backend.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class TestController {

    /**
     * Test endpoint for any authenticated user.
     * GET /api/user/profile
     */
    @GetMapping("/user/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(Map.of(
                "id", currentUser.getId(),
                "email", currentUser.getEmail(),
                "fullName", currentUser.getFullName(),
                "roles", currentUser.getAuthorities()
        ));
    }

    /**
     * Admin-only test endpoint.
     * GET /api/admin/dashboard
     */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getAdminDashboard() {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to the Talent Trade Admin Dashboard!",
                "status", "active"
        ));
    }

    /**
     * General authenticated endpoint.
     * GET /api/hello
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(Map.of(
                "message", "Hello, " + currentUser.getFullName() + "! Welcome to Talent Trade."
        ));
    }
}