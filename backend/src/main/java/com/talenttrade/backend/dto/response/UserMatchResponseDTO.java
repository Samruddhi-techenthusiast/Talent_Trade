package com.talenttrade.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMatchResponseDTO {

    private Long userId;
    private String userName;

    /** Total computed score — higher means a better trade match. */
    private int matchingScore;

    /** Skills that THIS candidate teaches and the current user wants to learn. */
    private List<String> matchedTeachSkills;

    /** Skills that THIS candidate wants to learn and the current user can teach. */
    private List<String> matchedLearnSkills;
}
