package com.talenttrade.backend.service;

import com.talenttrade.backend.dto.request.RatingRequest;
import com.talenttrade.backend.dto.response.AverageRatingResponse;
import com.talenttrade.backend.dto.response.RatingResponse;
import com.talenttrade.backend.security.UserPrincipal;

import java.util.List;

public interface RatingService {

    /**
     * Submits a rating for a completed trade.
     *
     * Enforced rules:
     *  - the trade must exist and be in COMPLETED status
     *  - the rater must be a participant (sender or receiver) of the trade
     *  - the ratedUserId must be the OTHER participant, not the rater themselves
     *  - the rater cannot have already rated this trade (DB-level + app-level check)
     *  - stars must be between 1 and 5 (enforced via @Valid on the controller)
     *
     * @param request     trade id, rated user id, stars, optional comment
     * @param currentUser resolved from JWT — always the rater, never from request body
     * @return            the created rating as a response DTO
     */
    RatingResponse giveRating(RatingRequest request, UserPrincipal currentUser);

    /**
     * Returns all ratings received by a given user, newest first.
     *
     * @param userId target user id
     */
    List<RatingResponse> getUserRatings(Long userId);

    /**
     * Returns the average star rating and total rating count for a user.
     * If the user has no ratings yet, averageRating is 0.0 and totalRatings is 0.
     *
     * @param userId target user id
     */
    AverageRatingResponse getAverageRating(Long userId);
}
