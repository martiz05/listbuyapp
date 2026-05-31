# BuyApp

BuyApp is an Android-first personal shopping application for creating reusable
shopping lists, recording completed purchases and analyzing how prices change
over time. Although the initial use case is supermarket shopping, the domain
model must remain adaptable to other kinds of purchases.

The MVP is for one user per account. The codebase must be designed so that an
iOS application and shared-list features can be added later without rewriting
the core business logic.

## Product Goals

- Create, edit, duplicate, archive and reuse shopping lists.
- Mark items as pending, selected or unavailable while shopping.
- Reuse frequent products and recently purchased items.
- Record the actual quantity and price paid for each selected item.
- Compare historical prices by product, date range, supermarket and branch.
- Track personal price inflation based on frequently purchased products.
- Define monthly budgets and optional budgets by category.
- Notify users when a product price increase exceeds their configured threshold.
- Work reliably during a shopping trip even when the device is offline.

## Confirmed Business Rules

### Products and Barcodes

- A generic product and a purchasable variant are different concepts.
  For example, `Milk` is a product and `Brand X Whole Milk 1 L` is a variant.
- Variants may include brand, package size, unit of measure and one or more
  barcodes.
- Quantities support decimals for products sold by weight, volume or partial
  units.
- A product is considered frequent after at least 3 completed purchases during
  the previous 90 days. This rule should be configurable later.
- Android barcode scanning must support common retail formats such as EAN and
  UPC. Manual entry must always remain available.
- Open Food Facts may be queried as an optional aid after scanning a barcode.
  It is never the authoritative catalog and the application must work without
  it.

### Purchases, Stores and Prices

- Each completed purchase belongs to one supermarket and one branch.
- Each purchase uses exactly one currency. A receipt containing multiple
  currencies must be split into separate purchases.
- Historical prices preserve the original amount paid and the normalized price
  per applicable unit, such as kilogram, liter or unit.
- Price comparisons must support product, selected products, date range,
  supermarket and branch filters.

### Currencies

- The user selects a base or national currency, initially expected to be `NIO`.
- The user may register multiple foreign currencies, initially including `USD`.
- Currencies must not be hardcoded. Use ISO 4217 currency codes.
- A purchase made in a foreign currency requires the exchange rate applicable
  to the purchase date.
- Exchange rates use the convention:
  `1 foreign currency unit = X base currency units`.
- Store the historical exchange rate with the purchase. Later exchange-rate
  changes must not rewrite previous purchase values.
- Reports and budgets use the base currency while preserving original amounts
  for traceability.

### Personal Inflation

Personal inflation is an estimate derived from completed purchases. It is not a
replacement for a country's official consumer price index.

- Build the comparison basket from frequently purchased products.
- Products merely added to a list do not affect inflation until a completed
  purchase provides a confirmed price.
- Compare equivalent normalized quantities between dates.
- Keep these concepts separate in reports:
  - price variation for an individual product;
  - price variation by supermarket or branch;
  - total spending variation;
  - estimated personal inflation for the frequent-product basket.

Initial formula:

```text
personal inflation (%) =
((basket cost at end date / basket cost at start date) - 1) * 100
```

### Budgets and Alerts

- Support one monthly overall budget.
- Support optional monthly budgets by category.
- Price increase alerts compare a variant against its previous recorded
  purchase price.
- Each user configures the percentage increase that triggers an alert. A
  default may be suggested by the UI but must not be enforced as a fixed rule.
- Statistics include spending by category and comparisons over time.

## Technology Direction

### Mobile Application

- Kotlin Multiplatform to share business logic with a future iOS application.
- Compose Multiplatform and Material 3 for the user interface.
- Android is the only delivered target during the MVP.
- Room for offline-first local persistence.
- Coroutines and Flow for asynchronous and reactive state.
- CameraX and ML Kit Barcode Scanning for Android barcode capture.
- A synchronization queue for changes made while the device is offline.

The UI should optimize the in-store flow: large touch targets, fast item
completion, clear pending and unavailable states, accessible contrast, dark
mode and minimal navigation depth.

### Backend

- ASP.NET Core Web API on the current .NET 10 LTS release.
- PostgreSQL as the relational database.
- Entity Framework Core with Npgsql.
- A modular monolith with Clean Architecture boundaries:
  `Domain`, `Application`, `Infrastructure` and `Api`.
- SOLID principles and design patterns where they reduce concrete complexity.
- Unit tests for domain rules and integration tests for API and persistence.
- Docker Compose for local API and PostgreSQL dependencies.

PostgreSQL is preferred over MongoDB because purchases, variants, branches,
currencies and historical prices require relational integrity, transactions
and analytical queries.

### Authentication

- Email and password authentication for the MVP.
- ASP.NET Core Identity for user management.
- Short-lived access tokens and rotating refresh tokens.
- Store refresh tokens securely and never expose them in logs.
- Google sign-in is planned for a later phase.
- Android secrets and tokens must use platform-backed secure storage.

## Initial Domain Model

```text
User
UserSettings
Currency
ExchangeRate
Store
StoreBranch
Category
Product
ProductVariant
ProductBarcode
UnitOfMeasure
ShoppingList
ShoppingListItem
Purchase
PurchaseItem
Budget
PriceAlertPreference
RefreshToken
SyncOperation
```

## Proposed Repository Structure

```text
backend/
  src/
    BuyApp.Domain/
    BuyApp.Application/
    BuyApp.Infrastructure/
    BuyApp.Api/
  tests/
mobile/
  shared/
  androidApp/
  iosApp/                 # Future host application
docs/
  adr/                    # Architecture Decision Records
```

## Security and Configuration

Never commit credentials, API keys, tokens, signing keys, connection strings
with passwords, production data or personally identifiable information.

- Keep local secrets in environment variables, .NET user secrets or ignored
  local files.
- Commit only sanitized examples such as `.env.example`.
- Treat mobile signing files and provider configuration files as secrets.
- Use placeholders in documentation and sample configuration.
- Review staged changes before every commit.

## Planned Delivery Stages

1. Define architecture decisions, domain rules, API contracts and UX flows.
2. Scaffold backend, database migrations and authentication.
3. Scaffold the Android-first Kotlin Multiplatform application.
4. Implement offline shopping lists, product catalog and synchronization.
5. Add completed purchases, branch-aware price history and barcode scanning.
6. Add multimonetary reporting, budgets, configurable alerts and personal
   inflation analysis.
7. Harden security, automated tests, observability and release workflows.

## Git Conventions

- Use small, focused commits following Conventional Commits.
- Keep the repository free of secrets and personal data.
- Use a personal Git identity for this repository.
- Document significant architecture decisions in `docs/adr/`.

## Current Implementation

The initial scaffold includes:

- A .NET 10 solution with `Domain`, `Application`, `Infrastructure` and `Api`
  projects.
- PostgreSQL persistence through Entity Framework Core and Npgsql.
- ASP.NET Core Identity email and password endpoints under `/api/v1/auth`.
- An authenticated profile endpoint at `/api/v1/account/me`.
- A health endpoint at `/health`.
- Domain rules and tests for ISO currency codes and historical exchange-rate
  conversion.
- An initial Entity Framework Core migration for Identity tables.
- An Android-first Kotlin Multiplatform project with shared Compose UI and an
  Android host application.
- Authenticated API operations to create shopping lists, add items with decimal
  quantities and update item status.
- Room persistence shared through Kotlin Multiplatform, including a versioned
  schema for shopping lists, items and pending synchronization operations.
- A Ktor client for registration, login, token refresh and authenticated list
  synchronization.
- An offline-first Compose flow to create a list, add products with decimal
  quantities and mark selected products. Changes are stored locally before
  synchronization and retried with client-generated identifiers.

The current mobile session store intentionally keeps tokens in memory only.
Persistent mobile login must be added later with Android Keystore-backed secure
storage; tokens must never be written to plain preferences or committed files.

## Local Development

### Requirements

- .NET SDK `10.0.300` or a compatible newer feature-band release.
- Docker Desktop for local PostgreSQL.
- JDK 17.
- Android SDK Platform 36 and Build Tools 36 for the Android application.

### PostgreSQL

Copy `.env.example` to `.env`, replace the placeholder with a local-only
password and start PostgreSQL:

```powershell
docker compose up -d postgres
```

### Backend

Configure the local connection string with .NET user secrets. Replace both
password placeholders with the value selected in `.env`:

```powershell
dotnet user-secrets set `
  "ConnectionStrings:BuyAppDatabase" `
  "Host=localhost;Port=5432;Database=buyapp;Username=buyapp;Password=replace-with-your-local-password" `
  --project backend/src/BuyApp.Api

dotnet ef database update `
  --project backend/src/BuyApp.Infrastructure `
  --startup-project backend/src/BuyApp.Api

dotnet run --project backend/src/BuyApp.Api
```

Verify the API at `http://localhost:5130/health`.

Run the backend checks:

```powershell
dotnet build BuyApp.sln --configuration Release
dotnet test BuyApp.sln --configuration Release --no-build
```

### Android

From the `mobile` directory, build the Android debug APK:

```powershell
.\gradlew.bat :androidApp:assembleDebug
```

The generated APK is written below `mobile/androidApp/build/outputs/apk/debug`.

The debug application uses `http://10.0.2.2:5130` to reach the backend running
on the host machine from the Android emulator. Cleartext HTTP is enabled only
for local debug development. Production builds must use HTTPS and a
release-specific API URL.
