package com.talenttrade.backend.controller;

import com.talenttrade.backend.dto.request.CreateTradeRequestDTO;
import com.talenttrade.backend.dto.response.TradeActionResponseDTO;
import com.talenttrade.backend.dto.response.TradeResponseDTO;
import com.talenttrade.backend.security.UserPrincipal;
import com.talenttrade.backend.service.TradeService;
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
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    /**
     * POST /api/trades
     * Creates a new trade request. The sender is always the authenticated user.
     *
     * Request body:
     * {
     *   "receiverId": 5,
     *   "offeredSkillId": 10,
     *   "requestedSkillId": 22,
     *   "message": "Hey! Want to swap React lessons for Java?"
     * }
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TradeResponseDTO> createTradeRequest(
            @Valid @RequestBody CreateTradeRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("POST /api/trades by user id={}", currentUser.getId());
        TradeResponseDTO created = tradeService.createTradeRequest(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/trades/{id}/accept
     * Only the receiver of the trade request can accept it. PENDING -> ACCEPTED.
     */
    @PutMapping("/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TradeActionResponseDTO> acceptTradeRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("PUT /api/trades/{}/accept by user id={}", id, currentUser.getId());
        return ResponseEntity.ok(tradeService.acceptTradeRequest(id, currentUser));
    }

    /**
     * PUT /api/trades/{id}/reject
     * Only the receiver of the trade request can reject it. PENDING -> REJECTED.
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TradeActionResponseDTO> rejectTradeRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("PUT /api/trades/{}/reject by user id={}", id, currentUser.getId());
        return ResponseEntity.ok(tradeService.rejectTradeRequest(id, currentUser));
    }

    /**
     * PUT /api/trades/{id}/cancel
     * Only the sender of the trade request can cancel it. PENDING -> CANCELLED.
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TradeActionResponseDTO> cancelTradeRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("PUT /api/trades/{}/cancel by user id={}", id, currentUser.getId());
        return ResponseEntity.ok(tradeService.cancelTradeRequest(id, currentUser));
    }

    /**
     * PUT /api/trades/{id}/complete
     * Either sender or receiver may complete an ACCEPTED trade. ACCEPTED -> COMPLETED.
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TradeActionResponseDTO> completeTradeRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("PUT /api/trades/{}/complete by user id={}", id, currentUser.getId());
        return ResponseEntity.ok(tradeService.completeTradeRequest(id, currentUser));
    }

    /**
     * GET /api/trades/{id}
     * Returns a single trade. Only a participant (sender or receiver) may view it.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TradeResponseDTO> getTradeById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("GET /api/trades/{} by user id={}", id, currentUser.getId());
        return ResponseEntity.ok(tradeService.getTradeById(id, currentUser));
    }

    /**
     * GET /api/trades/my
     * Returns all trades (sent + received) for the authenticated user.
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TradeResponseDTO>> getMyTrades(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("GET /api/trades/my by user id={}", currentUser.getId());
        return ResponseEntity.ok(tradeService.getMyTrades(currentUser));
    }

    /**
     * GET /api/trades/sent
     * Returns all trades sent by the authenticated user.
     */
    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TradeResponseDTO>> getSentTrades(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("GET /api/trades/sent by user id={}", currentUser.getId());
        return ResponseEntity.ok(tradeService.getSentTrades(currentUser));
    }

    /**
     * GET /api/trades/received
     * Returns all trades received by the authenticated user.
     */
    @GetMapping("/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TradeResponseDTO>> getReceivedTrades(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("GET /api/trades/received by user id={}", currentUser.getId());
        return ResponseEntity.ok(tradeService.getReceivedTrades(currentUser));
    }
}
