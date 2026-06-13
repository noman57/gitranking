package com.gitranking.scoring;

import com.gitranking.dto.GitHubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PopularityScorerTest {

    private ScoringProperties props;
    private PopularityScorer scorer;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        props = new ScoringProperties();
        scorer = new PopularityScorer(props);
    }

    @Test
    void score_isHigherForMoreStars() {
        GitHubRepository low  = repoWithStats(100,  10, NOW);
        GitHubRepository high = repoWithStats(10000, 10, NOW);

        assertThat(scorer.score(high, NOW)).isGreaterThan(scorer.score(low, NOW));
    }

    @Test
    void score_isHigherForMoreForks() {
        GitHubRepository low  = repoWithStats(100, 10,  NOW);
        GitHubRepository high = repoWithStats(100, 500, NOW);

        assertThat(scorer.score(high, NOW)).isGreaterThan(scorer.score(low, NOW));
    }

    @Test
    void score_decreasesAsRepositoryAges() {
        GitHubRepository recent = repoWithStats(1000, 100, NOW.minus(10, ChronoUnit.DAYS));
        GitHubRepository old    = repoWithStats(1000, 100, NOW.minus(500, ChronoUnit.DAYS));

        assertThat(scorer.score(recent, NOW)).isGreaterThan(scorer.score(old, NOW));
    }

    @Test
    void score_isZero_whenStarsAndForksAreZero() {
        GitHubRepository repo = repoWithStats(0, 0, NOW);

        assertThat(scorer.score(repo, NOW)).isEqualTo(0.0, within(0.0001));
    }

    @Test
    void score_handlesNullCounts_treatingThemAsZero() {
        GitHubRepository repo = new GitHubRepository();
        repo.setUpdatedAt(NOW.toString());

        assertThat(scorer.score(repo, NOW)).isEqualTo(0.0, within(0.0001));
    }

    @Test
    void score_handlesNullUpdatedAt_applyingNoRecencyPenalty() {
        GitHubRepository withDate    = repoWithStats(1000, 100, NOW);
        GitHubRepository withoutDate = repoWithStats(1000, 100, null);

        // withDate uses updatedAt=NOW -> days=0 -> recencyBonus=e^0=1.0
        // null updatedAt -> recencyBonus=1.0 as well, so both scores are equal
        assertThat(scorer.score(withDate, NOW)).isCloseTo(scorer.score(withoutDate, NOW), within(0.0001));
    }

    @Test
    void score_reflectsForksWeightBeingLowerThanStarsWeight() {
        // With linear scoring: moreStars score = 1100*3 + 100*2 = 3500
        //                       moreForks score = 100*3  + 1100*2 = 2500
        // starsWeight (3.0) > forksWeight (2.0), so moreStars wins
        GitHubRepository moreStars = repoWithStats(1100, 100, NOW);
        GitHubRepository moreForks = repoWithStats(100, 1100, NOW);

        assertThat(scorer.score(moreStars, NOW)).isGreaterThan(scorer.score(moreForks, NOW));
    }

    // --- helpers ---

    private GitHubRepository repoWithStats(int stars, int forks, Instant updatedAt) {
        GitHubRepository repo = new GitHubRepository();
        repo.setStargazersCount(stars);
        repo.setForksCount(forks);
        repo.setUpdatedAt(updatedAt != null ? updatedAt.toString() : null);
        return repo;
    }
}
