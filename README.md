# Helpdesk

A support-ticket (helpdesk) backend built with Spring Boot. Tickets move through
a status **state machine**, carry an **SLA** derived from their priority, and are
exposed through a REST API.

This is **v1 — deterministic, with no AI** — built as the foundation for a later
AI integration phase. The business rules live in the domain, so any caller,
including future AI components, is bound by the same constraints.

The Angular frontend lives in a separate repository: [helpdesk-frontend](https://github.com/gustavo-as/helpdesk-frontend).

## Tech stack

- Java 21, Spring Boot 4.1
- Spring Web, Spring Data JPA, Bean Validation
- H2 (in-memory database)
- springdoc-openapi (Swagger UI)
- Maven

## Requirements

- JDK 21 or newer
- Maven 3.9+

## Run

```bash
mvn spring-boot:run
```

The application starts on port **8080**.

- API base:    http://localhost:8080/api/tickets
- Swagger UI:  http://localhost:8080/swagger-ui.html
- H2 console:  http://localhost:8080/h2-console (user `sa`, no password; the JDBC
  URL is printed in the startup log, since the in-memory database uses a random name)

The H2 database is in-memory and resets on every restart.

## API

| Method | Path                          | Description                                |
|--------|-------------------------------|--------------------------------------------|
| POST   | /api/tickets                  | Create a ticket                            |
| GET    | /api/tickets                  | List tickets (optional `?status=`)         |
| GET    | /api/tickets/{id}             | Get a ticket with its responses            |
| POST   | /api/tickets/{id}/assign      | Assign the ticket to an agent              |
| POST   | /api/tickets/{id}/transition  | Change status (validated by the state machine) |
| POST   | /api/tickets/{id}/responses   | Add a response                             |

An illegal status transition returns **409 Conflict** — the rule lives in the
domain and is enforced at the API boundary.

## Documentation

The reasoning behind each decision is documented:

- `docs/STUDY.md` — the evolutionary narrative of the project.
- `docs/adr/` — Architecture Decision Records, one per significant decision.

## CORS

The API allows the Angular development origin `http://localhost:4200`.