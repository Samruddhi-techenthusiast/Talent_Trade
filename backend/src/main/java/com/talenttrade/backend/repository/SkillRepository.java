package com.talenttrade.backend.repository;

import com.talenttrade.backend.model.entity.Skill;
import com.talenttrade.backend.model.entity.SkillLevel;
import com.talenttrade.backend.model.entity.SkillType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    // ─────────────────────────────────────────────
    // BASIC USER SKILL QUERIES
    // ─────────────────────────────────────────────

    List<Skill> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Skill> findByUserIdAndLevelOrderByNameAsc(Long userId, SkillLevel level);

    long countByUserId(Long userId);

    List<Skill> findByUserIdAndSkillType(Long userId, SkillType skillType);

    // ─────────────────────────────────────────────
    // OWNERSHIP VALIDATION
    // ─────────────────────────────────────────────

    @Query("SELECT s FROM Skill s WHERE s.id = :skillId AND s.user.id = :userId")
    Optional<Skill> findByIdAndUserId(@Param("skillId") Long skillId,
                                      @Param("userId") Long userId);

    // ─────────────────────────────────────────────
    // DUPLICATE CHECKS (CASE INSENSITIVE)
    // ─────────────────────────────────────────────

    @Query("""
            SELECT COUNT(s) > 0
            FROM Skill s
            WHERE s.user.id = :userId
              AND LOWER(s.name) = LOWER(:name)
              AND s.skillType = :skillType
            """)
    boolean existsByUserIdAndNameIgnoreCaseAndSkillType(@Param("userId") Long userId,
                                                        @Param("name") String name,
                                                        @Param("skillType") SkillType skillType);

    @Query("""
            SELECT COUNT(s) > 0
            FROM Skill s
            WHERE s.user.id = :userId
              AND LOWER(s.name) = LOWER(:name)
              AND s.skillType = :skillType
              AND s.id <> :excludeId
            """)
    boolean existsByUserIdAndNameIgnoreCaseAndSkillTypeAndIdNot(@Param("userId") Long userId,
                                                                @Param("name") String name,
                                                                @Param("skillType") SkillType skillType,
                                                                @Param("excludeId") Long excludeId);

    // ─────────────────────────────────────────────
    // MATCHING MODULE SUPPORT (IMPORTANT)
    // ─────────────────────────────────────────────

    /**
     * Fetch all skills of other users (excluding current user)
     * Used for recommendation/matching system.
     */
    @Query("""
            SELECT s FROM Skill s
            JOIN FETCH s.user u
            WHERE u.id <> :currentUserId
              AND u.enabled = true
            """)
    List<Skill> findAllSkillsExceptUser(@Param("currentUserId") Long currentUserId);
}