# GitRanking

Search GitHub repositories and enrich each result with a **popularity score** based on stars, forks, and recency of updates.

## How It Works

Each repository returned from GitHub is scored using:

```
score = (stars × 3 + forks × 2) × e^(-0.001 × daysSinceLastUpdate)
```

Results are returned in GitHub's native order. The score is attached to each item so clients can apply their own ranking or filtering.

## Getting Started

### Run with Docker (recommended)

```bash
docker compose up --build
```

The API is available at `http://localhost:8080`. No local Java or Maven installation required.

To pass a GitHub token:

```bash
GITHUB_TOKEN=<your-github-pat> docker compose up --build
```

### Docker commands

```bash
# Start the full stack (builds the app image)
docker compose up --build

# Start in background
docker compose up --build -d

# View logs
docker compose logs -f app

# Stop
docker compose down

# Rebuild after code changes
docker compose up --build app
```

### Run locally

**Prerequisites:** Java 21, Maven 3.x

Set your GitHub Personal Access Token in `application.properties`:

```properties
github.token=<your-github-pat>
```

Start a Redis instance (required for caching):

```bash
docker compose up redis
```

Then run the app:

```bash
mvn spring-boot:run
```

Without a token the app runs unauthenticated (30 requests/min rate limit). With a token the limit increases to 5,000 requests/min.

## API

Full spec: [`docs/openapi.yaml`](docs/openapi.yaml)

### Search Repositories

```
GET /repositories
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `language` | string | No | Filter by programming language (e.g. `java`) |
| `createdAfter` | date | No | Only repos created on or after this date (`yyyy-MM-dd`) |
| `perPage` | integer | No | Results per page, default `30`, max `100` |
| `page` | integer | No | Page number, default `1` |

**Example request:**
```
GET /repositories?language=java&createdAfter=2023-01-01&perPage=10
```

**Example response:**
```json
{
  "page": 1,
  "perPage": 10,
  "totalCount": 28471,
  "items": [
    {
      "name": "spring-projects/spring-boot",
      "url": "https://github.com/spring-projects/spring-boot",
      "popularityScore": 218947.32
    }
  ]
}
```

**curl examples:**

```bash
# Search Java repositories created after 2023
curl "http://localhost:8080/repositories?language=java&createdAfter=2023-01-01&perPage=10"

# Search with pagination
curl "http://localhost:8080/repositories?language=python&perPage=5&page=2"

# No filters — top repositories
curl "http://localhost:8080/repositories"
```

### Health Check

```
GET /actuator/health
```

## Design Decisions

### OpenFeign Client

GitHub API calls are made via a declarative [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign) client (`GitHubClient`). The interface maps directly to `GET /search/repositories` — no boilerplate HTTP code. Standard GitHub headers (`Accept`, `X-GitHub-Api-Version`) and the bearer token are injected automatically on every request via a `RequestInterceptor` configured in `GitHubFeignConfig`.

A custom `ErrorDecoder` (`GitHubApiErrorDecoder`) intercepts non-2xx responses before they reach application code and translates them into typed domain exceptions:

| HTTP Status | Exception |
|---|---|
| 401, 403 | `GitHubAuthException` |
| 429 | `GitHubRateLimitException` |
| 500, 502, 503, 504 | `GitHubUpstreamException` |

### Global Error Handling

A `@RestControllerAdvice` (`GlobalExceptionHandler`) catches all domain exceptions and maps them to safe, human-readable HTTP responses. Raw GitHub API error bodies and internal stack traces are written to the log only — they are never included in the response body.

| Exception | HTTP Status | Client Message |
|---|---|---|
| `GitHubRateLimitException` | 429 | "Rate limit exceeded. Please wait before retrying." |
| `GitHubAuthException` | 502 | "Repository search is temporarily unavailable. Please try again later." |
| `GitHubUpstreamException` | 502 | "Repository search is temporarily unavailable. Please try again later." |
| Invalid parameter | 400 | "Invalid value for parameter '...'." |
| Unexpected | 500 | "An unexpected error occurred. Please try again later." |

### Retry Mechanism

[Resilience4j](https://resilience4j.readme.io/) retries failed GitHub API calls automatically via the `@Retry(name = "githubSearch")` annotation on `RepositorySearchService.search()`.

| Setting | Value |
|---|---|
| Max attempts | 3 |
| Wait between retries | 500ms |
| Retry on | `GitHubUpstreamException`, `IOException` |
| Do not retry | `GitHubAuthException`, `GitHubRateLimitException` |

Auth and rate-limit failures are not retried — they will not resolve by retrying and would only burn rate-limit quota further.

### Redis Cache

GitHub search results are cached in Redis via Spring Cache (`@Cacheable`) on `RepositorySearchService.search()`. The cache key is the full combination of request parameters — different queries are cached independently.

| Setting | Value |
|---|---|
| TTL | 1 hour |
| Eviction policy | `allkeys-lfu` (least frequently used) |
| Memory limit | 256 MB |
| Serialization | JSON (Jackson) |

LFU eviction means popular searches (high access frequency) stay cached longer and are the last to be evicted under memory pressure.

### Why Not MapStruct?

MapStruct was considered for mapping `GitHubRepository` (the internal GitHub API DTO) to `RepositoryResult` (the API response DTO). The mapping is a single constructor call:

```java
new RepositoryResult(repo.getFullName(), repo.getHtmlUrl(), scorer.score(repo, now))
```

Adding MapStruct would introduce an annotation processor, a mapper interface, and build-time code generation to replace one line of code. The rule applied here: don't add a framework to solve a problem the language already solves cleanly.

---

## Configuration Reference

| Property | Default | Description |
|---|---|---|
| `github.token` | _(empty)_ | GitHub PAT for authenticated requests |
| `github.api.url` | `https://api.github.com` | GitHub API base URL |
| `scoring.stars-weight` | `3.0` | Weight applied to star count |
| `scoring.forks-weight` | `2.0` | Weight applied to fork count |
| `scoring.recency-decay` | `0.001` | Exponential decay rate for recency (higher = stronger penalty for old repos) |
| `spring.data.redis.host` | `localhost` | Redis host |
| `spring.data.redis.port` | `6379` | Redis port |

## Running Tests

```bash
mvn test
```

Cache integration tests (`RepositorySearchCacheIT`) require Docker to spin up a Redis container via Testcontainers.
