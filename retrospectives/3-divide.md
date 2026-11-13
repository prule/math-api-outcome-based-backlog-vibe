# Retro 3: Clients can divide

## Caught by review

ponytail-review: no findings (verdict: ship).

/code-review, Standards (no hard violations):

1. The web adapter changed (new exception handler and type URI), which the reviewer read as "drift" under
   STANDARDS §3 ("Nothing else should need to change"). Not fixed: DOMAIN-MODEL §2 says "A new kind of failure also
   needs a domain exception and its Problem Details handler; nothing else changes". DIVIDE adds the first 422, so
   this is the documented path. A shared 422 base type would save a few lines per future rule but was not asked for.
2. BACKLOG status and retro missing. Both done before the PR, as the process orders.
3. `DivideTest.divide` is nearly the same as `CalculationTest.result` (Duplicated Code). Not fixed: two 3-line test
   helpers; keeping BR-3 rounding cases in their own class reads better than more rows in `CalculationTest`.
4. The BR-2 inline comment overlaps the exception's Javadoc. Not fixed: the inline comment says why `signum()`
   covers every zero form.

/code-review, Spec (nothing missing, nothing wrong):

1. 422 tested at the HTTP layer for `"0.00"` only. Fixed: `rejectsDivisionByAnyZeroAsUnprocessable_BR2` covers `0`,
   `0.00` and `-0`, each as a JSON number and as a string.
2. `1 ÷ 4 = "0.25"` checked only in the domain. Fixed: `dividesExactlyWhenTheResultFits` at the HTTP layer.

## Surprises

None. Tests were written first this time and failed to compile (no `DIVIDE`, no `DivisionByZeroException`)
before the implementation made them pass.

## Shortcuts taken

None.

## Lessons

- Tests-first was cheap here and is now the default. A compile failure is a weak red, though; for behaviour
  changes to existing code, aim for a failing assertion.
- Reviewers can read STANDARDS without DOMAIN-MODEL. When the two docs speak to the same rule, the more specific
  one (DOMAIN-MODEL §2 here) decides.
- The DOMAIN-MODEL edge-case list was complete enough that tests followed straight from it, and the "done when"
  checks matched it, so there were no scope questions.
