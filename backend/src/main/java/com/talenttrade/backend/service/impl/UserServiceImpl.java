package com.talenttrade.backend.service.impl;

import com.talenttrade.backend.dto.request.UpdateProfileRequest;
import com.talenttrade.backend.dto.response.UserProfileResponse;
import com.talenttrade.backend.exception.ResourceNotFoundException;
import com.talenttrade.backend.model.entity.User;
import com.talenttrade.backend.repository.UserRepository;
import com.talenttrade.backend.security.UserPrincipal;
import com.talenttrade.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // ── Public API ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User user = resolveCurrentUser();
        log.debug("Fetching profile for user id={}", user.getId());
        return UserProfileResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        User user = resolveCurrentUser();
        log.info("Updating profile for user id={}", user.getId());

        applyUpdates(user, request);

        User saved = userRepository.save(user);
        log.info("Profile updated successfully for user id={}", saved.getId());
        return UserProfileResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(Long id) {
        log.debug("Fetching public profile for user id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
        return UserProfileResponse.fromEntity(user);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Resolves the currently authenticated user from the Security context
     * and loads the full entity from the database.
     */
    private User resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found in database. Email: " + principal.getEmail()));
    }

    /**
     * Applies only the non-null / non-blank fields from the request to the entity.
     * This enables partial updates — unchanged fields are left as-is.
     */
    private void applyUpdates(User user, UpdateProfileRequest request) {
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        if (request.getBio() != null) {           // allow setting bio to empty string
            user.setBio(request.getBio());
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getLinkedinUrl() != null) {
            user.setLinkedinUrl(request.getLinkedinUrl());
        }
        if (request.getGithubUrl() != null) {
            user.setGithubUrl(request.getGithubUrl());
        }
    }
}
