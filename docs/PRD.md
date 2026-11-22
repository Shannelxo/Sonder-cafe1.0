# Product Requirements Document: Sonder Cafe

| | |
|---|---|
| Module | CTEC3703 Mobile Application Development |
| Option | A, individual (Customer Application) |
| Client / stakeholder | Lab tutor |
| Version | 0.1 (initial requirements) |

## 1. Purpose

Sonder Cafe wants a mobile app so customers can order ahead and collect their order when it's ready, instead of queueing at the counter. This document sets out what the first version must do. Any changes agreed with the client afterwards are recorded in [CHANGELOG.md](../CHANGELOG.md) as change requests (CR-xx).

## 2. Goals

1. Let customers order quickly from their phone and know exactly when to collect.
2. Keep customer data secure and collect only what the service needs.
3. Help customers find items they're likely to enjoy, using their order history and the current conditions.
4. Deliver a maintainable, tested codebase that can grow into a full café system later.

## 3. Users

| User | Description |
|---|---|
| Customer | Orders drinks and food for collection. Wants speed, clear prices and to know when their order is ready. |
| Admin (simulated) | Café staff member who processes orders. In this version their actions are simulated by a background job, plus a restricted admin console. |

## 4. Scope

**In scope:** customer accounts, menu browsing and search, cart, ordering, order tracking, notifications, feedback and ratings, recommendations, simulated order processing, and a minimal admin console for order statuses.

**Out of scope for this version:** online payment, loyalty points, menu editing by staff, multiple café branches, and cloud sync between devices.

## 5. Functional requirements

| ID | Requirement | Brief ref. | Priority |
|---|---|---|---|
| FR-01 | A customer can register with a display name, an email and a password. | 3.1.1 | Must |
| FR-02 | A registered customer can log in and out. The session stays active after the app restarts. | 3.1.1 | Must |
| FR-03 | The app shows the menu grouped by category (coffee, tea, cold drinks, cakes, snacks). | 3.1.2 | Must |
| FR-04 | Each item shows its name, description, price and availability. Unavailable items can't be added to the cart. | 3.1.3 | Must |
| FR-05 | The customer can search the menu by name and filter by category and availability. | 3.1.4 | Must |
| FR-06 | The customer can add items to a cart with a quantity from 1 to 20. | 3.1.5 | Must |
| FR-07 | The customer can view the cart, change quantities, remove items and see the total. | 3.1.6 | Must |
| FR-08 | The customer can place an order from the cart and sees a confirmation with the order number. | 3.1.7 | Must |
| FR-09 | The customer can view past orders and the live status of current ones. | 3.1.8 | Must |
| FR-10 | The customer can rate a collected order (1 to 5 stars) and leave an optional comment. | 3.1.9 | Must |
| FR-11 | The customer gets a notification each time an order's status changes. | 3.1.10 | Must |
| FR-12 | New orders are processed automatically through Pending, Preparing, Ready for Collection and Collected. | 3.1 (individual note) | Must |
| FR-13 | The app recommends items based on the customer's order history, overall popularity, ratings and the current weather. Each recommendation shows why it was suggested. | 4 | Must |
| FR-14 | An admin account can open an admin console to view all orders and change their status. Customers can't open it. | 3.2.1, 3.2.6 | Should |
| FR-15 | A customer can delete their account and all of its data. | 5 (privacy) | Should |

## 6. Non-functional requirements

| ID | Category | Requirement |
|---|---|---|
| NFR-01 | Security | Passwords are stored only as salted PBKDF2-HMAC-SHA256 hashes. A failed login doesn't reveal whether the email exists. Repeated failed logins trigger a short lockout. |
| NFR-02 | Security | No secrets, keys or credentials are kept in the source code or repository. |
| NFR-03 | Security | All network calls use HTTPS. Cleartext traffic is blocked. |
| NFR-04 | Privacy | Only a display name, an email and a password are collected. Location permission isn't requested. |
| NFR-05 | Privacy | App backup is disabled, so personal data isn't copied off the device. |
| NFR-06 | Reliability | Database and network errors are caught and shown as clear messages. The app doesn't crash on bad input or when offline. |
| NFR-07 | Reliability | If weather data can't be fetched, recommendations still work using the remaining signals. |
| NFR-08 | Usability | Every input error is shown next to the field it relates to and explains how to fix it. |
| NFR-09 | Accessibility | Interactive elements have content descriptions and touch targets of at least 48dp. Text respects the system font size. |
| NFR-10 | Maintainability | Layered architecture (UI, domain, data) with dependency injection. Business logic is unit tested. |
| NFR-11 | Compatibility | Runs on Android 8.0 (API 26) and later. |

## 7. Advanced integration

**Context-aware recommendations.** A recommendation engine gives each available menu item a score made up of:

- how often this customer has ordered it (personal history)
- how often all customers order it (popularity)
- how well it suits the current weather, from the Open-Meteo REST API (for example, cold drinks on warm days and hot drinks on cold or rainy days)
- its average customer rating

The highest-scoring items appear in a "Recommended for you" section on the menu, each with a short reason. This affects what customers see and order, so it's part of the app's core behaviour rather than a separate widget.

Weather results are cached for a short time. Network failures are handled by falling back to history-only recommendations.

## 8. Assumptions and constraints

- The app runs on one device per user, with data stored locally in Room.
- The café has one fixed location, which is used for the weather lookup.
- Prices are in pounds sterling and stored as whole pence.
- The project must be built in Android Studio with Kotlin.

## 9. Risks

| Risk | Impact | Mitigation |
|---|---|---|
| The weather API is unavailable or slow | Weaker recommendations | Timeout, short cache, and fall back to the other signals |
| Android throttles or postpones background work | Status changes arrive late | Short one-off WorkManager jobs, and the admin console can change statuses by hand |
| The user denies notification permission (Android 13+) | No status alerts | Status is still shown live on the order screens |
| Requirements change during development | Rework | Short sprints. Every change is logged as a CR and linked to commits. |

## 10. Acceptance

Each functional requirement is accepted when its backlog item's acceptance criteria are met and the related test cases in [TEST_PLAN.md](TEST_PLAN.md) pass.
