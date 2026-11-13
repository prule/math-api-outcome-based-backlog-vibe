# Retro 4: Hostile operands cannot take the service down

## Caught by review

ponytail-review (verdict: ship):

1. The number-length detail quoted the string-length setting. Fixed: `max-number-length` is read on its own, and
   numbers and strings get their own detail.

/code-review, Standards (no hard violations):

1. Limits held in `application.yaml` and read back with `@Value` might count as "configurable limits", which item 4
   excludes. Not changed: the exclusion is about building a way to configure them, and none was built. STANDARDS §8
   asks for the library's own switches over custom code, and `spring.jackson.factory.constraints.read.*` is Jackson's.
   The `@Value` reads keep each number in one place, so the detail can never quote a limit other than the real one.
   `HostileRequestTest` hard-codes 16,384 on purpose: the tests check the PRD contract, not the config.
2. Number detail used the string limit. Fixed (see ponytail 1).
3. The fallback "The request breaks a size limit." names no limit. Kept: Jackson has no other read limit this body
   can reach first (name length 50,000 and token count are both beyond a 16,384-byte body), so it only fires if a
   Jackson upgrade changes its wording, and `HostileRequestTest` fails in that case.
4. PRD §3.2 changed. Within the item: it asks for deep nesting to be rejected and for the client to learn which
   limit it broke. The body limit is now stated in bytes, because that is what the parser counts. The change is logged.
5. The handler only passes the three limits on (Middle Man / Data Clumps). Not changed: `RequestLimits` can't bind
   them itself without being a bean, and `@WebMvcTest` doesn't load plain `@Component`s (that broke every web test
   once; see Surprises).
6. Limits told apart by Jackson's message text (fragile). Not changed: Jackson throws one exception type for every
   limit, so the text is the only signal. One test per limit guards it.
7. `RequestLimitsTest` read like a unit test of `RequestLimits`. Fixed: renamed `HostileRequestTest`, after the
   outcome it proves.
8. `postJson` helpers repeated across test classes. Not changed: three 3-line helpers in separate test classes.
9. Constructor placed between fields and constants. Left to Spotless's layout; no rule says otherwise.

/code-review, Spec (meets the outcome, with gaps):

1. The BR-1 detail didn't say which of the two limits was broken. Fixed: "has more than 100 significant digits" or
   "has a scale outside ±1000"; test `saysWhichOperandLimitWasBroken_BR1`.
2. An exponent beyond Java's `int` range (`1e9999999999`) gets the generic invalid-request detail, not
   operand-out-of-range. Not fixed: `BigDecimal` cannot represent it, so it fails while parsing, before BR-1 can
   run. It is still a cheap 400. Reporting it as out of range means parsing operands ourselves instead of letting
   Jackson bind `BigDecimal`, which is too much code for a better message.
3. `1E-1000` accepted only in the domain test. Fixed: `acceptsOperandsAtTheLimits_BR1` also sends it over HTTP.
4. `@Value` wiring close to "configurable limits". See Standards 1.
5. Message-text matching is fragile. See Standards 6.

## Surprises

- `RequestLimits` as a `@Component` broke every web-slice test with `NoSuchBeanDefinitionException`. Cause:
  `@WebMvcTest` loads controllers and advice, not plain components. Fixed by building it in the handler.
- A 600-deep nested array in `a` was rejected as "not a number", not "too deep". Cause: binding a `BigDecimal` fails
  at the first `[`, before the parser goes deeper. Deep nesting only costs anything in a field the binder skips
  over, so the test now nests in an ignored field. That is the real attack shape.
- Spring Boot 4 already exposes Jackson's read limits as properties, so the request limits need no code: one yaml
  block. The work was in telling the client which one broke.
- A 20 MB body is reported as "JSON string longer than 1000 characters", not "body too large": the parser hits the
  string limit first. That is still true and still a limit the client broke.

## Shortcuts taken

None.

## Lessons

- Get a behavioural red, not only a compile error: a stub exception made 17 tests fail for the right reasons.
- Probe the jar with a small heap (`-Xmx128m`) and hostile payloads. Tests prove the messages; only the live server
  proves the memory holds.
- With a single "done when" like "told which limit it broke", the reviews push the detail wording further each
  round. Decide the wording from the PRD once, up front.
