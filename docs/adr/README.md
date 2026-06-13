# Architecture Decision Records

This folder records significant decisions made while building the project — one
file per decision.

Each record is numbered and treated as immutable: when a decision changes, we
add a new record that supersedes the old one rather than editing history. That
keeps the evolution of the reasoning visible, which is the point of an
evolutionary study. (See ADR 0006, which supersedes the original placement of
the exception classes.)

The domain and application classes themselves are kept clean (no explanatory
comments). The "why" lives here, and the **Governs** column below links each
decision back to the code it explains.

Each record follows the same shape: Context, Decision, Consequences,
Alternatives considered.

| #    | Decision                                                  | Governs                                  | Status   |
|------|-----------------------------------------------------------|------------------------------------------|----------|
| 0001 | Encode ticket status transitions as a state machine       | TicketStatus.java                        | Accepted |
| 0002 | Derive the SLA from priority, in the domain               | Priority.java                            | Accepted |
| 0003 | Keep ticket rules inside a rich domain entity             | Ticket.java, TicketResponse.java         | Accepted |
| 0004 | The service orchestrates domain rules and returns entities| TicketService.java                       | Accepted |
| 0005 | Separate DTOs at the web boundary, with summary and detail | controller/ (controller, dto, handler)  | Accepted |
| 0006 | Centralize exceptions in a top-level package              | exception/                               | Accepted |