# Retro 2: Clients get exact results for add, subtract and multiply

## Caught by review

ponytail-review (verdict: fix 1 and 2 first):

1. Unexpected exceptions fell through to Spring Boot's `/error` JSON, not Problem Details (PRD §3.2: every error is
   Problem Details). Reproduced live: `MULTIPLY 1e-1500000000 × 1e-1500000000` overflows the scale and returned
   Boot's `{timestamp, status, error, path}`. Fixed: catch-all `@ExceptionHandler(Exception)` logs the cause and
   returns a 500 Problem Details with a generic detail; test `answersAnUnexpectedFailureWithProblemDetailsThatHideTheCause`.
2. `"a": ""` reported as "Missing required field(s)". Fixed: detail now says "Missing or empty field(s)".

/code-review, Standards (no hard violations):

1. `spring-boot-starter-validation` needs a PR justification (STANDARDS §1). Done in the PR body.
2. The not-readable detail hard-coded `'operation', 'a' and 'b'`, which goes stale when unary operations arrive.
   Fixed: the detail no longer names fields.
3. "Missing" for every field error. Fixed (see ponytail 2).
4. Operation stays a `String` until the use case (Primitive Obsession). Not fixed: keeps `UnknownOperationException`
   a domain exception, as DOMAIN-MODEL §2 prescribes (`Operation.named`).
5. `PerformCalculation` is a pass-through (Middle Man); `(a, b)` travel together (Data Clump). Not fixed:
   STANDARDS §2 and DOMAIN-MODEL §2 prescribe this shape.
6. `Operation.named` tests lived in `CalculationTest`. Fixed: moved to `OperationTest`.
7. 415 test checked only the media type. Fixed: now also asserts `status`, `title` and `detail`. It does not assert
   `type`: framework errors have no PRD type, so Spring omits it (RFC 9457: absent means `about:blank`).
8. `_BR4` suffix in camelCase test names. Not fixed: STANDARDS §3 asks for the BR id in the name or a comment;
   this is that.

/code-review, Spec (meets the spec, nothing missing):

1. No catch-all 500 Problem Details. Fixed (see ponytail 1).
2. Empty-string detail misleading. Fixed (see ponytail 2).
3. `toPlainString()` on `1e999999999` builds a ~1 GB string: a live memory DoS. Not fixed: item 4 ("Hostile
   operands cannot take the service down") owns operand and request limits, and item 2 excludes BR-1. Nothing is
   deployed yet.
4. Whitespace and `NaN`/`Infinity` strings unchecked. Checked live: `"NaN"`, `"Infinity"`, bare `NaN`, `"0x10"`
   and `"1_000"` are rejected as invalid-request; `" 1 "` and `"+5"` are accepted and echoed as `"1"` and `"5"`.
   Lenient but harmless, so left as is.

## Surprises

- Every test passed on the first run, which is suspicious; per-class test counts confirmed all 33 ran.
- `pkill -f` / `pgrep -f` with the server's port in the pattern matched the probing shell's own command line and
  killed it (exit 144) three times. Cause: the pattern text appears in the shell's own arguments. Stopping the
  server by its listening port (`ss -ltnp`) works.

## Shortcuts taken

None. The empty-layer shortcut from item 1 was removed: every layer now has a class, and `ArchitectureTest` no longer
allows empty rules or optional layers.

## Lessons

- STANDARDS §6 says test-first; I wrote the code first, then the tests. The tests are thorough, but none was seen
  to fail first. Next item: write the "done when" tests first and watch them fail.
- Probe the running jar with hostile input during the build, not only in review: it found the off-contract 500
  in seconds.
- `Operation` has no `arity()` yet, though DOMAIN-MODEL §2 lists it: nothing needs it until unary operations
  (item 7).
