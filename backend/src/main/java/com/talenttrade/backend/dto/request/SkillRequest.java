package com.talenttrade.backend.dto.request;

import com.talenttrade.backend.model.entity.SkillLevel;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SkillRequest {

    @NotBlank(message = "Skill name is required")
    @Size(min = 1, max = 100, message = "Skill name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Skill level is required (BEGINNER, INTERMEDIATE, ADVANCED)")
    private SkillLevel level;

    @NotNull(message = "Experience in years is required")
    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience in years cannot exceed 50")
    private Integer experienceInYears;
}

