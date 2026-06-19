package com.talenttrade.backend.service.impl;

import com.talenttrade.backend.dto.response.UserMatchResponseDTO;
import com.talenttrade.backend.model.entity.Skill;
import com.talenttrade.backend.model.entity.SkillType;
import com.talenttrade.backend.model.entity.User;
import com.talenttrade.backend.repository.SkillRepository;
import com.talenttrade.backend.repository.UserRepository;
import com.talenttrade.backend.security.UserPrincipal;
import com.talenttrade.backend.service.MatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.talenttrade.backend.model.entity.Notification;
import com.talenttrade.backend.model.entity.NotificationType;
import com.talenttrade.backend.repository.NotificationRepository;
import com.talenttrade.backend.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final UserRepository  userRepository;
    private final SkillRepository skillRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    // ── Scoring constants ────────────────────────────────────────────────────
    private static final int FIRST_TEACH_MATCH_SCORE   = 50; // candidate teaches something I want to learn
    private static final int FIRST_LEARN_MATCH_SCORE   = 50; // candidate wants to learn something I teach
    private static final int ADDITIONAL_MATCH_SCORE    = 10; // each extra overlap beyond the first, per side
    private static final int SAME_LOCATION_BONUS       = 20;
    private static final int MAX_RECOMMENDATIONS       = 10;

    @Override
    @Transactional(readOnly = true)
    public List<UserMatchResponseDTO> getRecommendedUsers(UserPrincipal currentUser) {

        Long currentUserId = currentUser.getId();

        User me = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in database. id=" + currentUserId));

        // My own skill sets, split by type (lowercased for case-insensitive comparison)
        Set<String> myTeachSkills = toLowerNameSet(
                skillRepository.findByUserIdAndSkillType(currentUserId, SkillType.TEACH));
        Set<String> myLearnSkills = toLowerNameSet(
                skillRepository.findByUserIdAndSkillType(currentUserId, SkillType.LEARN));

        if (myTeachSkills.isEmpty() && myLearnSkills.isEmpty()) {
            log.info("User id={} has no skills configured — no recommendations possible", currentUserId);
            return List.of();
        }

        // ── Single bulk query: every OTHER user's skills, fetched with JOIN FETCH ──
        // This avoids the N+1 problem — one round trip instead of one query per candidate.
        List<Skill> allOtherSkills = skillRepository.findAllSkillsExceptUser(currentUserId);

        // Group skills by candidate user id, so we can process one candidate at a time
        // entirely in memory with zero further DB calls.
        Map<Long, List<Skill>> skillsByUser = allOtherSkills.stream()
                .collect(Collectors.groupingBy(s -> s.getUser().getId()));

        List<UserMatchResponseDTO> results = new ArrayList<>();

        for (Map.Entry<Long, List<Skill>> entry : skillsByUser.entrySet()) {
            List<Skill> candidateSkills = entry.getValue();
            User candidate = candidateSkills.get(0).getUser(); // same User instance for every row (JOIN FETCH)

            UserMatchResponseDTO match = computeMatch(me, candidate, candidateSkills, myTeachSkills, myLearnSkills);

            if (match.getMatchingScore() > 0) {
                results.add(match);
            }
        }

        // Sort by score descending, cap at top N
        results.sort(Comparator.comparingInt(UserMatchResponseDTO::getMatchingScore).reversed());

        List<UserMatchResponseDTO> top = results.size() > MAX_RECOMMENDATIONS
                ? results.subList(0, MAX_RECOMMENDATIONS)
                : results;

        log.info("Computed {} candidate matches for user id={}, returning top {}",
                results.size(), currentUserId, top.size());
        notifyNewMatches(me, top);
        return top;
    }

    // ── Core scoring logic for a single candidate ────────────────────────────

    /**
     * Computes the match score and matched-skill lists between the current
     * user ("me") and one candidate.
     *
     * Two independent overlaps are scored:
     *   Side A: candidate TEACHES something I want to LEARN
     *   Side B: candidate wants to LEARN something I TEACH
     * Each side scores +50 for its first match and +10 for every match after that.
     * A flat +20 location bonus is added if both users share the same location.
     */
    private UserMatchResponseDTO computeMatch(User me, User candidate, List<Skill> candidateSkills,
                                              Set<String> myTeachSkills, Set<String> myLearnSkills) {

        Set<String> candidateTeachSkills = toLowerNameSet(
                filterByType(candidateSkills, SkillType.TEACH));
        Set<String> candidateLearnSkills = toLowerNameSet(
                filterByType(candidateSkills, SkillType.LEARN));

        // Side A: what candidate teaches that I want to learn
        List<String> matchedTeachSkills = intersectPreservingCase(candidateSkills, SkillType.TEACH, myLearnSkills);

        // Side B: what candidate wants to learn that I teach
        List<String> matchedLearnSkills = intersectPreservingCase(candidateSkills, SkillType.LEARN, myTeachSkills);

        int score = 0;
        score += scoreSide(matchedTeachSkills.size());
        score += scoreSide(matchedLearnSkills.size());

        if (isSameLocation(me, candidate)) {
            score += SAME_LOCATION_BONUS;
        }

        return UserMatchResponseDTO.builder()
                .userId(candidate.getId())
                .userName(candidate.getFullName())
                .matchingScore(score)
                .matchedTeachSkills(matchedTeachSkills)
                .matchedLearnSkills(matchedLearnSkills)
                .build();
    }

    /**
     * Scores one side of the match: +50 for the first overlapping skill,
     * +10 for each additional one. Zero overlaps = zero points for this side.
     */
    private int scoreSide(int overlapCount) {
        if (overlapCount == 0) {
            return 0;
        }
        int firstMatchScore = FIRST_TEACH_MATCH_SCORE; // same constant value for both sides
        int additional = overlapCount - 1;
        return firstMatchScore + (additional * ADDITIONAL_MATCH_SCORE);
    }

    /**
     * Returns the candidate's skills of the given type whose lowercased name
     * appears in `targetSet`, preserving the ORIGINAL casing for display purposes.
     */
    private List<String> intersectPreservingCase(List<Skill> candidateSkills, SkillType type, Set<String> targetSet) {
        return candidateSkills.stream()
                .filter(s -> s.getSkillType() == type)
                .filter(s -> targetSet.contains(s.getName().toLowerCase()))
                .map(Skill::getName)
                .distinct()
                .toList();
    }

    private List<Skill> filterByType(List<Skill> skills, SkillType type) {
        return skills.stream().filter(s -> s.getSkillType() == type).toList();
    }

    private Set<String> toLowerNameSet(List<Skill> skills) {
        return skills.stream()
                .map(s -> s.getName().toLowerCase())
                .collect(Collectors.toSet());
    }

    private boolean isSameLocation(User me, User candidate) {
        return me.getLocation() != null
                && candidate.getLocation() != null
                && me.getLocation().equalsIgnoreCase(candidate.getLocation());
    }
    private void notifyNewMatches(User me, List<UserMatchResponseDTO> topMatches) {

        final int NOTIFY_THRESHOLD = 100;
        LocalDateTime since = LocalDateTime.now().minusHours(24);

        List<Notification> recentMatchNotifications =
                notificationRepository.findByRecipientUserIdAndNotificationTypeAndCreatedAtAfter(
                        me.getId(),
                        NotificationType.NEW_MATCH_FOUND,
                        since
                );

        Set<String> recentlyNotifiedMessages = recentMatchNotifications.stream()
                .map(Notification::getMessage)
                .collect(Collectors.toSet());

        for (UserMatchResponseDTO match : topMatches) {

            if (match.getMatchingScore() < NOTIFY_THRESHOLD) {
                continue;
            }

            String message = match.getUserName()
                    + " is a strong skill-trade match for you (score: "
                    + match.getMatchingScore()
                    + "). Check out their profile!";

            if (recentlyNotifiedMessages.contains(message)) {
                continue;
            }

            notificationService.createNotification(
                    me,
                    "New Match Found",
                    message,
                    NotificationType.NEW_MATCH_FOUND
            );
        }
    }
}
