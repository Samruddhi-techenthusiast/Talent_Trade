package com.talenttrade.backend.service.impl;

import com.talenttrade.backend.dto.request.SkillRequest;
import com.talenttrade.backend.dto.response.SkillResponse;
import com.talenttrade.backend.exception.DuplicateResourceException;
import com.talenttrade.backend.exception.ResourceNotFoundException;
import com.talenttrade.backend.model.entity.Skill;
import com.talenttrade.backend.model.entity.User;
import com.talenttrade.backend.repository.SkillRepository;
import com.talenttrade.backend.repository.UserRepository;
import com.talenttrade.backend.security.UserPrincipal;
import com.talenttrade.backend.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository  userRepository;

    // ── Add Skill ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SkillResponse addSkill(SkillRequest request, UserPrincipal currentUser) {
        log.info("User id={} is adding skill '{}'", currentUser.getId(), request.getName());

        // Guard: no duplicate skill names for the same user (case-insensitive)
        if (skillRepository.existsByUserIdAndNameIgnoreCase(currentUser.getId(), request.getName())) {
            throw new DuplicateResourceException(
                    "You already have a skill named '" + request.getName() + "'. " +
                            "Please update the existing one instead."
            );
        }

        User user = loadUser(currentUser.getId());

        Skill skill = Skill.builder()
                .name(request.getName().trim())
                .level(request.getLevel())
                .experienceInYears(request.getExperienceInYears())
                .user(user)
                .build();

        Skill saved = skillRepository.save(skill);
        log.info("Skill id={} '{}' created for user id={}", saved.getId(), saved.getName(), user.getId());
        return SkillResponse.fromEntity(saved);
    }

    // ── Get My Skills ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getMySkills(UserPrincipal currentUser) {
        log.debug("Fetching all skills for user id={}", currentUser.getId());

        return skillRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(SkillResponse::fromEntity)
                .toList();
    }

    // ── Update Skill ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SkillResponse updateSkill(Long skillId, SkillRequest request, UserPrincipal currentUser) {
        log.info("User id={} is updating skill id={}", currentUser.getId(), skillId);

        // Single query: load skill AND verify ownership at the DB level
        Skill skill = skillRepository
                .findByIdAndUserId(skillId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill not found with id: " + skillId +
                                " or you do not have permission to modify it."
                ));

        // Guard: new name must not clash with another skill already owned by this user
        if (skillRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(
                currentUser.getId(), request.getName(), skillId)) {
            throw new DuplicateResourceException(
                    "You already have another skill named '" + request.getName() + "'."
            );
        }

        skill.setName(request.getName().trim());
        skill.setLevel(request.getLevel());
        skill.setExperienceInYears(request.getExperienceInYears());

        Skill updated = skillRepository.save(skill);
        log.info("Skill id={} updated successfully", updated.getId());
        return SkillResponse.fromEntity(updated);
    }

    // ── Delete Skill ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteSkill(Long skillId, UserPrincipal currentUser) {
        log.info("User id={} is deleting skill id={}", currentUser.getId(), skillId);

        // Single query: verify existence AND ownership before deletion
        Skill skill = skillRepository
                .findByIdAndUserId(skillId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill not found with id: " + skillId +
                                " or you do not have permission to delete it."
                ));

        skillRepository.delete(skill);
        log.info("Skill id={} deleted by user id={}", skillId, currentUser.getId());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));
    }
}
