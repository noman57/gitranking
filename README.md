# GitRanking

Search GitHub repositories and enrich each result with a **popularity score** based on stars, forks, and recency of updates.

## How It Works

Each repository returned from GitHub is scored using:

```
score = (stars × 3 + forks × 2) × e^(-0.001 × daysSinceLastUpdate)
```

Results are returned in GitHub's native order. The score is attached to each item so clients can apply their own ranking or filtering.

## Getting Started

### Prerequisites

- Java 21
- Maven 3.x

### Configuration

Set your GitHub Personal Access Token in `application.properties` or as an environment variable:

```properties
github.token=<your-github-pat>
```

Without a token the app runs unauthenticated (30 requests/min rate limit). With a token the limit increases to 5,000 requests/min.

### Run

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080`.

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
  "incompleteResults": false,
  "items": [
    {
      "name": "spring-projects/spring-boot",
      "url": "https://github.com/spring-projects/spring-boot",
      "popularityScore": 218947.32
    }
  ]
}
```

### Health Check

```
GET /actuator/health
```

## Configuration Reference

| Property | Default | Description |
|---|---|---|
| `github.token` | _(empty)_ | GitHub PAT for authenticated requests |
| `scoring.stars-weight` | `3.0` | Weight applied to star count |
| `scoring.forks-weight` | `2.0` | Weight applied to fork count |
| `scoring.recency-decay` | `0.001` | Exponential decay rate for recency (higher = stronger penalty for old repos) |

## Running Tests

```bash
mvn test
```
