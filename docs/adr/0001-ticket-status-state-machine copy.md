# 0001 — Encode ticket status transitions as a state machine

- Status: Accepted
- Date: 2026-06-13

## Context

A support ticket moves through a small set of statuses (`OPEN`, `IN_PROGRESS`,
`RESOLVED`, `CLOSED`). Not every move is legal: a ticket should not jump from
`OPEN` straight to `RESOLVED`, and a `CLOSED` ticket should not change at all.

The rule that defines legal moves needs a single, authoritative home. This
matters beyond the immediate app: a later stage of this project introduces an
autonomous agent that will move tickets on its own. Whatever constrains a human
here must constrain the agent there — by construction, not by trusting the
agent to behave.

## Decision

Model status as an enum that owns its own transition rules:

```java
public enum TicketStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED;

    public Set<TicketStatus> allowedNext() {
        return switch (this) {
            case OPEN        -> Set.of(IN_PROGRESS, CLOSED);
            case IN_PROGRESS -> Set.of(RESOLVED, OPEN);
            case RESOLVED    -> Set.of(CLOSED, IN_PROGRESS);
            case CLOSED      -> Set.of();
        };
    }

    public boolean canTransitionTo(TicketStatus target) {
        return allowedNext().contains(target);
    }
}
```

The `Ticket` entity exposes no status setter. The only way to change status is
`transitionTo(target)`, which consults `canTransitionTo` and throws
`InvalidTransitionException` on an illegal move. Every caller, including the
service layer, goes through this path.

## Consequences

Positive:

- One source of truth for the workflow; no scattered conditionals that can
  drift out of sync.
- The `switch` is exhaustive with no `default`: adding a new status makes the
  code fail to compile until its transitions are defined. The compiler forces
  the decision instead of letting a silent fall-through become a bug.
- A terminal state is just data (`CLOSED` returns an empty set), not a special
  case handled with an `if`.
- This rule becomes a rail for later automation: an agent calling the service is
  bound by the same check automatically.

Tradeoffs:

- The transition table lives in code, so changing the workflow requires a
  recompile; it is not runtime-configurable. Acceptable at this scope. Revisit
  only if the workflow ever needs to change without a deploy.

## Alternatives considered

- **A plain `String`/field with validation scattered across services.**
  Rejected: the rule gets duplicated and can drift, and nothing stops a caller
  from setting the status directly.
- **A status field validated only in the service layer.** Rejected: the rule
  would live outside the type, so any new code path could bypass it. Keeping it
  in the domain makes it unavoidable.
- **A workflow engine (for example, Spring StateMachine).** Rejected: overkill
  for four states; it adds a dependency and a layer of indirection that obscure
  the very thing this project is meant to teach.