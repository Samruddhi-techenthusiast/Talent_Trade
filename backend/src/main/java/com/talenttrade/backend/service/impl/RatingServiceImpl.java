package com.talenttrade.backend.service.impl;

import com.talenttrade.backend.dto.request.RatingRequest;
import com.talenttrade.backend.dto.response.AverageRatingResponse;
import com.talenttrade.backend.dto.response.RatingResponse;
import com.talenttrade.backend.exception.*;
import com.talenttrade.backend.model.entity.Rating;
import com.talenttrade.backend.model.entity.TradeRequest;
import com.talenttrade.backend.model.entity.TradeStatus;
import com.talenttrade.backend.model.entity.User;
import com.talenttrade.backend.repository.RatingRepository;
import com.talenttrade.backend.repository.TradeRequestRepository;
import com.talenttrade.backend.repository.UserRepository;
import com.talenttrade.backend.security.UserPrincipal;
import com.talenttrade.backend.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.talenttrade.backend.model.entity.NotificationType;
import com.talenttrade.backend.service.NotificationService;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository       ratingRepository;
    private final TradeRequestRepository tradeRequestRepository;
    private final UserRepository         userRepository;
    private final NotificationService notificationService;
    // ── Give Rating ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RatingResponse giveRating(RatingRequest request, UserPrincipal currentUser) {

        Long raterId = currentUser.getId();

        TradeRequest trade = tradeRequestRepository.findById(request.getTradeId())
                .orElseThrow(() -> new TradeNotFoundException(
                        "Trade not found with id: " + request.getTradeId()));

        // Rule: only a COMPLETED trade can be rated
        if (trade.getStatus() != TradeStatus.COMPLETED) {
            throw new InvalidRatingException(
                    "Only completed trades can be rated. Current status: " + trade.getStatus());
        }

        boolean raterIsSender   = trade.getSender().getId().equals(raterId);
        boolean raterIsReceiver = trade.getReceiver().getId().equals(raterId);

        // Rule: the rater must be a participant in this trade
        if (!raterIsSender && !raterIsReceiver) {
            throw new UnauthorizedTradeActionException(
                    "You can only rate trades you participated in.");
        }

        // Rule: ratedUserId must be the OTHER participant — never yourself,
        // and never some unrelated third party slipped into the request body.
        Long expectedRatedUserId = raterIsSender ? trade.getReceiver().getId() : trade.getSender().getId();
        if (!expectedRatedUserId.equals(request.getRatedUserId())) {
            throw new InvalidRatingException(
                    "The rated user must be the other participant in this trade.");
        }

        // Rule: a rater can only rate a given trade once (app-level check;
        // the DB unique constraint on (trade_id, rater_user_id) is the final safety net)
        if (ratingRepository.existsByTradeIdAndRaterUserId(trade.getId(), raterId)) {
            throw new DuplicateRatingException(
                    "You have already submitted a rating for this trade.");
        }

        User rater = loadUser(raterId);
        User ratedUser = loadUser(request.getRatedUserId());

        Rating rating = Rating.builder()
                .raterUser(rater)
                .ratedUser(ratedUser)
                .trade(trade)
                .stars(request.getStars())
                .comment(request.getComment())
                .build();

        Rating saved = ratingRepository.save(rating);

        log.info("Rating id={} created: rater={} rated user={} stars={} on trade={}",
                saved.getId(), raterId, ratedUser.getId(), saved.getStars(), trade.getId());

        String stars = "★".repeat(request.getStars())
                + "☆".repeat(5 - request.getStars());

        notificationService.createNotification(
                ratedUser,
                "New Rating Received",
                rater.getFullName() + " rated you " + stars
                        + " (" + request.getStars() + "/5)"
                        + (request.getComment() != null && !request.getComment().isBlank()
                        ? ": \"" + request.getComment() + "\""
                        : "."),
                NotificationType.RATING_RECEIVED
        );

        return RatingResponse.fromEntity(saved);
    }

    // ── Get User Ratings ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getUserRatings(Long userId) {
        log.debug("Fetching ratings received by user id={}", userId);

        return ratingRepository.findByRatedUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(RatingResponse::fromEntity)
                .toList();
    }

    // ── Get Average Rating ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AverageRatingResponse getAverageRating(Long userId) {
        log.debug("Calculating average rating for user id={}", userId);

        double average = ratingRepository.findAverageRatingByUserId(userId)
                .orElse(0.0); // AVG() returns SQL NULL for zero rows — default to 0.0

        long total = ratingRepository.countByRatedUserId(userId);

        return AverageRatingResponse.builder()
                .userId(userId)
                .averageRating(roundToOneDecimal(average))
                .totalRatings(total)
                .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
