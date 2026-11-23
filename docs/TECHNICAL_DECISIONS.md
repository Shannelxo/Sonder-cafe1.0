# Technical Decisions

Why the app is built the way it is, and its known limitations. Each entry links to the code it describes. Requirement IDs refer to [PRD.md](PRD.md); backlog IDs to [BACKLOG.md](BACKLOG.md).

## 1. Architecture

| Decision | Reason | Where |
|---|---|---|
| Three layers: UI (Compose + ViewModels), domain (models, repository interfaces, use cases, recommendation engine), data (Room, Retrofit, WorkManager). | Business rules don't depend on Android, so they can be unit tested on the JVM (NFR-10). Swapping Room for a cloud backend later would only touch the data layer. | `domain/`, `data/`, `ui/` |
| Hilt for dependency injection. All interface-to-implementation bindings are in one module. | One place shows how the app is wired. Tests pass fakes through constructors without Hilt. | `di/RepositoryModule.kt` |
| One `StateFlow<UiState>` per screen, plus a stateless `XxxContent` composable for each screen. | The screen draws whatever state it is given, so UI tests drive `XxxContent` with fixed data and need no database or Hilt. | e.g. `ui/cart/CartScreen.kt` |
| Errors are returned as `AppResult.Success` / `AppResult.Error` instead of thrown. Repositories always re-throw `CancellationException`. | Every failure the user can see has a message written for the user (NFR-06). Re-throwing cancellation keeps coroutine cancellation working. | `core/result/AppResult.kt` |
| String routes with IDs as path arguments, read through `SavedStateHandle`. | ViewModels can be tested without Android's `Bundle`. | `ui/navigation/Routes.kt` |

## 2. Data storage (Room)

| Decision | Reason | Where |
|---|---|---|
| Six tables. Foreign keys cascade on delete. | Deleting a user removes their cart, orders and feedback in one statement (FR-15). | `data/local/entity/` |
| Order lines copy the item name and price. They have no foreign key to the menu. | A past order still shows what the customer paid, even if the menu changes later. | `OrderItemEntity.kt` |
| Placing an order is a single `@Transaction` (insert order, insert lines, clear cart). | A failure part-way can't leave an order with no lines, or a cart that was ordered twice. | `OrderDao.placeOrder` |
| Status updates use `WHERE status = :expected`. | If the simulation and the admin change the same order at once, one wins and the other gets a clear message. A step can't be skipped. | `OrderDao.updateStatus` |
| Unique index on `feedback.order_id`. | One rating per order, enforced by the database as well as the repository. | `FeedbackEntity.kt` |
| No `fallbackToDestructiveMigration()`. The schema is exported to `app/schemas`. | A missing migration fails loudly during development instead of silently deleting user data. | `di/DatabaseModule.kt` |

## 3. Security and privacy

| Decision | Reason | Where |
|---|---|---|
| Passwords are hashed with PBKDF2-HMAC-SHA256: 210,000 iterations, a 16-byte random salt, and a constant-time comparison. | The Java standard library has this built in, so no extra dependency is needed. Hashing runs off the main thread. (NFR-01) | `core/security/PasswordHasher.kt` |
| The same error appears for an unknown email and for a wrong password, and an unknown email still runs a full hash. | Neither the message nor the response time reveals which emails have accounts. | `AuthRepositoryImpl.login` |
| 5 failed logins cause a 30-second lockout. | Slows down password guessing on the device. | `AuthRepositoryImpl` |
| Only the user ID is kept in DataStore. | No password or token is stored for the session. | `data/session/SessionManager.kt` |
| Registration always creates a customer. The admin is seeded from `local.properties` in debug builds only. | No admin password in the repository (NFR-02). A customer can't make themselves an admin. | `app/build.gradle.kts`, `DatabaseSeeder.kt` |
| The admin role is checked in navigation, again in the admin ViewModel, and again before every status change. | Reaching the screen by some other route still doesn't allow anything (FR-14). | `SonderCafeNavHost.kt`, `AdminConsoleViewModel.kt` |
| A customer can only open their own orders, even with a valid order ID. | An ID from a notification or route can't expose another customer's order. | `OrderDetailViewModel.kt` |
| Input is validated in the ViewModel for quick feedback, and again in the repository. | The rules hold however the repository is called. | `core/validation/`, repositories |
| Room uses bound parameters. LIKE wildcards in search terms are escaped. | Prevents SQL injection, and a search for `%` matches a literal `%`. | `MenuRepositoryImpl.kt`, `MenuDao.kt` |
| HTTPS only (cleartext blocked). `allowBackup=false`. The database is excluded from cloud backup and device transfer. | NFR-03, NFR-05. | `network_security_config.xml`, `data_extraction_rules.xml` |
| No location permission. The weather request uses the café's fixed coordinates. | Only the minimum personal data is collected (NFR-04). | `WeatherRepositoryImpl.kt` |
| HTTP logging only in debug builds, at `BASIC` level (no request or response bodies). | Nothing sensitive ends up in logcat. | `di/NetworkModule.kt` |

## 4. Recommendations

Score = 0.40 × personal history + 0.25 × popularity + 0.20 × weather fit + 0.15 × rating, with each signal scaled to 0–1. Only available items are ranked, and ties are broken by name.

- **Personal history and popularity** are each divided by the highest count, so one very popular item doesn't drown out the rest.
- **Ratings** are mapped from 1–5 onto 0–1. Unrated items get a neutral 0.5, so new items aren't penalised.
- **Weather fit** favours hot drinks at 12°C or colder, or when it's wet. It favours cold items at 20°C or warmer and dry.
- **Without weather data**, the weather signal counts as 0 for every item, and the screen says that weather isn't being used (NFR-07).
- **Each result has a reason**, such as "One of your favourites" or "Something warm for a 9°C rainy day", so the suggestion doesn't feel random.

This is a transparent weighted score, not machine learning. Every result can be explained and unit tested (`RecommendationEngineTest`). The weights are a design choice and have not been tuned with real usage data.

Code: `domain/recommendation/RecommendationEngine.kt`, `domain/usecase/GetRecommendationsUseCase.kt`.

## 5. Weather (Open-Meteo)

- **Why Open-Meteo:** it's free and needs no API key, so there is no secret to manage.
- **Networking:** Retrofit with kotlinx.serialization, and 10-second timeouts.
- **Caching:** a result is reused for 30 minutes. If a request fails, a result up to 3 hours old is still used.
- **Sanity checks:** values outside a believable range are rejected.
- **Testability:** an injected `Clock` lets the caching be tested with MockWebServer (`WeatherRepositoryImplTest`).

## 6. Order simulation and notifications

- **WorkManager chain:** after an order is placed, the status moves Pending → Preparing → Ready for collection → Collected. Each step waits 15s, 30s and 45s after the previous one finishes, so the whole order takes about 90 seconds.
  - WorkManager survives the app being closed.
  - The chain is unique per order, so a duplicate tap can't start a second chain.
  - Each worker does nothing if the order has already reached its target status (FR-12).
- **One notification per order**, updated at each status change. Tapping it opens that order (FR-11). On Android 13+ the permission is requested when the first order is placed. If it's refused, the app still shows the live status.

Code: `data/work/`, `core/notifications/AndroidOrderNotifier.kt`, `MainActivity.kt`.

## 7. Accessibility

- **Labelled controls:** every icon button has a content description that names its item, e.g. "Increase quantity of Latte" or "Remove Latte". Decorative icons have none.
- **Grouped announcements:** composite widgets are read as one phrase, e.g. "Rated 4 out of 5", "Order status: Preparing", "3 items in cart".
- **Rating input:** the editable star rating behaves as a radio group.
- **Banners:** error and info banners are polite live regions, so TalkBack reads them when they appear.
- **Touch targets:** at least 48dp. Text sizes are in `sp`, so they follow the system font size (NFR-09).
- **Colour:** the colour pairs were chosen with WCAG AA in mind. They should be checked with a contrast tool before AA is claimed.

## 8. Known limitations

| Limitation | Effect | Possible improvement |
|---|---|---|
| The login lockout is held in memory. | Restarting the app resets the counter. | Store attempts in DataStore or Room, with a timestamp. |
| 210,000 PBKDF2 iterations | Below OWASP's current advice of 600,000 for PBKDF2-HMAC-SHA256. Chosen to keep login fast on older phones. | Raise the count, or use Argon2 through a vetted library. Store the iteration count with each hash so it can change later. |
| The debug admin password is compiled into the debug APK through BuildConfig. | Anyone with the debug APK could extract it. Release builds have empty values. | Create staff accounts on a backend instead. |
| All data is on one device, and the admin uses the same app. | No real multi-user café. Admin and customer share a phone in the demo. | A backend with authentication and a separate staff app (PB-92). |
| The simulation uses fixed delays. | Not a realistic preparation time. | Base the delays on order size, or drive the status from staff actions only. |
| Recommendation weights are hand-picked. | They may not reflect real preferences. | Tune them with real order data, or A/B test them. |
| `InputSanitizer` only trims and limits text. | It is not an SQL-injection defence. Room's bound parameters are. | No change needed; this is documented so it isn't misunderstood. |
