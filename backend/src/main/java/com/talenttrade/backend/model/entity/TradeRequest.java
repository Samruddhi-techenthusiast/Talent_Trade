package com.talenttrade.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "trade_requests",
        indexes = {
                @Index(name = "idx_trade_sender",   columnList = "sender_id"),
                @Index(name = "idx_trade_receiver", columnList = "receiver_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who initiates the trade (taken from JWT, never from request body).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * User who receives the trade request.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /**
     * Skill that the sender is offering to the receiver.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offered_skill_id", nullable = false)
    private Skill offeredSkill;

    /**
     * Skill that the sender wants from the receiver in exchange.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_skill_id", nullable = false)
    private Skill requestedSkill;

    /**
     * Optional personal note the sender can attach to the trade request
     * (e.g. "Hey, I'd love to swap React lessons for your Java expertise!").
     */
    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TradeStatus status = TradeStatus.PENDING;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
