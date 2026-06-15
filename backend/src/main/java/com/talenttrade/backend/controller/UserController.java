package com.talenttrade.backend.controller;

import com.talenttrade.backend.dto.request.UpdateProfileRequest;
import com.talenttrade.backend.dto.response.UserProfileResponse;
import com.talenttrade.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users/me
     * Returns the full profile of the currently authenticated user.
     * Requires: valid JWT token in Authorization header.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        log.info("GET /api/users/me called");
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/users/me
     * Updates profile fields of the currently authenticated user.
     * All fields are optional — only provided fields are updated.
     * Requires: valid JWT token in Authorization header.
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("PUT /api/users/me called");
        UserProfileResponse updated = userService.updateProfile(request);
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /api/users/{id}
     * Returns the public profile of any user by ID.
     * Requires: valid JWT token in Authorization header.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} called", id);
        UserProfileResponse profile = userService.getUserById(id);
        return ResponseEntity.ok(profile);
    }
}
