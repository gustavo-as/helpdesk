# Architecture Decision Records

This folder records significant decisions made while building the project — one
file per decision.

Each record is numbered and treated as immutable: when a decision changes, we
add a new record that supersedes the old one rather than editing history. That
keeps the evolution of the reasoning visible, which is the point of an
evolutionary study.

Each record follows the same shape: Context, Decision, Consequences,
Alternatives considered.

| #    | Decision                                          | Status   |
|------|---------------------------------------------------|----------|
| 0001 | Encode ticket status transitions as a state machine | Accepted |