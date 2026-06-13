# 0005 — Separate DTOs at the web boundary, with summary and detail views

- Status: Accepted
- Date: 2026-06-13

## Context

The REST API must not expose JPA entities directly: serializing them couples the
API to the database schema and leaks lazy proxies. We also need validated input,
and we still owe the resolution of the lazy-loading cost named in ADR 0004
(`responses` is lazy and `open-in-view` is disabled).

## Decision

- Input uses request DTOs (records) with Bean Validation. The controller never
  binds a request body to an entity.
- Output uses two response projections:
  - `TicketSummaryView` for list endpoints — no `responses`.
  - `TicketView` for a single ticket — full, including `responses` and the
    `allowedNext` legal transitions.
- The detail path loads the ticket through a fetch-join
  (`repository.findByIdWithResponses`), so `responses` is initialized inside the
  transaction. The list path never touches `responses`. This is how ADR 0004 is
  honored with `open-in-view` turned off.
- `TicketView` exposes `allowedNext` (the legal transitions from the current
  status) so a client renders only valid actions instead of re-implementing the
  state machine.
- Domain exceptions are translated to RFC 7807 `ProblemDetail` responses: 404
  for a missing ticket, 409 for an invalid transition.

## Consequences

Positive:

- Entities are never serialized; the API shape is independent of the schema.
- Input is validated at the edge.
- The list stays cheap because it never loads responses.
- The client inherits the state machine for free through `allowedNext`; the UI
  cannot drift from the server's rules.
- Errors follow a standard, machine-readable format.

Tradeoffs:

- More types to maintain (request DTOs plus two views) and the mapping code in
  the views' `from` factories. Acceptable: it keeps the boundary explicit.

## Alternatives considered

- **Serialize entities directly.** Rejected: couples the API to the schema,
  leaks lazy proxies, and gives no input validation.
- **One view for both list and detail.** Rejected: it would force loading
  `responses` for every row of the list — exactly the lazy cost we are avoiding.
- **Let the client hardcode the allowed transitions.** Rejected: it duplicates
  the state machine outside the server, where the two copies will drift apart.