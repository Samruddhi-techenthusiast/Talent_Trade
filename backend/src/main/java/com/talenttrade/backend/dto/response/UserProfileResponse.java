package com.talenttrade.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.talenttrade.backend.model.entity.User;
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
public class UserProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String bio;
    private String location;
    private String profileImageUrl;
    private String linkedinUrl;
    private String githubUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Static factory method — converts a User entity to UserProfileResponse.
     * Keeps mapping logic in one place; no MapStruct dependency needed.
     */
    public static UserProfileResponse fromEntity(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .bio(user.getBio())
                .location(user.getLocation())
                .profileImageUrl(user.getProfileImageUrl())
                .linkedinUrl(user.getLinkedinUrl())
                .githubUrl(user.getGithubUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
