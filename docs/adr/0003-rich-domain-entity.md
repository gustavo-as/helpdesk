# 0003 — Keep ticket rules inside a rich domain entity

- Status: Accepted
- Date: 2026-06-13

## Context

The `Ticket` entity could be a plain data bag with public getters and setters,
leaving the rules to the service layer. But ADRs 0001 and 0002 already put the
status transitions and the SLA inside the domain. The entity has to preserve
that: nothing should be able to set a status that skips the state machine, and
the computed fields (due date, last-updated) must stay consistent.

## Decision

Model `Ticket` and `TicketResponse` as rich entities that protect their own
invariants.

- No setters for status, timestamps, or the SLA due date. State changes only
  through intent-revealing methods: `transitionTo`, `assignTo`, `addResponse`.
- `transitionTo` delegates the legality check to the state machine
  (`TicketStatus.canTransitionTo`) and throws `InvalidTransitionException` on an
  illegal move.
- The constructor establishes the invariants: a new ticket starts `OPEN`, gets
  `createdAt`, and computes `slaDueAt = createdAt + priority.sla()`. The due
  date is computed here, where the clock lives.
- Every mutating method calls a private `touch()` so `updatedAt` can never drift
  out of sync.
- The `responses` collection is encapsulated. Callers add through `addResponse`,
  which sets both sides of the relationship via the package-private
  `attachTo`, so the bidirectional link cannot desync.
- A `protected` no-arg constructor exists only because JPA requires it; it is
  not meant for application code.

## Consequences

Positive:

- Invariants cannot be violated from outside the entity. The state machine has
  no bypass, which is what makes it a dependable rail for later automation.
- Computed fields (`slaDueAt`, `updatedAt`) are always consistent, because the
  only paths that change state also maintain them.
- The entities are testable as plain Java objects, without a database.

Tradeoffs:

- More code than an anemic bean with generated setters. That verbosity is the
  cost of the guarantees.
- The `protected` constructor is a concession to JPA.
- Getters currently expose the internal `responses` list directly; if external
  mutation ever becomes a risk, return an unmodifiable view. Deferred for now.

## Alternatives considered

- **Anemic entity (public setters) with rules in services.** Rejected: a
  `setStatus` would let any caller skip the state machine, defeating ADR 0001.
- **Lombok `@Data`.** Rejected: it generates setters that break the invariants,
  which is exactly what this decision is preventing.