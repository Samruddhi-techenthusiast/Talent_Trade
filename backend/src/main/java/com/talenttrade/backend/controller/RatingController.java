package com.talenttrade.backend.controller;

import com.talenttrade.backend.dto.request.RatingRequest;
import com.talenttrade.backend.dto.response.AverageRatingResponse;
import com.talenttrade.backend.dto.response.RatingResponse;
import com.talenttrade.backend.security.UserPrincipal;
import com.talenttrade.backend.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    /**
     * POST /api/ratings
     * Submits a rating for a completed trade. The rater is always the
     * authenticated user — never accepted from the request body.
     *
     * Request body:
     * {
     *   "tradeId": 14,
     *   "ratedUserId": 7,
     *   "stars": 5,
     *   "comment": "Great Java mentor, very patient!"
     * }
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RatingResponse> giveRating(
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("POST /api/ratings by user id={}", currentUser.getId());
        RatingResponse created = ratingService.giveRating(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/ratings/user/{userId}
     * Returns all ratings received by a given user, newest first.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RatingResponse>> getUserRatings(@PathVariable Long userId) {
        log.info("GET /api/ratings/user/{}", userId);
        List<RatingResponse> ratings = ratingService.getUserRatings(userId);
        return ResponseEntity.ok(ratings);
    }

    /**
     * GET /api/ratings/average/{userId}
     * Returns the average star rating and total rating count for a user.
     */
    @GetMapping("/average/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AverageRatingResponse> getAverageRating(@PathVariable Long userId) {
        log.info("GET /api/ratings/average/{}", userId);
        AverageRatingResponse average = ratingService.getAverageRating(userId);
        return ResponseEntity.ok(average);
    }
}
