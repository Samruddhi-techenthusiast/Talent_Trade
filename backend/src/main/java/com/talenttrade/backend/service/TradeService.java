package com.talenttrade.backend.service;

import com.talenttrade.backend.dto.request.CreateTradeRequestDTO;
import com.talenttrade.backend.dto.response.TradeActionResponseDTO;
import com.talenttrade.backend.dto.response.TradeResponseDTO;
import com.talenttrade.backend.security.UserPrincipal;

import java.util.List;

public interface TradeService {

    /**
     * Creates a new trade request from the currently authenticated user
     * to another user.
     *
     * @param request     receiver + offered/requested skill ids + optional message
     * @param currentUser resolved from JWT — always the sender, never from request body
     * @return            the created trade as a response DTO
     * @throws com.talenttrade.backend.exception.InvalidTradeException
     *         if sender == receiver, skill ownership is wrong, or an active
     *         trade already exists between this sender/receiver pair
     */
    TradeResponseDTO createTradeRequest(CreateTradeRequestDTO request, UserPrincipal currentUser);

    /**
     * Accepts a pending trade request. Only the receiver may perform this action.
     * Valid transition: PENDING -> ACCEPTED.
     */
    TradeActionResponseDTO acceptTradeRequest(Long tradeId, UserPrincipal currentUser);

    /**
     * Rejects a pending trade request. Only the receiver may perform this action.
     * Valid transition: PENDING -> REJECTED.
     */
    TradeActionResponseDTO rejectTradeRequest(Long tradeId, UserPrincipal currentUser);

    /**
     * Cancels a pending trade request. Only the sender may perform this action.
     * Valid transition: PENDING -> CANCELLED.
     */
    TradeActionResponseDTO cancelTradeRequest(Long tradeId, UserPrincipal currentUser);

    /**
     * Marks an accepted trade as completed. Either sender or receiver may call this.
     * Valid transition: ACCEPTED -> COMPLETED.
     */
    TradeActionResponseDTO completeTradeRequest(Long tradeId, UserPrincipal currentUser);

    /**
     * Returns a single trade by id.
     * Only a participant (sender or receiver) may view it.
     */
    TradeResponseDTO getTradeById(Long tradeId, UserPrincipal currentUser);

    /**
     * Returns all trades (sent + received) for the currently authenticated user.
     */
    List<TradeResponseDTO> getMyTrades(UserPrincipal currentUser);

    /**
     * Returns all trades sent by the currently authenticated user.
     */
    List<TradeResponseDTO> getSentTrades(UserPrincipal currentUser);

    /**
     * Returns all trades received by the currently authenticated user.
     */
    List<TradeResponseDTO> getReceivedTrades(UserPrincipal currentUser);
}
