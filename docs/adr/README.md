# Architecture Decision Records

This folder records significant decisions made while building the project — one
file per decision.

Each record is numbered and treated as immutable: when a decision changes, we
add a new record that supersedes the old one rather than editing history. That
keeps the evolution of the reasoning visible, which is the point of an
evolutionary study.

The domain classes themselves are kept clean (no explanatory comments). The
"why" lives here, and the **Governs** column below links each decision back to
the code it explains.

Each record follows the same shape: Context, Decision, Consequences,
Alternatives considered.

| #    | Decision                                            | Governs                          | Status   |
|------|-----------------------------------------------------|----------------------------------|----------|
| 0001 | Encode ticket status transitions as a state machine | TicketStatus.java                | Accepted |
| 0002 | Derive the SLA from priority, in the domain         | Priority.java                    | Accepted |
| 0003 | Keep ticket rules inside a rich domain entity       | Ticket.java, TicketResponse.java | Accepted |