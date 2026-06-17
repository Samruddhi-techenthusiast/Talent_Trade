package com.talenttrade.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.talenttrade.backend.model.entity.Skill;
import com.talenttrade.backend.model.entity.SkillLevel;
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
public class SkillResponse {

    private Long id;
    private String name;
    private SkillLevel level;
    private Integer experienceInYears;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a Skill entity to SkillResponse DTO.
     * Keeps all mapping logic in one place — no MapStruct needed.
     */
    public static SkillResponse fromEntity(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .level(skill.getLevel())
                .experienceInYears(skill.getExperienceInYears())
                .userId(skill.getUser().getId())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }
}
