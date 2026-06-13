# 0006 — Centralize exceptions in a top-level package

- Status: Accepted
- Date: 2026-06-13

## Context

The project's exceptions (`InvalidTransitionException`,
`TicketNotFoundException`) were first placed inside the domain, under
`dev.poc.helpdesk.domain.exception`, on the reasoning that a domain exception is
part of the domain (see the discussion behind the rich entity in ADR 0003).

In practice, exceptions are a cross-cutting concern: they are raised in the
domain, pass through the service, and are translated at the web boundary by the
`GlobalExceptionHandler`. Splitting them by "is this a business rule or not?"
adds a judgment call on every new exception without a clear payoff at this
scale.

## Decision

Move all exceptions to a single top-level package, `dev.poc.helpdesk.exception`,
and keep new exceptions there.

This supersedes the earlier placement under `domain.exception`.

## Consequences

Positive:

- One predictable home for every exception. No per-exception decision about
  where it belongs.
- Matches a common, low-ceremony Spring project layout, which keeps the POC easy
  to navigate.

Tradeoffs:

- The package sits at the root, beside `domain`, `service`, and `controller`,
  rather than expressing that some exceptions are domain rules. This is a known
  simplification.
- If the project ever needs to distinguish business exceptions from technical or
  infrastructure ones, this single package will be revisited — at which point a
  new ADR would supersede this one.

## Alternatives considered

- **Keep exceptions inside the domain (`domain.exception`).** The original
  choice. Rejected for now: it forces a domain-versus-not classification on each
  new exception, which is overhead the POC does not benefit from yet.
- **Split immediately into business and technical exception packages.**
  Rejected: premature for the current scope; it adds structure before there is a
  real mix of exception kinds to justify it.