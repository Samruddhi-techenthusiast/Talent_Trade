package com.talenttrade.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTradeRequestDTO {

    @NotNull(message = "Receiver id is required")
    private Long receiverId;

    @NotNull(message = "Offered skill id is required")
    private Long offeredSkillId;

    @NotNull(message = "Requested skill id is required")
    private Long requestedSkillId;

    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;

    // NOTE: senderId is intentionally absent.
    // The sender is always resolved from the authenticated JWT principal
    // in the controller — never trusted from client input.
}
