package com.gitranking.scoring;

import com.gitranking.dto.GitHubRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Assigns a popularity score to a GitHub repository.
 *
 * <p>Formula:
 * <pre>
 *   score = (stars * starsWeight + forks * forksWeight) * recencyBonus
 *   recencyBonus = e^(-recencyDecay * daysSinceLastUpdate)
 * </pre>
 * Raw star and fork counts are used directly (no log scaling). Exponential decay
 * penalises repositories that have not been updated recently.
 */
@Slf4j
@Component
public class PopularityScorer {

    private final ScoringProperties props;

    public PopularityScorer(ScoringProperties props) {
        this.props = props;
    }

    /**
     * Computes the popularity score for the given repository.
     *
     * @param repo the repository to score
     * @param now  reference instant used for recency calculation (injected for testability)
     * @return popularity score >= 0
     */
    public double score(GitHubRepository repo, Instant now) {
        log.debug("Scoring '{}' — stars: {}, forks: {}, updatedAt: {}", repo.getFullName(), repo.getStargazersCount(), repo.getForksCount(), repo.getUpdatedAt());
        double activityScore = safe(repo.getStargazersCount()) * props.getStarsWeight()
                             + safe(repo.getForksCount())      * props.getForksWeight();

        double recency = recencyMultiplier(repo.getUpdatedAt(), now);
        log.debug("Score for '{}': activityScore={}, recencyMultiplier={}, finalScore={}", repo.getFullName(), activityScore, recency, activityScore * recency);
        return activityScore * recency;
    }

    private double recencyMultiplier(String updatedAt, Instant now) {
        if (updatedAt == null || updatedAt.isBlank()) return 1.0;
        try {
            long days = ChronoUnit.DAYS.between(Instant.parse(updatedAt), now);
            return Math.exp(-props.getRecencyDecay() * Math.max(days, 0));
        } catch (Exception e) {
            log.debug("Could not parse updatedAt '{}', defaulting recency multiplier to 1.0", updatedAt);
            return 1.0;
        }
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
