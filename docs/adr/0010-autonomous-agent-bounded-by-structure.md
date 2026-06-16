# 0010 — Level 3: an autonomous agent bounded by structure, not trust

- Status: Accepted
- Date: 2026-06-16

## Context

Level 3 is the project's goal: an agent resolves simple tickets on its own —
including the customer reply — with no human reviewing each action. This removes
the per-action human gate that was the rail at Level 2 (ADR 0009). The question
Level 3 must answer: if no human reviews each action, what bounds the agent,
especially when it sends free-text replies to customers, which ADR 0009 said
require human review?

The answer cannot be "we trust the model." Autonomy must be bounded by structure.

## Decision

The agent resolves simple tickets end to end (including the customer reply) and
escalates everything else to a human. Its autonomy is bounded by five composing
rails, none of which depends on trusting the model:

1. **Scope self-limitation.** The agent's first step is a decision: resolve or
   escalate. It acts autonomously only on tickets it judges simple and safe;
   anything uncertain is escalated to a human and left untouched. "I should not
   handle this" is a first-class, expected outcome, not a failure.

2. **A validated decision (a structural rail on control flow).** The agent's
   decision and chosen action are constrained to a fixed allowlist (`RESOLVE`,
   `ESCALATE`; reply, transition, assign). The decision is validated against
   that set exactly as Level 1 validated a category against the enum. The agent
   cannot invent an action outside the allowlist.

3. **The deterministic rails still apply.** Every action runs through the same
   `TicketService` the human uses. The state machine still rejects illegal
   transitions (the 409 wall) and the domain still enforces its invariants. The
   agent inherits every existing rule by construction; it has no back door.

4. **Full attribution and audit.** Every action the agent takes is recorded as
   authored by the agent. The gate moves from before each action (Level 2) to a
   complete, reviewable trail after the fact. A human can see exactly what the
   agent did and intervene.

5. **Opt-in and a kill switch.** The agent runs only when explicitly invoked
   (per ADR 0008's cost posture), and the capability can be turned off entirely.
   A human decides to let the agent act, and can stop it.

On the customer reply specifically: ADR 0009 required a human gate for free-text
replies because they cannot be validated structurally. Level 3 relaxes that
pre-action gate **only** for the bounded class of tickets the agent judges
simple, and replaces it with compensating controls (scope limit, attribution,
audit, kill switch). The reply text remains unvalidated; the safety comes from
limiting *when* the agent may send one and from making every send reviewable.

## Consequences

Positive:

- The agent composes every rail type built across the project: a validated
  decision (Level 1's structural rail), the deterministic service (the
  foundation), and the bounded-scope and audit controls (new at Level 3).
  Autonomy rests on structure, not trust.
- Escalation is safe by default: when unsure, the agent does nothing and hands
  off to a human.
- The audit trail keeps accountability legible even without a per-action gate.
- The kill switch bounds the blast radius of any failure.

Tradeoffs and residual risk:

- A free-text reply can reach a customer without prior human review. This is a
  real risk, accepted for the bounded simple-ticket scope and mitigated — not
  eliminated — by the controls above. The mitigation is *when* and
  *reviewability*, not validation of the text itself.
- The agent's scope judgment can be wrong: it may resolve something it should
  have escalated. The audit trail and kill switch exist precisely for this; in a
  real deployment, resolved tickets would be sampled for human review.
- Quality depends on the model and prompt; a weak reply harms the customer
  experience even when it breaks nothing structurally.

## Alternatives considered

- **Keep the human gate on replies (triage-only autonomy).** A safer design
  where the agent performs only operational actions and replies stay gated.
  Rejected for this project's goal, which is to demonstrate genuine end-to-end
  autonomy within rails — but it remains the right choice for higher-stakes
  domains, and the architecture supports switching to it by routing the reply
  back through the Level 2 gate.
- **Full autonomy with no scope limit (the agent attempts every ticket).**
  Rejected: removing the escalation path removes the most important rail.
  Self-limitation is what makes the rest defensible.
- **Trust the model and skip the audit trail.** Rejected: attribution is what
  preserves accountability once the per-action gate is gone. Without it,
  "autonomy" becomes "unaccountable".