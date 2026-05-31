# ADR 0002: Use PostgreSQL as the system of record

## Status

Accepted

## Context

The application records users, products, variants, stores, branches,
purchases, prices, budgets and historical exchange rates. Reports compare
these records across dates and stores.

## Decision

Use PostgreSQL with Entity Framework Core and Npgsql.

## Consequences

- Relational integrity and transactions protect purchase history.
- SQL supports the reporting queries required for price tracking.
- JSON columns remain available if a future integration needs flexible
  metadata.
