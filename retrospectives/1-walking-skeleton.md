# Retro 1: Developers can build, check and ship the service

## Caught by review

ponytail-review (verdict: ship):

1. CI token had default permissions. Fixed: workflow sets `permissions: contents: read`.
2. Gradle distribution not pinned by checksum. Fixed: `distributionSha256Sum` in `gradle-wrapper.properties`
   (value from services.gradle.org).

/code-review, Standards (no hard violations):

1. `io.spring.dependency-management` plugin not in STANDARDS §1. Justified in the PR: it is how the Boot plugin
   applies Boot's dependency versions; the alternative (`platform(...)` BOM) is the same thing spelled by hand.
2. No retro yet. This file.
3. Smoke test hits `/actuator/health`, not a calculation. Not fixed: no endpoint exists yet; item 2 adds the
   happy-path request.
4. `spring-boot-starter-validation` unused. Fixed: removed; item 2 adds it if request validation needs it.
5. "DTOs don't leak into domain" covered only by the layered rule. Not fixed: the layered rule already fails the
   build if domain touches the web adapter; a second rule would say the same thing.
6. Package patterns repeated in `ArchitectureTest`. Not fixed: three short strings in one file, next to each other;
   constants would add indirection for no safety.
7. `package-info.java` files only restate STANDARDS §2. Fixed: deleted; item 2's classes create the packages.
8. Empty-layer shortcut must be removed in item 2. Kept, tracked under Shortcuts taken.

/code-review, Spec (no missing requirements):

1. CI half of "passes locally and in CI" unproven. Proven by this PR's CI run.
2. Health checked via MockMvc, not a live `bootRun`. Not fixed: STANDARDS §6 asks for one `@SpringBootTest` smoke
   test; MockMvc over the full context exercises the same actuator endpoint.
3. Domain rule lists only Spring, Jakarta and Jackson, not "any framework". Not fixed: those are every framework
   STANDARDS §2 names and every one on the classpath.
4. No `application.yaml`. Not fixed: there are no local-development defaults to hold yet.
5. Unused validation starter and `package-info` files. Fixed (see Standards 4 and 7).
6. Wrapper not checksum-pinned. Fixed (see ponytail 2). The reviewer also said CI does not validate the wrapper;
   wrong: `gradle/actions/setup-gradle@v5` validates the wrapper jar by default.

## Surprises

None. The build passed first time. Both failure checks were confirmed by hand: a domain class importing Spring
fails `ArchitectureTest > domainIsPlainJava`, and an unformatted file fails `spotlessJavaCheck`.

## Shortcuts taken

- `src/test/java/com/paulrule/mathapi/ArchitectureTest.java:15`: `allowEmptyShould` / `withOptionalLayers`, so the
  layer rules pass while the layers are empty. Limit: until then, an empty or misnamed package is not caught.
  Upgrade: remove both in item 2, once every layer has a class.

## Lessons

- An outcome-based item still pulled in implementation habits from the old task backlog (validation starter,
  layer marker files). Add only what a "done when" check needs.
- The process paused between steps waiting for a go-ahead; CLAUDE.md now says to run an item end to end and stop
  only when the user is needed.
