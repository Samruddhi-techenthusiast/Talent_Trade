package com.talenttrade.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.talenttrade.backend.model.entity.TradeRequest;
import com.talenttrade.backend.model.entity.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeResponseDTO {

    private Long tradeId;

    private Long senderId;
    private String senderName;

    private Long receiverId;
    private String receiverName;

    private Long offeredSkillId;
    private String offeredSkill;

    private Long requestedSkillId;
    private String requestedSkill;

    private String message;

    private TradeStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a TradeRequest entity to its response DTO.
     * Keeps all mapping logic centralized — entities never leave the service layer.
     */
    public static TradeResponseDTO fromEntity(TradeRequest trade) {
        return TradeResponseDTO.builder()
                .tradeId(trade.getId())
                .senderId(trade.getSender().getId())
                .senderName(trade.getSender().getFullName())
                .receiverId(trade.getReceiver().getId())
                .receiverName(trade.getReceiver().getFullName())
                .offeredSkillId(trade.getOfferedSkill().getId())
                .offeredSkill(trade.getOfferedSkill().getName())
                .requestedSkillId(trade.getRequestedSkill().getId())
                .requestedSkill(trade.getRequestedSkill().getName())
                .message(trade.getMessage())
                .status(trade.getStatus())
                .createdAt(trade.getCreatedAt())
                .updatedAt(trade.getUpdatedAt())
                .build();
    }
}
