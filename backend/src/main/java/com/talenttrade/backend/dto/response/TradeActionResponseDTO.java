package com.talenttrade.backend.dto.response;

import com.talenttrade.backend.model.entity.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight confirmation response for state-changing actions
 * (accept / reject / complete), instead of returning the full trade object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeActionResponseDTO {

    private Long tradeId;
    private TradeStatus previousStatus;
    private TradeStatus newStatus;
    private String message;

    public static TradeActionResponseDTO of(Long tradeId, TradeStatus previous,
                                            TradeStatus updated, String message) {
        return TradeActionResponseDTO.builder()
                .tradeId(tradeId)
                .previousStatus(previous)
                .newStatus(updated)
                .message(message)
                .build();
    }
}
