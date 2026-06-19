package com.talenttrade.backend.service;

import com.talenttrade.backend.dto.response.UserMatchResponseDTO;
import com.talenttrade.backend.security.UserPrincipal;

import java.util.List;

public interface MatchingService {

    /**
     * Computes skill-trade match recommendations for the currently
     * authenticated user against every other active user in the system.
     *
     * Scoring (see MatchingServiceImpl for the authoritative logic):
     *   +50  candidate teaches a skill the current user wants to learn
     *   +50  candidate wants to learn a skill the current user teaches
     *   +10  each additional overlapping skill match beyond the first on each side
     *   +20  candidate is in the same location as the current user (bonus)
     *
     * @param currentUser resolved from JWT
     * @return top matches sorted by score descending, capped at 10 results,
     *         excluding candidates with a score of 0 (no overlap at all)
     */
    List<UserMatchResponseDTO> getRecommendedUsers(UserPrincipal currentUser);
}