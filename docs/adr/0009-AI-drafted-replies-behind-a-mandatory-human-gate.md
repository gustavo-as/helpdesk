# 0009 — Level 2: AI-drafted replies behind a mandatory human gate

- Status: Accepted
- Date: 2026-06-14

## Context

Level 2 lets an LLM draft a reply to a ticket, which a human reviews and sends.
This differs from Level 1 (category suggestion) in a way that matters: Level 1's
output was constrained to a finite set (the `Category` enum), so it could be
validated structurally — only a valid category passed. A reply is free-form
text; there is no finite set to validate it against. The question is what keeps
a non-deterministic, free-text output safe when it may be sent to a customer.

## Decision

- **The rail is a mandatory human gate, not structural validation.** The LLM
  produces a draft; it is never sent automatically. A human reviews and edits
  the draft, then sends it. Because the output cannot be validated against a
  fixed set, the human review *is* the validation. (Level 1's rail was the enum;
  Level 2's rail is the gate.)
- **The human sends under their own name.** The reply is authored by the human
  who reviewed it, not by "the AI". Whoever sends it owns it; accountability
  stays with the person, which is the point of the gate.
- **The draft is generated from the ticket's context:** title, description, and
  existing response history, so it is relevant to the conversation.
- **Drafting is opt-in and non-blocking** (per ADR 0008): responding to a ticket
  never calls the LLM automatically; drafting is a separate, optional action.
  The manual reply path always works.
- **The endpoint is read-only:** `POST /api/tickets/{id}/draft-reply` returns a
  draft string and persists nothing. Only the human's subsequent existing
  "respond" call writes to the system.

## Consequences

Positive:

- Free-text AI output never reaches a customer unreviewed; the gate is part of
  the workflow's structure, not a guideline.
- Accountability is unambiguous: the sender owns the message.
- The drafting endpoint writes nothing, so it cannot corrupt state; only the
  existing, validated `respond` path persists.
- It reuses the opt-in, non-blocking cost posture from ADR 0008.

Tradeoffs:

- A human must review every draft, so this speeds drafting, not sending. That is
  intentional at Level 2; full autonomy is Level 3.
- Draft quality depends on the prompt and the ticket context; a weak draft wastes
  the reviewer's time, but cannot do harm because it is gated.

## Alternatives considered

- **Auto-send AI replies.** Rejected at Level 2: free text to a customer with no
  review is exactly the risk the gate exists to prevent. Autonomy is introduced
  deliberately at Level 3, with its own controls.
- **Validate the reply structurally (regex, banned words).** Rejected as the
  primary rail: free-form replies cannot be reduced to a finite valid set;
  lightweight checks may complement the gate but cannot replace it.
- **Mark the sent reply as "AI-authored".** Rejected for now: the human edits and
  sends it, so they author and own it; attributing it to the AI would blur
  accountability. Revisit if provenance tracking becomes a requirement.