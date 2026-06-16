# Helpdesk Backend — An Evolutionary Study

A learning project that builds a support-ticket system in stages: first as
plain, deterministic software with no AI, then progressively augmented with an
LLM and finally an autonomous agent. The goal is not the app — it is to see,
concretely, **where a software developer's role sits as AI takes over more of
the work.**

This repository holds the **backend** (Spring Boot). The Angular frontend lives
in its own repository, `helpdesk-frontend`, with its own documentation.

## How to read this

This file is the narrative log. It grows one stage at a time, in step with the
commits. Each stage records what was built, the idea it demonstrates, and a
link to the relevant decision record when a non-obvious choice was made.

Two companion formats live alongside it:

- `docs/adr/` — Architecture Decision Records: one file per significant
  decision, with its rationale and the alternatives rejected. The code is kept
  clean (no explanatory comments); the "why" lives in the ADRs.
- the git history itself — each stage is a small, self-contained commit.

## The guiding idea

Most people use AI; few are fluent with it. Fluency is less about knowing which
button to press and more about judgment. A useful frame is the **4D framework**
— Delegation, Description, Discernment, Diligence — by Rick Dakan and Joseph
Feller, developed in collaboration with Anthropic.

Translated to engineering, the four map onto things a developer already does:

- **Delegation** -> deciding what to hand off versus keep: a decomposition and
  architecture call.
- **Description** -> writing a precise spec or contract.
- **Discernment** -> code review: judging whether the output is actually
  correct, not merely plausible.
- **Diligence** -> ownership: tests, traceability, accountability for what ships.

The bet this project tests: as models get more capable and more autonomous, the
typing shrinks and these four — the judgment — become the bulk of the value.
The developer's center of gravity slides from *producing* artifacts to
*defining the problem, operating the rails, and answering for the result.*

## The before / rails / after shape

When an autonomous agent runs a loop on its own (plan -> execute ->
self-validate, repeated), the human's work wraps around that loop:

- **Before** — define the problem and the acceptance criteria the agent
  optimizes against.
- **During (the rails)** — build and operate what bounds the agent:
  deterministic rules, a sandbox, observability, approval gates on irreversible
  actions.
- **After** — review, sign off, and own the outcome. Accountability is not a
  skill the model acquires; it is the relationship between whoever holds the
  risk and whoever drives the operation.

A theme runs through the whole build: **the deterministic core written in the
no-AI version becomes the rails for the AI version.** Nothing is thrown away.

## Roadmap

| Stage | What                                                  | What it demonstrates                          |
|-------|-------------------------------------------------------|-----------------------------------------------|
| v1    | Deterministic backend, no AI (this repo, done)        | The domain rules that later become the rails  |
| L1    | LLM as a tool: auto-classify, suggest priority        | Description + Discernment; human still decides |
| L2    | LLM drafts replies; human reviews and sends           | The approval gate                             |
| L3    | Autonomous agent resolves simple tickets in the rails | The full loop, all four Ds, accountability    |

(The Angular frontend is a parallel track in its own repository.)

## Stage log

### Stage 0 — Project skeleton

Generated a Spring Boot project (web, JPA, validation, H2) and committed the
empty, running skeleton. Marker zero.

### Stage 1 — The domain (complete)

The deterministic heart, written by hand, smallest dependencies first. No AI —
but precisely what makes the later AI safe, because the rules live in the domain
rather than in a prompt, where nothing downstream can negotiate around them.

- `TicketStatus` — the status state machine. See **ADR 0001**.
- `Priority` — SLA per priority. See **ADR 0002**.
- `Category` — a bare enum; the seam where an LLM will later suggest a
  classification automatically.
- `Ticket` and `TicketResponse` — rich entities that protect their own
  invariants (no status setter; due date and timestamps kept consistent;
  encapsulated responses). See **ADR 0003**.

Organization: domain enums were grouped under `domain.enumerator`. Exceptions
were first placed under `domain.exception`, then centralized in a top-level
`exception` package. See **ADR 0006**.

### Stage 2 — Persistence, service, and REST API (complete)

- `TicketRepository` — Spring Data JPA, with a fetch-join query for the detail
  view.
- `TicketService` — orchestrates domain rules and returns entities, never DTOs,
  so non-web callers (a future agent) get rich objects carrying the rail
  methods. See **ADR 0004**.
- `TicketController` plus request/response DTOs and a `GlobalExceptionHandler`.
  Two response projections (summary for lists, full detail with a fetch-join);
  `allowedNext` is exposed so a client renders only legal transitions; domain
  exceptions map to RFC 7807 responses. See **ADR 0005**. The web package was
  later renamed from `web` to `controller`.
- `open-in-view` is disabled on purpose, which is why the detail path fetch-joins
  the responses.
- Added Swagger UI via springdoc-openapi (3.x line, for Spring Boot 4) for
  visual API testing.

Validation: the API was exercised end-to-end through Swagger UI. The state
machine was confirmed enforcing the rules at the HTTP boundary — an illegal
transition (for example `IN_PROGRESS -> CLOSED`) returns **409 Conflict**. That
409 is the rail: the rule written in the domain on day one blocks the operation
at the edge, and a future agent calling the same service inherits the same wall
by construction.

## Next — AI integration

With the deterministic core and its rails in place, the next phase adds AI in
layers (L1 -> L3 above). Each layer plugs into a seam already present in this
codebase (the `Category` enum, the service methods, the state machine) and
keeps the AI bound by the same rules the rest of the system obeys.


---->

### Stage 3 — AI integration, Level 1: category suggestion (complete)

The first AI seam. When a ticket is being created, an LLM reads its title and
description and *suggests* a `Category`. The decisions behind it are recorded in
**ADR 0007** (how the AI integrates) and **ADR 0008** (cost strategy).

- `ClassificationService` (package `ai`) calls the LLM through Spring AI's
  `ChatClient`. The class contains no Anthropic-specific code — the provider is
  configuration, not code — and it returns `Optional<Category>`.
- The rail: the model's free-form reply is validated against the `Category`
  enum. Only a value that maps to a real category passes; anything else (a
  hallucinated category, an odd format, an empty reply) becomes
  `Optional.empty()`. The LLM proposes; the domain decides. It is the same kind
  of wall that returns 409 for an illegal transition, now applied to AI output.
- `ClassificationController`, with `SuggestionRequest` and `CategorySuggestion`
  DTOs, exposes a dedicated endpoint (`POST /api/classification/category`),
  separate from ticket creation. The `available` flag carries the "no
  suggestion" case as a normal state, not an error.
- Opt-in and non-blocking (ADR 0008): creating a ticket never calls the LLM;
  suggestion is a separate, optional request. An AI outage — or an empty credit
  balance — cannot break the core flow.
- Provider abstraction, proven in practice: switching the model would touch only
  `pom.xml` and `application.properties`, never the service code.

Validation: tested through Swagger. A billing complaint was classified as
`BILLING`, a login error as `TECHNICAL`, a vague question as `GENERAL` — each
reply passing through the enum rail.

## Next — Level 1 frontend, then Levels 2 and 3

- Frontend: a "suggest category" action on the create-ticket screen, with the
  human gate (the AI proposes, the user confirms or overrides), a loading state
  for the 1-2 second call, and graceful handling when `available` is false.
- Level 2: the LLM drafts a reply, reviewed and sent by a human (the gate again).
- Level 3: an autonomous agent resolves simple tickets within the same rails.