# WatchTower

**Website change monitoring for Android.** Track any URL, get notified when content changes, and see exactly what's different.

---

## What It Does

Add a website URL. WatchTower fetches its HTML, hashes the content, and saves it as a baseline. On subsequent checks — manual or automatic — it compares the new content against that baseline. If something changed, you see it.

**Three states, nothing more:**

| Status | Meaning |
|--------|---------|
| **Passed** | Content matches baseline |
| **Changed** | Content differs from baseline |
| **Error** | Site unreachable or fetch failed |

When a change is detected, the app stores the new HTML and generates a line-by-line diff. You can view the changed content in a WebView with the site's original styling preserved. Hit **Resolve** to accept the change as the new baseline.

---

## Features

- **Dashboard** with live stats — total sites, changes detected, passed count
- **Background monitoring** via WorkManager with configurable intervals (15m to 24h)
- **Parallel checking** with adjustable concurrency pool (1-20 simultaneous requests)
- **HTML diff viewer** showing only changed lines, rendered with original site styles
- **Progress notifications** during batch checks, summary notification on completion
- **Status filtering** — tap a status to filter the site list
- **Countdown timer** showing time until next background check
- **Favicon loading** via Google's favicon service for visual site identification

---

## Tech Stack

| Layer | Library | Version |
|-------|---------|---------|
| UI | Jetpack Compose + Material 3 | BOM 2026.02.00 |
| Navigation | androidx.navigation (type-safe) | 2.9.2 |
| DI | Koin | 4.1.1 |
| HTTP | Ktor Client + OkHttp engine | 3.4.0 |
| Database | Room | 2.7.1 |
| Settings | DataStore Preferences | 1.1.7 |
| Background | WorkManager | 2.10.1 |
| HTML parsing | KSoup | 0.2.5 |
| Diff engine | java-diff-utils | 4.15 |
| Image loading | Coil + Ktor backend | 3.3.0 |
| Logging | Kermit | 2.0.8 |

**Kotlin 2.3.10** / **AGP 8.13.2** / **minSdk 28** / **targetSdk 36**

---

## Architecture

Single-module app using **MVVM + Clean Architecture** across three layers:

```
com.riva.watchtower
├── domain/          Models (Site, SiteStatus) and repository interface
├── data/            Room DB, Ktor HTTP, file storage, DataStore, repository impl
├── presentation/    Compose UI, ViewModels, navigation, theme
├── worker/          WorkManager background check
├── utils/           HTTP client, hashing, diffing, notifications
└── di/              Koin modules
```

**State management:** ViewModels expose `StateFlow<UiState>` and accept sealed `UiEvent` classes. Composables collect state with `collectAsStateWithLifecycle()` and send events back.

**Navigation:** Type-safe routes using `@Serializable` data objects. Three destinations — Home, Detail(siteId), Settings.

---

## How It Works

### Site Checking

```
URL → Ktor fetch → KSoup extract body → MD5 hash → compare against baseline
```

Content extraction strips `<script>`, `<style>`, `<iframe>`, `<svg>`, and `<noscript>` tags before hashing, so cosmetic changes to scripts or styles don't trigger false positives.

### Parallel Execution

Both manual and background checks use `SiteCheckRunner` — a shared coroutine-based engine with a `Semaphore(poolSize)` to throttle concurrent HTTP requests. Pool size is user-configurable in Settings.

### HTML Diff

When a change is detected:
1. Baseline and latest HTML are read from file storage
2. Body content is extracted from both (head styles preserved separately)
3. `DiffUtils.diff()` generates line-level deltas
4. Only INSERT and CHANGE deltas are rendered — deletions are omitted
5. Changed lines are wrapped in a new HTML document with the site's original `<style>` tags
6. Rendered in a WebView with JavaScript disabled

### Background Monitoring

WorkManager runs `SiteCheckWorker` as a foreground service with a progress notification. On completion, a summary notification reports how many sites changed. Configurable interval from 15 minutes to 24 hours with exponential backoff on failure.

---

## Screens

### Home
Dashboard with stats cards, filterable site list, manual check-all with animated progress bar, FAB to add new sites via bottom sheet.

### Detail
Site info card with favicon and status badge, action buttons (open in browser, resolve, recheck, delete), and a WebView rendering the HTML diff when changes are detected.

### Settings
Toggle background monitoring, choose check interval, adjust parallel pool size. All settings persisted immediately to DataStore.

---

## Build

```bash
./gradlew assembleDebug       # Debug APK
./gradlew assembleRelease     # Release APK
./gradlew test                # Unit tests
./gradlew lint                # Lint checks
```

Requires **JDK 11+**.

---

## Project Structure

```
app/src/main/java/com/riva/watchtower/
├── data/
│   ├── db/                    Room database, DAO, entity mappers
│   ├── external/              SiteTrackingProvider (HTTP fetching)
│   ├── local/                 HtmlStorageProvider, SettingsDataStore
│   └── repository/            SiteRepositoryImpl
├── domain/
│   ├── enums/                 SiteStatus (PASSED, CHANGED, ERROR)
│   ├── models/                Site data class
│   └── repository/            SiteRepository interface
├── presentation/
│   ├── components/            RemoteImage, SiteListCard, StatsCard, StatusBadge
│   ├── features/
│   │   ├── home/              Dashboard (ViewModel, UiState, UiEvent, Screen)
│   │   ├── detail/            Site detail + diff viewer
│   │   └── settings/          App settings
│   ├── navigation/            AppDestinations, AppNavHost
│   └── theme/                 Colors, typography, Material 3 theme
├── utils/
│   ├── HttpClientFactory      Ktor client (debug-only logging)
│   ├── SiteCheckRunner        Shared parallel check engine
│   ├── NotificationHelper     Channels, progress & result notifications
│   ├── HtmlContentExtractor   Body extraction, content hashing
│   ├── DateFormatter          Timestamp formatting
│   └── UrlUtils               Domain extraction, favicon URLs
├── worker/
│   ├── SiteCheckWorker        CoroutineWorker for background checks
│   └── WorkScheduler          Periodic work scheduling
├── di/AppModule               Koin dependency graph
├── MainActivity               Single-activity Compose entry point
└── WatchTowerApp              Application class (Koin init, notification channels)
```

---

## License

All rights reserved.
