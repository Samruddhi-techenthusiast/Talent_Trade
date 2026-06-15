package com.talenttrade.backend.service;

import com.talenttrade.backend.dto.request.UpdateProfileRequest;
import com.talenttrade.backend.dto.response.UserProfileResponse;

public interface UserService {

    /**
     * Returns the profile of the currently authenticated user.
     * Resolves identity from the Spring Security context.
     */
    UserProfileResponse getCurrentUserProfile();

    /**
     * Updates profile fields of the currently authenticated user.
     * Only non-null fields in the request are applied (partial update).
     *
     * @param request DTO containing fields to update
     * @return updated UserProfileResponse
     */
    UserProfileResponse updateProfile(UpdateProfileRequest request);

    /**
     * Returns the public profile of any user by their database ID.
     *
     * @param id target user's ID
     * @return UserProfileResponse
     * @throws com.talenttrade.backend.exception.ResourceNotFoundException if no user with that ID exists
     */
    UserProfileResponse getUserById(Long id);
}
