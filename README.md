# Sonder Cafe

An Android app for ordering from Sonder Cafe. Customers can browse the menu, build a cart, place an order for collection and follow it until it's ready.

Built for CTEC3703 Mobile Application Development (Option A: Customer Application).

## Features

- Register and log in. Passwords are stored as salted PBKDF2 hashes, never as plain text.
- Browse the menu by category and view each item's price, description and availability.
- Search by name and filter by category or availability.
- Cart with quantity changes and removal.
- Place orders, get a confirmation, view past orders and track the current one live.
- Notifications when an order's status changes.
- Feedback and star ratings for collected orders.
- **Recommended for you**: suggestions based on your order history, what's popular, item ratings and the current weather at the café.
- Admin console (admin accounts only) for updating order statuses by hand.

### How the café side is simulated

This is the customer app, so there are no café staff to process orders. When an order is placed, a background job (WorkManager) moves it through the real order workflow:

```
PENDING -> PREPARING -> READY_FOR_COLLECTION -> COLLECTED
```

Each step updates the order in the database and sends a notification, using the same code path a real admin action would. The admin console can also change a status by hand, which is useful for demos and shows the role-based access in action.

## Tech stack

| Area | Choice |
|---|---|
| Language / UI | Kotlin, Jetpack Compose (Material 3) |
| Architecture | MVVM with a repository layer and a small domain layer |
| Dependency injection | Hilt |
| Database | Room (SQLite) |
| Session storage | DataStore (Preferences) |
| Networking | Retrofit, OkHttp, kotlinx.serialization |
| Weather data | [Open-Meteo](https://open-meteo.com/) forecast API (no API key needed) |
| Background work | WorkManager |
| Testing | JUnit 4, kotlinx-coroutines-test, Turbine, MockWebServer, Room testing, Compose UI tests |

## Architecture

```
ui/        Compose screens and ViewModels (one UiState per screen)
domain/    Models, repository interfaces, use cases, recommendation engine
data/      Room database, Retrofit API, DataStore session, WorkManager jobs, repository implementations
di/        Hilt modules
core/      Shared helpers: validation, input sanitising, password hashing, money formatting
```

The UI only talks to ViewModels, and ViewModels only depend on domain interfaces. The data layer implements those interfaces, so Room or the network can be swapped for fakes in tests.

Repositories return an `AppResult` (success or a user-friendly error) instead of throwing, so database and network failures turn into clear messages on screen.

## Getting started

**You need:** Android Studio (Ladybug or newer), JDK 17, and an emulator or device running Android 8.0 (API 26) or later.

1. Clone the repository and open it in Android Studio.
2. Android Studio creates `local.properties` with your SDK path. Add the entries listed in `local.properties.example`. These set the demo admin account. Leave them out if you don't need admin access.
3. Sync Gradle and run the `app` configuration.

The menu is seeded on first launch. Register a new account to use the app as a customer.

## Running tests

```bash
# Unit tests (JVM)
./gradlew testDebugUnitTest

# Instrumented and Compose UI tests (needs a running emulator or device)
./gradlew connectedDebugAndroidTest
```

See [docs/TEST_PLAN.md](docs/TEST_PLAN.md) for the full test plan and manual test cases.

## Privacy and security

- No secrets are kept in the repository. Admin seed credentials come from `local.properties`, which git ignores.
- The app collects only a display name, an email and a password. It doesn't ask for location: weather is looked up for the café's fixed address.
- All network traffic uses HTTPS, and cleartext traffic is blocked.
- App backup is turned off, so the local database isn't copied to cloud backups.
- User input is trimmed, sanitised and length-checked before it's stored.
- Users can delete their account and all of its data from the Account screen.
- Every recommendation shows why it was suggested.

## Documentation

- [Product requirements (PRD)](docs/PRD.md)
- [Product backlog](docs/BACKLOG.md)
- [Test plan](docs/TEST_PLAN.md)
- [Client meeting records](CLIENT_MEETINGS.md)
- [Change log and change requests](CHANGELOG.md)
