package com.talenttrade.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dedicated response shape for GET /api/ratings/average/{userId}.
 * Returning a bare double would lose context (no rating count, no userId echo),
 * so this wraps it with enough info for a frontend to render "4.5 (12 reviews)".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AverageRatingResponse {

    private Long userId;
    private double averageRating;
    private long totalRatings;
}
