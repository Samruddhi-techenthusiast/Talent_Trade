package com.talenttrade.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "skills",
        uniqueConstraints = {
                // A user cannot have the same skill name + type twice
                // (e.g. "Java" as TEACH and "Java" as LEARN are both allowed,
                // but "Java" TEACH twice is not)
                @UniqueConstraint(
                        name = "uk_user_skill_name_type",
                        columnNames = {"user_id", "name", "skill_type"}
                )
        },
        indexes = {
                @Index(name = "idx_skill_user_id", columnList = "user_id"),
                @Index(name = "idx_skill_name_type", columnList = "name, skill_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkillLevel level;

    @Column(nullable = false)
    private Integer experienceInYears;

    /**
     * NEW — distinguishes whether this row is a skill the user can TEACH
     * or a skill the user wants to LEARN. Defaults to TEACH for backward
     * compatibility with skills created before this column existed.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "skill_type", nullable = false, length = 10)
    @Builder.Default
    private SkillType skillType = SkillType.TEACH;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
