# Retro 5: Developers can integrate from the API docs alone

## Caught by review

Before review (my own read of the live spec): springdoc's `ProblemDetail` schema had a nested `properties` object
that Spring never sends, and `format: uri` for relative type URIs. Fixed in `OpenApiConfig.problemDetailsAsSent`,
with test `describesProblemDetailsAsTheyAreSent`.

ponytail-review (verdict: fix 1 first):

1. The hand-written examples could drift from the real errors, and only the unknown-operation detail was checked.
   Fixed: `showsTheRealResponseInEachExample` sends a request that triggers each example and compares the whole
   body. It caught real drift straight away: the 415 example said `'text/plain'`, but the API says
   `'text/plain;charset=UTF-8'`.

/code-review, Standards:

1. **Hard:** BR-1 limits and BR-3 rounding were copied into `@Schema` descriptions (STANDARDS §4: domain values are
   generated in `config.OpenApiConfig`). Fixed: `Calculation.MAX_PRECISION` / `MAX_SCALE` and
   `Operation.ROUNDING` are now public domain constants, `OpenApiConfig.domainValues` builds the descriptions from
   them, and `documentsTheLimitsAndRoundingTheDomainEnforces` checks it.
2. `ApiDocumentationTest` is a second `@SpringBootTest`, but STANDARDS §6 says one. Fixed: springdoc is not part of the
   web slice, so §6 now lists an "API docs" row (change log updated).
3. springdoc needs a PR justification (STANDARDS §1). Done in the PR body.
4. Problem type URIs, titles and the path are repeated as literals in the examples (Duplicated Code). Not changed:
   the drift test now fails if any example differs from the real response, which is the risk the duplication posed.
5. Message chains through the springdoc model. Not changed: that is the springdoc model's shape. A rename fails
   `ApiDocumentationTest` at once.
6. One customizer did three jobs (Divergent Change). Fixed: split into `apiInfo`, `problemDetailsAsSent`,
   `domainValues`, plus `requestLimits` (Spec 2).
7. `List.copyOf` on an already-unmodifiable list. Fixed: removed.
8. The enum test repeats `Operation.values()`, as the config does. Not changed: it checks that the served spec
   carries the domain's list (the customizer ran, nothing overwrote it). The real-response test guards the detail.

/code-review, Spec:

1. A 406 (non-JSON `Accept`) is a real Problem Details response but was undocumented. Fixed: documented with an
   example, and the example is checked against the real 406.
2. The request limits weren't in the docs. Fixed: `OpenApiConfig.requestLimits` adds them to the 400 description,
   read from the same `spring.jackson.factory.constraints.read.*` properties the parser uses; `documentsTheRequestLimits`.
3. The test called `/swagger-ui/index.html`, not the PRD's `/swagger-ui.html`. Fixed: it checks both (redirect, then
   page).
4. `CalculationResponse` had no `required` list. Fixed: all four fields are required.
5. `Operation.supportedNames()` was made public. Kept: `OpenApiConfig` needs it to build the unknown-operation example.
6. A README line names the docs URLs. Kept: it's a pointer, not hand-written docs.
7. The 415 detail mismatch. Fixed (see ponytail 1).

## Surprises

- A Python string replace silently failed to match because Spotless had already reformatted the method. The 406
  test then ran without its `Accept` header and failed, and Spotless had also removed the now-unused import. Cause:
  editing after `spotlessApply` with patterns written for the pre-format code.
- MockMvc appends `;charset=UTF-8` to text content types, so the 415 detail differs from a curl with `text/plain`.
  The example now shows the MockMvc form, which is also what most HTTP clients send.
- springdoc's own `ProblemDetail` schema doesn't match what Spring sends. Only reading the served spec showed it; no
  test would have asked.

## Shortcuts taken

None.

## Lessons

- Check docs against the real responses, not against themselves. The one test that compares every example with a
  live response found a real difference on its first run and makes every literal safe.
- Read the generated artifact (spec JSON) once by eye before review. It found a problem neither the tests nor the
  reviewers were looking for.
- After `spotlessApply`, edit with the Edit tool against the current file, not with replace patterns written
  beforehand.
