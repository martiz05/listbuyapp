# ADR 0001: Start with a modular monolith

## Status

Accepted

## Context

BuyApp needs shopping lists, purchases, catalog management, reporting,
currencies and authentication. The product is at its first implementation
stage and does not yet require independently deployed services.

## Decision

Build one ASP.NET Core API using Clean Architecture boundaries:

- `BuyApp.Domain` contains business rules and value objects.
- `BuyApp.Application` contains use cases and ports.
- `BuyApp.Infrastructure` contains persistence and external integrations.
- `BuyApp.Api` exposes HTTP endpoints and composes dependencies.

Keep feature boundaries explicit so that a module can be extracted later only
if deployment or scaling requirements justify the added operational cost.

## Consequences

- Local development and deployment remain simple.
- Transactions and refactoring across early modules are straightforward.
- Boundaries must be enforced through project references and code review.
