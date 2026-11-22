# Test Plan

## 1. Approach

| Level | Tool | What it covers | Command |
|---|---|---|---|
| Unit (JVM) | JUnit 4, coroutines-test, Turbine, MockWebServer | Validation, sanitising, hashing, money formatting, order status rules, recommendation engine, use cases, weather repository, ViewModels | `./gradlew testDebugUnitTest` |
| Instrumented | AndroidX Test, Room in-memory database | DAO queries, transactions, foreign keys | `./gradlew connectedDebugAndroidTest` |
| UI | Compose UI test | Login form errors, menu search, cart quantity changes | `./gradlew connectedDebugAndroidTest` |
| Manual | Emulator / device | End-to-end flows, notifications, offline behaviour | See section 3 |

Unit tests use fake repositories instead of Room or the network, so they're fast and repeatable. UI tests run against the stateless screen composables with fixed test data.

## 2. Automated test inventory

| Test class | Covers |
|---|---|
| `MoneyFormatterTest` | PB-03 |
| `InputSanitizerTest` | PB-03 |
| `ValidatorsTest` | PB-03, PB-06 |
| `PasswordHasherTest` | PB-03, NFR-01 |
| `OrderStatusTest` | PB-13 |
| `RecommendationEngineTest` | PB-17 |
| `PlaceOrderUseCaseTest` | PB-11 |
| `WeatherRepositoryImplTest` | PB-16 |
| `LoginViewModelTest` | PB-07 |
| `CartViewModelTest` | PB-10 |
| `CartDaoTest` | PB-05, PB-10 |
| `OrderDaoTest` | PB-05, PB-11 |
| `LoginScreenTest` | PB-07 |
| `MenuScreenTest` | PB-09 |
| `CartScreenTest` | PB-10 |

## 3. Manual test cases

Fill in the result, date and notes when each case is run. Record the device or emulator used.

| ID | Backlog | Steps | Expected result | Result | Date | Notes |
|---|---|---|---|---|---|---|
| TC-01 | PB-06 | Register with a valid name, email and password | Account created, menu shown | | | |
| TC-02 | PB-06 | Register with an invalid email or a short password | Field error explains the problem | | | |
| TC-03 | PB-06 | Register with an email that is already used | "An account with this email already exists" | | | |
| TC-04 | PB-07 | Log in with a wrong password | Generic error, no hint about whether the email exists | | | |
| TC-05 | PB-07 | Fail login 5 times in a row | Login is temporarily locked with a message | | | |
| TC-06 | PB-07 | Log in, close the app, reopen it | Still logged in | | | |
| TC-07 | PB-08 | Open an item from the menu | Name, description, price and availability shown | | | |
| TC-08 | PB-09 | Search "latte", then filter by category | Only matching items shown | | | |
| TC-09 | PB-10 | Try to add an unavailable item | Add button disabled, reason shown | | | |
| TC-10 | PB-10 | Change quantity in the cart, then remove an item | Total updates correctly | | | |
| TC-11 | PB-11 | Place an order | Confirmation with order number, cart emptied | | | |
| TC-12 | PB-12, PB-13 | Watch an order on the order detail screen | Status moves through every stage without refreshing | | | |
| TC-13 | PB-14 | Place an order with the app in the background | A notification appears for each status change | | | |
| TC-14 | PB-14 | Deny notification permission, then place an order | No crash, status still updates in the app | | | |
| TC-15 | PB-15 | Rate a collected order | Rating saved, can't rate the same order twice | | | |
| TC-16 | PB-16, PB-17 | Open the menu with internet on | Recommendations shown with a weather-based reason | | | |
| TC-17 | PB-16, PB-17 | Turn on aeroplane mode, open the menu | Recommendations still shown, no crash | | | |
| TC-18 | PB-18 | Log in as a customer and look for the admin console | Not visible or reachable | | | |
| TC-19 | PB-18 | Log in as admin and change an order status | Customer's order shows the new status | | | |
| TC-20 | PB-21 | Delete account from the Account screen | Logged out, can't log in with the old details | | | |

## 4. Test environment

| Item | Details |
|---|---|
| Emulator / device | |
| Android version | |
| Android Studio version | |

## 5. Results summary

Record the outcome of each full test run here (date, number passed or failed, issues raised).

| Date | Unit | Instrumented / UI | Manual | Issues found |
|---|---|---|---|---|
| | | | | |
