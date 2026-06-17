package com.talenttrade.backend.controller;

import com.talenttrade.backend.dto.request.SkillRequest;
import com.talenttrade.backend.dto.response.SkillResponse;
import com.talenttrade.backend.security.UserPrincipal;
import com.talenttrade.backend.service.SkillService;
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
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    /**
     * POST /api/skills
     * Add a new skill for the authenticated user.
     *
     * Request body:
     * {
     *   "name": "Spring Boot",
     *   "level": "INTERMEDIATE",
     *   "experienceInYears": 2
     * }
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SkillResponse> addSkill(
            @Valid @RequestBody SkillRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("POST /api/skills by user id={}", currentUser.getId());
        SkillResponse created = skillService.addSkill(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/skills/me
     * Returns all skills of the currently authenticated user.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SkillResponse>> getMySkills(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("GET /api/skills/me by user id={}", currentUser.getId());
        List<SkillResponse> skills = skillService.getMySkills(currentUser);
        return ResponseEntity.ok(skills);
    }

    /**
     * PUT /api/skills/{id}
     * Update an existing skill. Only the owner can update it.
     *
     * Request body:
     * {
     *   "name": "Spring Boot",
     *   "level": "ADVANCED",
     *   "experienceInYears": 3
     * }
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("PUT /api/skills/{} by user id={}", id, currentUser.getId());
        SkillResponse updated = skillService.updateSkill(id, request, currentUser);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/skills/{id}
     * Delete a skill. Only the owner can delete it.
     * Returns 204 No Content on success.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("DELETE /api/skills/{} by user id={}", id, currentUser.getId());
        skillService.deleteSkill(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
