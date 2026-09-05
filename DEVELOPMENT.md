# Development Guide

## Requirements
- JDK 17
- Maven 3.9+

## Setup — enable pre-commit hook (once per clone)
```bash
git config core.hooksPath .githooks
```

## Common commands
| Task | Command |
|------|---------|
| Compile | `mvn compile` |
| Run tests | `mvn test` |
| Full quality gate (tests + coverage + style + bugs) | `mvn verify` |
| Auto-format code | `mvn spotless:apply` |
| Format check only | `mvn spotless:check` |
| Style check | `mvn checkstyle:check` |
| Static bug analysis | `mvn spotbugs:check` |
| Bug report (HTML/GUI) | `mvn spotbugs:gui` |
| Coverage report | open `target/site/jacoco/index.html` after `mvn verify` |

## Quality gates enforced
- **Spotless** — Google Java Format. CI fails if unformatted.
- **Checkstyle** — see `checkstyle.xml`. Warnings fail the build.
- **SpotBugs** — effort=Max, threshold=Low.
- **JaCoCo** — line coverage ≥ 70% (`jacoco.line.coverage.min` in `pom.xml`).

All four run in `mvn verify`, in the pre-commit hook (Spotless + Checkstyle only, for speed), and in GitHub Actions on every push and PR to `main`.

## Adjusting the coverage threshold
Edit `<jacoco.line.coverage.min>` in `pom.xml`. Start lax while codebase is small, tighten as it grows.
