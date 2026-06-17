package com.talenttrade.backend.service;

import com.talenttrade.backend.dto.request.SkillRequest;
import com.talenttrade.backend.dto.response.SkillResponse;
import com.talenttrade.backend.security.UserPrincipal;

import java.util.List;

public interface SkillService {

    /**
     * Adds a new skill for the currently authenticated user.
     *
     * @param request       validated skill data
     * @param currentUser   injected from JWT via @AuthenticationPrincipal
     * @return              the created SkillResponse
     * @throws com.talenttrade.backend.exception.DuplicateResourceException
     *         if the user already has a skill with the same name
     */
    SkillResponse addSkill(SkillRequest request, UserPrincipal currentUser);

    /**
     * Returns all skills belonging to the currently authenticated user.
     *
     * @param currentUser   injected from JWT via @AuthenticationPrincipal
     * @return              list of SkillResponse, ordered newest-first
     */
    List<SkillResponse> getMySkills(UserPrincipal currentUser);

    /**
     * Updates an existing skill.
     * Only the owner of the skill is allowed to perform this operation.
     *
     * @param skillId       ID of the skill to update
     * @param request       updated skill data
     * @param currentUser   injected from JWT via @AuthenticationPrincipal
     * @return              updated SkillResponse
     * @throws com.talenttrade.backend.exception.ResourceNotFoundException
     *         if the skill does not exist or does not belong to the current user
     */
    SkillResponse updateSkill(Long skillId, SkillRequest request, UserPrincipal currentUser);

    /**
     * Deletes a skill by ID.
     * Only the owner of the skill is allowed to perform this operation.
     *
     * @param skillId       ID of the skill to delete
     * @param currentUser   injected from JWT via @AuthenticationPrincipal
     * @throws com.talenttrade.backend.exception.ResourceNotFoundException
     *         if the skill does not exist or does not belong to the current user
     */
    void deleteSkill(Long skillId, UserPrincipal currentUser);
}
