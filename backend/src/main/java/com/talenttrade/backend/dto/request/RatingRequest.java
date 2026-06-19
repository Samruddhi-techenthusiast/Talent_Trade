package com.talenttrade.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RatingRequest {

    @NotNull(message = "Trade id is required")
    private Long tradeId;

    @NotNull(message = "Rated user id is required")
    private Long ratedUserId;

    @NotNull(message = "Stars rating is required")
    @Min(value = 1, message = "Stars must be at least 1")
    @Max(value = 5, message = "Stars must be at most 5")
    private Integer stars;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;

    // NOTE: raterUserId is intentionally absent.
    // The rater is always resolved from the authenticated JWT principal —
    // never trusted from client input.
}
