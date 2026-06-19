package com.talenttrade.backend.controller;

import com.talenttrade.backend.dto.response.UserMatchResponseDTO;
import com.talenttrade.backend.security.UserPrincipal;
import com.talenttrade.backend.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchingService matchingService;

    /**
     * GET /api/match/recommendations
     * Returns the top skill-trade matches for the currently authenticated user,
     * sorted by score descending. The user is always resolved from the JWT —
     * never accepted as a request parameter.
     */
    @GetMapping("/recommendations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserMatchResponseDTO>> getRecommendations(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("GET /api/match/recommendations by user id={}", currentUser.getId());
        List<UserMatchResponseDTO> recommendations = matchingService.getRecommendedUsers(currentUser);
        return ResponseEntity.ok(recommendations);
    }
}