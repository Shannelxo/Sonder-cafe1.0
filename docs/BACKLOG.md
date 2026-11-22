# Product Backlog

Priority uses MoSCoW: **M**ust, **S**hould, **C**ould, **W**on't (this version).
Status values: Planned, In progress, Done, Changed (see CR), Dropped.

Change requests from client meetings are logged in [CHANGELOG.md](../CHANGELOG.md). Any backlog items they create or modify reference the CR ID.

## Sprint 1: Setup and design (ends 29 Oct 2026)

| ID | User story / task | Acceptance criteria | Req. | Priority | Status |
|---|---|---|---|---|---|
| PB-01 | Set up the Android project with Compose, Hilt, Room and a version catalog | Project syncs and builds. Dependencies are managed in `libs.versions.toml`. | NFR-10 | M | Planned |
| PB-02 | Write the PRD, backlog, test plan and meeting/change record templates | Documents are in the repo and linked from the README | - | M | Planned |
| PB-03 | Core helpers: validation, input sanitising, money formatting, password hashing | All helpers have passing unit tests | NFR-01, NFR-08 | M | Planned |
| PB-04 | Domain models and repository interfaces | Models cover users, menu, cart, orders, feedback and weather | NFR-10 | M | Planned |

## Sprint 2: Core features (ends 5 Nov 2026)

| ID | User story / task | Acceptance criteria | Req. | Priority | Status |
|---|---|---|---|---|---|
| PB-05 | Room database, DAOs and seeded menu | Menu appears on first launch. DAO tests pass. | FR-03 | M | Planned |
| PB-06 | As a customer, I can register an account | Invalid input shows field errors. A duplicate email shows a clear message. The password is stored hashed. | FR-01 | M | Planned |
| PB-07 | As a customer, I can log in and out, and stay logged in after a restart | Wrong details show a generic error. Lockout after 5 failed attempts. | FR-02, NFR-01 | M | Planned |
| PB-08 | As a customer, I can browse the menu and view item details | Items are grouped by category. Price, description and availability are shown. | FR-03, FR-04 | M | Planned |
| PB-09 | As a customer, I can search and filter the menu | Results update as I type. Category and "available only" filters work together. | FR-05 | M | Planned |
| PB-10 | As a customer, I can add items to my cart and change or remove them | Quantity is limited to 1–20. Unavailable items can't be added. The total updates. | FR-06, FR-07 | M | Planned |
| PB-11 | As a customer, I can place an order and see a confirmation | An empty cart can't be ordered. The cart clears afterwards. The confirmation shows the order number. | FR-08 | M | Planned |

## Sprint 3: Integration (ends 12 Nov 2026)

| ID | User story / task | Acceptance criteria | Req. | Priority | Status |
|---|---|---|---|---|---|
| PB-12 | As a customer, I can view past orders and track the current one | The status changes on screen without a refresh | FR-09 | M | Planned |
| PB-13 | Simulated order processing | A new order moves through every status automatically. Each change is saved. | FR-12 | M | Planned |
| PB-14 | As a customer, I get a notification when my order's status changes | A notification is sent for each change. Notification permission is requested on Android 13+. | FR-11 | M | Planned |
| PB-15 | As a customer, I can rate and comment on a collected order | Rating 1–5 is required. The comment is optional, up to 500 characters. One rating per order. | FR-10 | M | Planned |
| PB-16 | Fetch current weather for the café from Open-Meteo | Data is cached. Timeouts and errors are handled without crashing. | FR-13, NFR-07 | M | Planned |
| PB-17 | As a customer, I see "Recommended for you" items with a reason | Uses history, popularity, rating and weather. Still works offline. | FR-13 | M | Planned |
| PB-18 | As an admin, I can view all orders and change their status | Only admin accounts can reach the console | FR-14 | S | Planned |

## Sprint 4: Finish (ends 16 Nov 2026)

| ID | User story / task | Acceptance criteria | Req. | Priority | Status |
|---|---|---|---|---|---|
| PB-19 | Unit tests for validation, security, recommendations, use cases and ViewModels | All pass with `testDebugUnitTest` | NFR-10 | M | Planned |
| PB-20 | Instrumented tests for DAOs and key Compose screens | All pass with `connectedDebugAndroidTest` | NFR-10 | M | Planned |
| PB-21 | As a customer, I can delete my account and data | Removes the user, cart, orders and feedback, then logs out | FR-15, NFR-04 | S | Planned |
| PB-22 | Accessibility and UI polish | Content descriptions, 48dp touch targets, empty and loading states | NFR-09 | S | Planned |
| PB-23 | Final documentation and report support | README, test plan results and change log are up to date | - | M | Planned |

## Ideas for later (Won't this version)

| ID | Idea |
|---|---|
| PB-90 | Online payment |
| PB-91 | Loyalty points |
| PB-92 | Cloud sync and a separate admin app |
