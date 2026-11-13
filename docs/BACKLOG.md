# Math API — Backlog

> Living document, outcome based. Each item is one PR. Work top to bottom.
> An item says **who** gets **what outcome** and **how we will know**. It does not say how to build it:
> the builder chooses the design, guided by the [Standards](STANDARDS.md).
> When you start an item, set it to **In progress**; when its PR merges, set it to **Done** and note the PR.
> Source of scope: [PRD](PRD.md). Rules: [Domain Model](DOMAIN-MODEL.md).
>
> Items map one-for-one to the earlier task-based backlog (`math-api-vibe-coded`) so the builds can be compared.

## Status

| # | Outcome | Status | PR |
|---|---------|--------|----|
| 1 | Developers can build, check and ship the service | In progress | |
| 2 | Clients get exact results for add, subtract and multiply | Not started | |
| 3 | Clients can divide | Not started | |
| 4 | Hostile operands cannot take the service down | Not started | |
| 5 | Developers can integrate from the API docs alone | Not started | |
| 5a | API docs are not public in production | Not started | |
| 6 | Clients can raise to a power and take a modulo | Not started | |
| 7 | Clients can run single-operand operations | Not started | |
| 8 | Clients can discover the supported operations | Not started | |

Items 1–5 complete PRD v1; 5a is a v1 follow-up. Items under **Later** need a PRD update first (each moves
something out of the non-goals or roadmap).

## How to read an item

- **Outcome**: the change in the world, from the actor's point of view.
- **Why now**: why it is at this point in the order.
- **We know it's done when**: observable checks, through the API or the build. Each one needs a test.
- **Not in this item**: scope the builder must not pull in.

---

## v1

### 1. Developers can build, check and ship the service

- **Outcome**: A developer can clone the repo, run one command, and know the service builds, starts, is healthy and
  follows the architecture rules; every push gets the same verdict from CI.
- **Why now**: Every later item is then just behaviour.
- **We know it's done when**:
  - `./gradlew build` passes locally and in CI on every PR and push to `main`.
  - The running service reports itself healthy.
  - The build fails if the domain depends on a framework, or if code is not formatted (STANDARDS.md).
- **Not in this item**: any calculation, API docs.

### 2. Clients get exact results for add, subtract and multiply

- **Outcome**: A client service sends two operands and one of ADD, SUBTRACT or MULTIPLY, and gets back the exact
  decimal result, never a binary floating-point approximation. A bad request gets a clear, machine-readable rejection.
- **Why now**: The core promise of the product (PRD §1), for the operations that need no rounding.
- **We know it's done when**:
  - `0.1 + 0.2` returns `"0.3"`.
  - Every edge case in DOMAIN-MODEL.md §3 for these operations gives the result BR-4 says.
  - Operands are accepted as JSON numbers or strings; results come back as strings (PRD §3).
  - A malformed body and an unknown operation each return the PRD's Problem Details.
- **Not in this item**: DIVIDE, operand limits (BR-1), API docs.

### 3. Clients can divide

- **Outcome**: A client can divide two operands and gets a result rounded the one documented way, or a clear
  rejection when dividing by zero.
- **Why now**: Completes the four basic operations of v1.
- **We know it's done when**:
  - `1 ÷ 3` returns 34 significant digits, rounded per BR-3; `1 ÷ 4` returns `"0.25"`.
  - Dividing by any representation of zero (`0`, `0.00`, `-0`) returns 422 per BR-2.
  - Adding DIVIDE changed nothing in how the other operations behave.
- **Not in this item**: client-chosen precision.

### 4. Hostile operands cannot take the service down

- **Outcome**: No single request, however large its numbers or body, can exhaust the service's CPU or memory.
  The client is told clearly which limit it broke.
- **Why now**: Closes the resource-exhaustion hole before anyone else calls the API.
- **We know it's done when**:
  - Operands exactly at each BR-1 limit are accepted; one past each limit is rejected with 400 Problem Details.
  - `1e999999999 × 1e999999999` is rejected immediately, before any arithmetic.
  - Huge numbers, huge strings and oversized or deeply nested bodies are rejected without the parsing itself
    becoming the attack.
- **Not in this item**: configurable limits.

### 5. Developers can integrate from the API docs alone

- **Outcome**: A developer who has never seen the code can learn every operation, request shape, result and
  rejection from interactive API docs served by the service (PRD §3.3).
- **Why now**: Completes v1 scope.
- **We know it's done when**:
  - The service serves an OpenAPI spec and an interactive UI.
  - The spec lists every operation, both operand forms, the all-string result, and every Problem Details response
    with an example.
  - A test fails if the documented operations drift from the real ones.
- **Not in this item**: hand-written docs outside the generated spec.

### 5a. API docs are not public in production

- **Outcome**: An operator can run the service in production without exposing the API docs or UI, with no code
  change.
- **Why now**: v1 follow-up: docs are for developers, not the public.
- **We know it's done when**:
  - Docs are served by default and return 404 under `SPRING_PROFILES_ACTIVE=prod`.
- **Not in this item**: a custom switch that duplicates a library's own.

---

## Later (post-v1 — update PRD.md first)

Each item starts by moving its scope from PRD §5/§6 into scope and adding any new business rules (new BR ids) to
DOMAIN-MODEL.md. Order is a suggestion; reorder as needs arrive.

### 6. Clients can raise to a power and take a modulo

- **Outcome**: A client can use POWER and MODULO with results and rejections as predictable as the basic four.
- **We know it's done when**:
  - The rules for exponent range and form, modulo by zero and the sign of a modulo result are written as BRs and
    each has a test.
  - No POWER request can exhaust CPU.
  - Existing clients see no change; examples that used POWER as an "unknown operation" now use a name that will
    never be one.

### 7. Clients can run single-operand operations

- **Outcome**: A client can take the square root of, or negate, a single operand.
- **We know it's done when**:
  - SQRT and NEGATE work per new BRs (incl. SQRT of a negative and SQRT precision).
  - Every existing request to `/api/v1/calculations` behaves exactly as before.
  - The PR description states how a request with the wrong number of operands is handled, and why.

### 8. Clients can discover the supported operations

- **Outcome**: A client can learn at runtime which operations exist and how many operands each takes, so it can
  adapt to new operations without a release of its own.
- **We know it's done when**:
  - The list always matches what the service supports; a test fails if it drifts.

### 9. Clients can choose the precision of a result

- **Outcome**: A client can ask for a precision and rounding mode other than the BR-3 default; clients that don't
  ask see no change.
- **We know it's done when**: the default is unchanged, and no requested precision can exhaust CPU.

### 10. Clients can review past calculations

- **Outcome**: A client can retrieve calculations it performed earlier.
- **We know it's done when**: a successful calculation can be fetched later, singly and as a paged list. This ends
  statelessness, so PRD §4/§5, DOMAIN-MODEL.md and STANDARDS.md change first.

### 11. Only known clients can call the API, at a fair rate

- **Outcome**: The API knows who is calling and keeps each caller within a fair request rate.
- **We know it's done when**: unidentified calls are rejected, over-limit calls get 429 Problem Details, and the
  domain did not change.
