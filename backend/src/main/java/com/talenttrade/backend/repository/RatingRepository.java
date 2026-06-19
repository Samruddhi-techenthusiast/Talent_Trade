package com.talenttrade.backend.repository;

import com.talenttrade.backend.model.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * All ratings received BY a given user, newest first.
     * Used by GET /api/ratings/user/{userId}.
     */
    List<Rating> findByRatedUserIdOrderByCreatedAtDesc(Long ratedUserId);

    /**
     * Duplicate-prevention check: has this rater already rated this specific trade?
     * Called before inserting a new rating — backed by the DB unique constraint too.
     */
    Optional<Rating> findByTradeIdAndRaterUserId(Long tradeId, Long raterUserId);

    boolean existsByTradeIdAndRaterUserId(Long tradeId, Long raterUserId);

    /**
     * Computes the average star rating for a user.
     * Returns null (via Optional) if the user has no ratings yet —
     * AVG() on an empty set returns SQL NULL, not 0, so this must be handled
     * in the service layer to avoid a NullPointerException on unboxing.
     */
    @Query("SELECT AVG(r.stars) FROM Rating r WHERE r.ratedUser.id = :userId")
    Optional<Double> findAverageRatingByUserId(@Param("userId") Long userId);

    /**
     * Total number of ratings received by a user — used alongside the average
     * so the API can report "4.5 stars (12 reviews)" instead of a bare number.
     */
    long countByRatedUserId(Long userId);
}
