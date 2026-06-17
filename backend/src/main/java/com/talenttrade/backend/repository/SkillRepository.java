package com.talenttrade.backend.repository;

import com.talenttrade.backend.model.entity.Skill;
import com.talenttrade.backend.model.entity.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /**
     * All skills belonging to a specific user, ordered newest-first.
     */
    List<Skill> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Fetch a skill by id and eagerly verify ownership in the same query.
     * Used in update/delete to avoid an extra round-trip.
     */
    @Query("SELECT s FROM Skill s WHERE s.id = :skillId AND s.user.id = :userId")
    Optional<Skill> findByIdAndUserId(@Param("skillId") Long skillId,
                                      @Param("userId")  Long userId);

    /**
     * Guard against duplicate skill names per user (case-insensitive).
     * Called before adding a new skill.
     */
    @Query("""
            SELECT COUNT(s) > 0
            FROM Skill s
            WHERE s.user.id = :userId
              AND LOWER(s.name) = LOWER(:name)
            """)
    boolean existsByUserIdAndNameIgnoreCase(@Param("userId") Long userId,
                                            @Param("name")   String name);

    /**
     * Same as above but excludes the current skill — used during update
     * to allow keeping the same name while changing level/years.
     */
    @Query("""
            SELECT COUNT(s) > 0
            FROM Skill s
            WHERE s.user.id = :userId
              AND LOWER(s.name) = LOWER(:name)
              AND s.id <> :excludeId
            """)
    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(@Param("userId")    Long userId,
                                                    @Param("name")      String name,
                                                    @Param("excludeId") Long excludeId);

    /**
     * Optional: filter a user's skills by level (e.g. for a "find experts" feature later).
     */
    List<Skill> findByUserIdAndLevelOrderByNameAsc(Long userId, SkillLevel level);

    /**
     * Count how many skills a user has registered.
     */
    long countByUserId(Long userId);
}
