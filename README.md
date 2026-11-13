# math-api (outcome-based backlog, vibe coded)

A rebuild of [`math-api`](https://github.com/prule/math-api) from the same PRD, domain model and standards as
[`math-api-vibe-coded`](https://github.com/prule/math-api-vibe-coded), with one change: the
[backlog](docs/BACKLOG.md) states **outcomes** (who gets what, and how we know) instead of implementation prompts.
The builder chooses the design.

This is the vibe build: no OpenSpec, no agents. Each item is prompted directly, then reviewed (ponytail +
`/code-review`), fixed, given a retro in `retrospectives/`, and merged by PR (see [CLAUDE.md](CLAUDE.md)). An
OpenSpec build works the same outcome backlog, so the two can be compared.

A stateless REST service for exact decimal arithmetic. See [docs/PRD.md](docs/PRD.md).

## Comparing the builds

Once both builds have finished the same backlog items, both are scanned with SonarQube using the same quality
profile (bugs, code smells, duplication, cognitive complexity, coverage). Neither build runs Sonar during its own
process, so the scan is a neutral yardstick, not something either build was tuned against.
