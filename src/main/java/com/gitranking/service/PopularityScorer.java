package com.gitranking.service;

import com.gitranking.client.model.GitHubRepository;
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
        log.debug("Scoring '{}' — stars: {}, forks: {}, updatedAt: {}",
                repo.getFullName(), repo.getStargazersCount(), repo.getForksCount(), repo.getUpdatedAt());

        double activityScore = repo.getStargazersCount() * props.starsWeight()
                             + repo.getForksCount()      * props.forksWeight();

        double recency = recencyMultiplier(repo.getUpdatedAt(), now);
        log.debug("Score for '{}': activityScore={}, recencyMultiplier={}, finalScore={}",
                repo.getFullName(), activityScore, recency, activityScore * recency);
        return activityScore * recency;
    }

    private double recencyMultiplier(Instant updatedAt, Instant now) {
        if (updatedAt == null) return 1.0;
        long days = ChronoUnit.DAYS.between(updatedAt, now);
        return Math.exp(-props.recencyDecay() * Math.max(days, 0));
    }
}
