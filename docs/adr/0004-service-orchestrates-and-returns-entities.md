# 0004 — The service orchestrates domain rules and returns entities

- Status: Accepted
- Date: 2026-06-13

## Context

We need a layer that coordinates use cases — load a ticket, invoke a domain
operation, persist the result — and defines the transaction boundaries. Two
questions come with it:

1. Where do the business rules live: in this service, or in the domain?
2. What does the service hand back: domain entities, or presentation DTOs?

The second question matters more than it looks, because a later stage of this
project introduces an autonomous agent that calls this same service. Whatever
the service returns is what the agent has to work with.

## Decision

- All business rules stay in the domain (entities and enums). The service only
  orchestrates: load, invoke the entity's behavior, let transactional dirty
  checking persist the change. No business logic in the service beyond
  coordination, and none in the controller.
- The service returns domain entities, not DTOs. Mapping to a presentation DTO
  is the web layer's responsibility.
- Read methods are marked `@Transactional(readOnly = true)`; mutating methods
  are `@Transactional`.

## Consequences

Positive:

- One home for the rules (the domain), one home for coordination (the service),
  one home for presentation (the web layer). Each layer has a single
  responsibility.
- The service is reusable by any caller, not just the web controller. A future
  autonomous agent calls the same methods and gets back rich objects that carry
  the rail methods (`transitionTo`, `isBreachingSla`). The state machine and the
  SLA are inherited by construction, not by trusting the caller.
- Transaction boundaries are explicit and visible on the service.

Tradeoffs:

- Returning entities means lazy associations (a ticket's `responses`) must be
  initialized before serialization, because `open-in-view` is disabled. This is
  handled at the web layer, not here: the detail read will use a fetch-join to
  load responses, and the list view will be a summary without them. We keep
  `open-in-view` off on purpose.

## Alternatives considered

- **Service returns DTOs.** Rejected: it flattens the domain object, so non-web
  callers (the agent) would have to reload entities to act, and it pushes a
  presentation concern into the service, binding it to the web layer.
- **Business logic in the service (transaction script).** Rejected: it would
  duplicate or crack the invariants the rich entity already protects (ADR 0003).
- **Enable `open-in-view` to avoid lazy issues.** Rejected: it holds the DB
  connection open through view rendering and hides N+1 problems. We prefer to
  fetch explicitly.