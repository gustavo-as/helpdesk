# 0008 — Treat LLM classification as an opt-in, cost-aware step

- Status: Accepted
- Date: 2026-06-14

## Context

Each LLM classification is a paid API call — on the order of cents per call,
which adds up at volume. This cost characteristic was raised during review and
is worth deciding before the feature is built, not after the bill arrives.

Classification is a convenience, not a core function: a ticket is perfectly
valid with a manually chosen category.

## Decision

- **On-demand, not automatic on every create.** Classification is triggered
  explicitly (a "suggest category" action), not fired for every ticket. The user
  opts in per ticket.
- **Optional and non-blocking.** If the call is skipped, fails, or is slow,
  ticket creation proceeds with a manually chosen category. The LLM never sits on
  the critical path.
- **A small, cheap model is acceptable for this task.** Sorting text into four
  categories is simple and does not need the most expensive model. Provider and
  model are configuration (see ADR 0007), so this is tunable without code
  changes.
- **Cache identical inputs** (optional, later): the same title and description
  need not be re-classified.

## Consequences

Positive:

- Cost scales with use, not with ticket volume: nothing is charged for tickets
  where the user never asks for a suggestion.
- The feature degrades gracefully; an AI outage or a budget cap does not break
  the core flow.
- Model size is a lever, not a fixed cost.

Tradeoffs:

- Opt-in means fewer tickets get auto-categorized than a fully automatic approach
  would. Acceptable: the manual path always exists, and Level 1 is about keeping
  a human in the loop anyway.

## Alternatives considered

- **Classify every ticket automatically on creation.** Rejected: cost scales
  directly with ticket volume, and it contradicts the gate in ADR 0007.
- **Always use the largest model.** Rejected: unnecessary for a four-way
  classification; it spends money on an easy task.
- **Ignore cost until it becomes a problem.** Rejected: the cost is known up
  front, so designing for it now is cheaper than retrofitting later.