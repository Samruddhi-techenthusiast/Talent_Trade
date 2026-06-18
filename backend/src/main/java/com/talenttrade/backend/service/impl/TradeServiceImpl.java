package com.talenttrade.backend.service.impl;

import com.talenttrade.backend.dto.request.CreateTradeRequestDTO;
import com.talenttrade.backend.dto.response.TradeActionResponseDTO;
import com.talenttrade.backend.dto.response.TradeResponseDTO;
import com.talenttrade.backend.exception.InvalidTradeException;
import com.talenttrade.backend.exception.ResourceNotFoundException;
import com.talenttrade.backend.exception.TradeNotFoundException;
import com.talenttrade.backend.exception.UnauthorizedTradeActionException;
import com.talenttrade.backend.model.entity.Skill;
import com.talenttrade.backend.model.entity.TradeRequest;
import com.talenttrade.backend.model.entity.TradeStatus;
import com.talenttrade.backend.model.entity.User;
import com.talenttrade.backend.repository.SkillRepository;
import com.talenttrade.backend.repository.TradeRequestRepository;
import com.talenttrade.backend.repository.UserRepository;
import com.talenttrade.backend.security.UserPrincipal;
import com.talenttrade.backend.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {

    private final TradeRequestRepository tradeRequestRepository;
    private final UserRepository         userRepository;
    private final SkillRepository        skillRepository;

    /** Trade statuses considered "active" for duplicate-request prevention. */
    private static final List<TradeStatus> ACTIVE_STATUSES =
            List.of(TradeStatus.PENDING, TradeStatus.ACCEPTED);

    // ── Create Trade Request ─────────────────────────────────────────────────

    @Override
    @Transactional
    public TradeResponseDTO createTradeRequest(CreateTradeRequestDTO request, UserPrincipal currentUser) {

        Long senderId = currentUser.getId();

        // Rule: sender and receiver cannot be the same user
        if (senderId.equals(request.getReceiverId())) {
            throw new InvalidTradeException("You cannot send a trade request to yourself.");
        }

        User sender = loadUser(senderId);
        User receiver = loadUser(request.getReceiverId()); // also enforces "receiver must exist"

        Skill offeredSkill = loadSkill(request.getOfferedSkillId());
        Skill requestedSkill = loadSkill(request.getRequestedSkillId());

        // Rule: offered skill must belong to sender
        if (!offeredSkill.getUser().getId().equals(senderId)) {
            throw new InvalidTradeException("The offered skill must belong to you (the sender).");
        }

        // Rule: requested skill must belong to receiver
        if (!requestedSkill.getUser().getId().equals(receiver.getId())) {
            throw new InvalidTradeException("The requested skill must belong to the receiver.");
        }

        // Rule: no duplicate active (PENDING/ACCEPTED) trade between this sender/receiver pair
        if (tradeRequestRepository.existsActiveTradeBetween(senderId, receiver.getId(), ACTIVE_STATUSES)) {
            throw new InvalidTradeException(
                    "An active trade request already exists between you and this user. " +
                            "Please wait for it to be resolved before sending a new one.");
        }

        TradeRequest trade = TradeRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .offeredSkill(offeredSkill)
                .requestedSkill(requestedSkill)
                .message(request.getMessage())
                .status(TradeStatus.PENDING)
                .build();

        TradeRequest saved = tradeRequestRepository.save(trade);
        log.info("Trade id={} created: sender={} -> receiver={}", saved.getId(), senderId, receiver.getId());

        return TradeResponseDTO.fromEntity(saved);
    }

    // ── Accept Trade Request ──────────────────────────────────────────────────

    @Override
    @Transactional
    public TradeActionResponseDTO acceptTradeRequest(Long tradeId, UserPrincipal currentUser) {
        TradeRequest trade = loadTrade(tradeId);

        if (!trade.getReceiver().getId().equals(currentUser.getId())) {
            throw new UnauthorizedTradeActionException(
                    "Only the receiver of this trade request can accept it.");
        }

        validateTransition(trade.getStatus(), TradeStatus.ACCEPTED);

        TradeStatus previous = trade.getStatus();
        trade.setStatus(TradeStatus.ACCEPTED);
        tradeRequestRepository.save(trade);

        log.info("Trade id={} accepted by user id={}", tradeId, currentUser.getId());
        return TradeActionResponseDTO.of(tradeId, previous, TradeStatus.ACCEPTED,
                "Trade request accepted successfully.");
    }

    // ── Reject Trade Request ──────────────────────────────────────────────────

    @Override
    @Transactional
    public TradeActionResponseDTO rejectTradeRequest(Long tradeId, UserPrincipal currentUser) {
        TradeRequest trade = loadTrade(tradeId);

        if (!trade.getReceiver().getId().equals(currentUser.getId())) {
            throw new UnauthorizedTradeActionException(
                    "Only the receiver of this trade request can reject it.");
        }

        validateTransition(trade.getStatus(), TradeStatus.REJECTED);

        TradeStatus previous = trade.getStatus();
        trade.setStatus(TradeStatus.REJECTED);
        tradeRequestRepository.save(trade);

        log.info("Trade id={} rejected by user id={}", tradeId, currentUser.getId());
        return TradeActionResponseDTO.of(tradeId, previous, TradeStatus.REJECTED,
                "Trade request rejected.");
    }

    // ── Cancel Trade Request ─────────────────────────────────────────────────

    @Override
    @Transactional
    public TradeActionResponseDTO cancelTradeRequest(Long tradeId, UserPrincipal currentUser) {
        TradeRequest trade = loadTrade(tradeId);

        // Rule: only the SENDER can cancel (distinct from reject, which is the receiver's call)
        if (!trade.getSender().getId().equals(currentUser.getId())) {
            throw new UnauthorizedTradeActionException(
                    "Only the sender of this trade request can cancel it.");
        }

        validateTransition(trade.getStatus(), TradeStatus.CANCELLED);

        TradeStatus previous = trade.getStatus();
        trade.setStatus(TradeStatus.CANCELLED);
        tradeRequestRepository.save(trade);

        log.info("Trade id={} cancelled by sender id={}", tradeId, currentUser.getId());
        return TradeActionResponseDTO.of(tradeId, previous, TradeStatus.CANCELLED,
                "Trade request cancelled.");
    }

    // ── Complete Trade Request ───────────────────────────────────────────────

    @Override
    @Transactional
    public TradeActionResponseDTO completeTradeRequest(Long tradeId, UserPrincipal currentUser) {
        TradeRequest trade = loadTrade(tradeId);

        boolean isParticipant = trade.getSender().getId().equals(currentUser.getId())
                || trade.getReceiver().getId().equals(currentUser.getId());

        if (!isParticipant) {
            throw new UnauthorizedTradeActionException(
                    "Only a participant of this trade can mark it as completed.");
        }

        validateTransition(trade.getStatus(), TradeStatus.COMPLETED);

        TradeStatus previous = trade.getStatus();
        trade.setStatus(TradeStatus.COMPLETED);
        tradeRequestRepository.save(trade);

        log.info("Trade id={} marked COMPLETED by user id={}", tradeId, currentUser.getId());
        return TradeActionResponseDTO.of(tradeId, previous, TradeStatus.COMPLETED,
                "Trade marked as completed successfully.");
    }

    // ── Get Trade By Id ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public TradeResponseDTO getTradeById(Long tradeId, UserPrincipal currentUser) {
        TradeRequest trade = loadTrade(tradeId);

        boolean isParticipant = trade.getSender().getId().equals(currentUser.getId())
                || trade.getReceiver().getId().equals(currentUser.getId());

        if (!isParticipant) {
            throw new UnauthorizedTradeActionException(
                    "You do not have permission to view this trade.");
        }

        return TradeResponseDTO.fromEntity(trade);
    }

    // ── Get My / Sent / Received Trades ──────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponseDTO> getMyTrades(UserPrincipal currentUser) {
        return tradeRequestRepository.findBySenderIdOrReceiverId(currentUser.getId())
                .stream()
                .map(TradeResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponseDTO> getSentTrades(UserPrincipal currentUser) {
        return tradeRequestRepository.findBySenderIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(TradeResponseDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TradeResponseDTO> getReceivedTrades(UserPrincipal currentUser) {
        return tradeRequestRepository.findByReceiverIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(TradeResponseDTO::fromEntity)
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Skill loadSkill(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + skillId));
    }

    private TradeRequest loadTrade(Long tradeId) {
        return tradeRequestRepository.findById(tradeId)
                .orElseThrow(() -> new TradeNotFoundException("Trade request not found with id: " + tradeId));
    }

    /**
     * Centralized status-transition guard. Valid transitions:
     *   PENDING  -> ACCEPTED
     *   PENDING  -> REJECTED
     *   PENDING  -> CANCELLED
     *   ACCEPTED -> COMPLETED
     * Any other transition (including acting on an already-terminal state) is rejected.
     */
    private void validateTransition(TradeStatus current, TradeStatus target) {
        boolean isValid = switch (target) {
            case ACCEPTED, REJECTED, CANCELLED -> current == TradeStatus.PENDING;
            case COMPLETED                     -> current == TradeStatus.ACCEPTED;
            case PENDING                       -> false; // nothing transitions back to PENDING
        };

        if (!isValid) {
            throw new InvalidTradeException(
                    String.format("Invalid status transition: cannot move from %s to %s.", current, target));
        }
    }
}
