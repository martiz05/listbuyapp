# ADR 0003: Use Kotlin Multiplatform for an Android-first mobile app

## Status

Accepted

## Context

Android is the only MVP target, but an iOS client is expected later. The app
must also work offline during shopping trips.

## Decision

Use Kotlin Multiplatform and Compose Multiplatform. Deliver only the Android
application during the MVP. Keep platform integrations, including Android
barcode capture, behind platform-specific boundaries.

## Consequences

- Business rules and suitable UI components can be shared with a future iOS
  host.
- Android-specific integrations remain available where needed.
- The mobile build requires a modern JDK, Gradle and Android SDK.
