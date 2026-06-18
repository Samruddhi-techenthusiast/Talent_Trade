package com.talenttrade.backend.repository;

import com.talenttrade.backend.model.entity.TradeRequest;
import com.talenttrade.backend.model.entity.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRequestRepository extends JpaRepository<TradeRequest, Long> {

    /**
     * All trades sent by a given user (used by GET /api/trades/sent).
     */
    List<TradeRequest> findBySenderIdOrderByCreatedAtDesc(Long senderId);

    /**
     * All trades received by a given user (used by GET /api/trades/received).
     */
    List<TradeRequest> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    /**
     * All trades with a given status — useful for admin views / reporting.
     */
    List<TradeRequest> findByStatus(TradeStatus status);

    /**
     * All trades where the user is either the sender OR the receiver.
     * Used by GET /api/trades/my.
     */
    @Query("""
            SELECT t FROM TradeRequest t
            WHERE t.sender.id = :userId OR t.receiver.id = :userId
            ORDER BY t.createdAt DESC
            """)
    List<TradeRequest> findBySenderIdOrReceiverId(@Param("userId") Long userId);

    /**
     * Duplicate-prevention check: does an active (PENDING or ACCEPTED) trade
     * already exist between this exact sender and receiver pair?
     * Called before creating a new trade request.
     */
    @Query("""
            SELECT COUNT(t) > 0 FROM TradeRequest t
            WHERE t.sender.id = :senderId
              AND t.receiver.id = :receiverId
              AND t.status IN :activeStatuses
            """)
    boolean existsActiveTradeBetween(@Param("senderId") Long senderId,
                                     @Param("receiverId") Long receiverId,
                                     @Param("activeStatuses") List<TradeStatus> activeStatuses);

    /**
     * Exact match variant requested in spec — checks for an existing trade
     * between a sender/receiver pair with one specific status.
     */
    boolean existsBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, TradeStatus status);
}
