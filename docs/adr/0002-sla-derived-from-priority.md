# 0002 — Derive the SLA from priority, in the domain

- Status: Accepted
- Date: 2026-06-13

## Context

Each ticket has a target resolution time (an SLA) that depends on how urgent it
is. We need a single, unambiguous place to define "URGENT means 2 hours, LOW
means 72 hours" that is hard to get wrong.

## Decision

Attach the SLA to the `Priority` enum itself, as a `java.time.Duration` passed
in the constructor:

```java
public enum Priority {
    LOW(Duration.ofHours(72)),
    MEDIUM(Duration.ofHours(24)),
    HIGH(Duration.ofHours(8)),
    URGENT(Duration.ofHours(2));
    // ...
}
```

The concrete due date is computed where the clock lives — on the `Ticket`, as
`createdAt.plus(priority.sla())` — not inside `Priority`. Priority knows its
window; the ticket knows when it started.

## Consequences

Positive:

- Priority and its SLA are defined together, in one place. Adding a new priority
  forces you to supply its SLA, because it is a constructor argument, so the set
  can never be half-defined.
- The type is `Duration`, not a bare number, so there is no "hours or minutes?"
  ambiguity and no magic number.
- Deterministic and pure: same priority, same window, every time. Easy to test,
  and later easy for an agent to rely on as a fixed ruler.

Tradeoffs:

- The SLA values live in code, so changing them needs a deploy. Same tradeoff as
  ADR 0001, and acceptable for the same reason. If SLAs ever need to vary per
  customer or change at runtime, this moves to configuration or a policy table.

## Alternatives considered

- **SLA as configuration (application.yml or DB) from day one.** Rejected for
  now: it adds indirection before there is a real need. The move stays easy
  later because callers depend on `priority.sla()`, not on where the value comes
  from.
- **A separate SlaPolicy service.** Rejected at this scope: it separates the SLA
  from the thing it describes for no current benefit.