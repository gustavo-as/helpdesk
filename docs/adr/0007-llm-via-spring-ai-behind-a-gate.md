# 0007 — Integrate the LLM through Spring AI, as a suggestion behind a human gate

- Status: Accepted
- Date: 2026-06-14

## Context

The AI phase begins here. The first seam (Level 1) is automatic classification
of a ticket's `Category` from its title and description — the `Category` enum
was kept deliberately simple for exactly this. Wiring an LLM into a
deterministic system raises three questions:

1. What role does the AI play — does it decide, or suggest?
2. How is the LLM called — bound to one provider, or behind an abstraction?
3. How does free-form model output stay inside the system's rules?

## Decision

- **Role: a suggestion behind a human gate.** The LLM reads title and
  description and *proposes* a category; a human confirms or overrides it before
  it is saved. The AI does the tedious reading; the human keeps the decision.
  This is the "Delegation" of the 4D framing — delegate the work, keep the
  judgment.
- **Abstraction: Spring AI.** Code is written against Spring AI's `ChatClient`,
  so the provider becomes configuration rather than code. This mirrors how
  `TicketService` hides infrastructure from the domain.
- **Provider: Claude (Anthropic) initially, and swappable.** Chosen for
  first-class Spring AI support; replaceable by changing a dependency and
  configuration, with no change to the classification logic.
- **Rail: validate the model output against the `Category` enum.** The LLM may
  return anything; only a value that maps to a valid `Category` is accepted. An
  unrecognized or hallucinated category is rejected and falls back to a manual
  choice. The domain validates; the model merely proposes.

## Consequences

Positive:

- The gate keeps a human in the loop at Level 1 — the point of this level — and
  the same pattern is reused at Level 2 (drafting replies). The gate is removed
  deliberately only at Level 3 (the autonomous agent).
- Provider lock-in is avoided; switching models is a configuration change.
- Non-deterministic output is bounded by the same kind of rail the rest of the
  system uses: a suggestion can never introduce an invalid category.
- Spring AI's structured-output support makes constraining the model to the enum
  straightforward.

Tradeoffs:

- An extra dependency and an external call in the flow (cost is addressed in
  ADR 0008).
- The LLM call can fail or be slow, so classification must be optional and
  degrade to manual — it must never block ticket creation.

## Alternatives considered

- **AI classifies automatically, with no gate.** Rejected at Level 1: it hides
  misclassification and skips the very concept this level teaches. The gate is
  removed on purpose later (Level 3), not by accident now.
- **Call the provider API directly (a hand-rolled HTTP client).** Rejected: it
  binds the code to one provider; switching later means rework.
- **Run a local model.** Rejected for the POC: it adds serving and
  infrastructure complexity that turns the project into an MLOps exercise rather
  than an AI-integration one.
- **Trust the model's raw output.** Rejected: free-form text is the opposite of
  the deterministic guarantees the system is built on. The enum validation is
  the rail.