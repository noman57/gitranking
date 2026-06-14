package com.gitranking.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configurable weights and decay factor for the repository popularity scoring algorithm.
 *
 * <p>Score formula:
 * <pre>
 *   score = (stars × starsWeight + forks × forksWeight) * recencyBonus
 *   recencyBonus = e^(-recencyDecay * daysSinceUpdate)
 * </pre>
 * Default weights produce: score = (stars×3 + forks×2) × recencyBonus.
 * Exponential decay penalises repositories that have not been updated recently.
 */
@ConfigurationProperties(prefix = "scoring")
public record ScoringProperties(
        /** Weight applied to the raw star count. */
        double starsWeight,
        /** Weight applied to the raw fork count. */
        double forksWeight,
        /**
         * Exponential decay constant for recency. Higher values penalise older updates more aggressively.
         * A value of 0.001 causes a ~10% reduction after roughly 105 days.
         */
        double recencyDecay
) {
    public ScoringProperties() {
        this(3.0, 2.0, 0.001);
    }
}
