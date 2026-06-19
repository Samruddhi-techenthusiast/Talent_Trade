package com.talenttrade.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.talenttrade.backend.model.entity.Rating;
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
public class RatingResponse {

    private Long id;

    private Long tradeId;

    private Long raterUserId;
    private String raterUserName;

    private Long ratedUserId;
    private String ratedUserName;

    private Integer stars;
    private String comment;

    private LocalDateTime createdAt;

    /**
     * Converts a Rating entity to its response DTO.
     * Keeps mapping logic centralized — entities never leave the service layer.
     */
    public static RatingResponse fromEntity(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .tradeId(rating.getTrade().getId())
                .raterUserId(rating.getRaterUser().getId())
                .raterUserName(rating.getRaterUser().getFullName())
                .ratedUserId(rating.getRatedUser().getId())
                .ratedUserName(rating.getRatedUser().getFullName())
                .stars(rating.getStars())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
