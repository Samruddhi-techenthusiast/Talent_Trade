package com.talenttrade.backend.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @Size(max = 500, message = "Profile image URL must not exceed 500 characters")
    @Pattern(
            regexp = "^(https?://.*)?$",
            message = "Profile image URL must be a valid HTTP/HTTPS URL"
    )
    private String profileImageUrl;

    @Size(max = 300, message = "LinkedIn URL must not exceed 300 characters")
    @Pattern(
            regexp = "^(https?://(www\\.)?linkedin\\.com/.*)?$",
            message = "LinkedIn URL must be a valid LinkedIn profile URL"
    )
    private String linkedinUrl;

    @Size(max = 300, message = "GitHub URL must not exceed 300 characters")
    @Pattern(
            regexp = "^(https?://(www\\.)?github\\.com/.*)?$",
            message = "GitHub URL must be a valid GitHub profile URL"
    )
    private String githubUrl;
}
