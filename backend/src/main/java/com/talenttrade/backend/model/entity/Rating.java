package com.talenttrade.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ratings",
        uniqueConstraints = {
                // A rater can only rate a given trade once — enforces "once per trade" at the DB level,
                // not just in application code.
                @UniqueConstraint(
                        name = "uk_trade_rater",
                        columnNames = {"trade_id", "rater_user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_rating_rated_user", columnList = "rated_user_id"),
                @Index(name = "idx_rating_trade", columnList = "trade_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user GIVING the rating (the authenticated caller).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rater_user_id", nullable = false)
    private User raterUser;

    /**
     * The user BEING rated — must be the other participant in the trade.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rated_user_id", nullable = false)
    private User ratedUser;

    /**
     * The completed trade this rating is tied to.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trade_id", nullable = false)
    private TradeRequest trade;

    @Column(nullable = false)
    private Integer stars;

    @Column(length = 1000)
    private String comment;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
