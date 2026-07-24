# Stromschnelle — Priority Todo Widget App

A single-module Android app + home-screen widget for tracking prioritized todos.
Declarative stack throughout: Jetpack Compose (app), Glance (widget), Room, DataStore, WorkManager.

## Goals
- Track todos: title, description, settable icon, creation date, completion date.
- Manual drag-to-reorder = priority order.
- Completing a todo strikes it through and sinks it to the bottom **but keeps its rank**
  (un-completing restores its original position via an untouched `sortIndex`).
- After a global, configurable grace period (default 1 day) a completed todo is **hidden**
  from the default list — but **never deleted**. It remains in the DB forever and is
  visible in a dedicated "Completed" screen, for later data mining.
- Home-screen widget: shows the priority-sorted list, checkbox to complete/uncomplete,
  tap a row to open the app on that todo's detail.

## Non-goals
- No hard-delete of todos anywhere (data retained permanently).
- No add/edit/reorder from the widget (done in the app).
- No accounts, sync, or cloud.

## Tech / conventions
- Kotlin, Gradle Kotlin DSL. `minSdk 26`, `targetSdk 35`, `compileSdk 35`.
- Package: `de.nereide.stromschnelle`.
- MVVM + unidirectional data flow. Room `Flow` → Repository → ViewModel `StateFlow` → Compose.
- AOSP / Android Studio default style; ktlint-friendly.
- Pinned versions: AGP 8.7.x, Kotlin 2.0.21, Compose BOM 2024.12.01,
  Glance 1.1.1, Room 2.6.1, WorkManager 2.9.1, DataStore-preferences 1.1.1,
  Navigation-compose 2.8.x, kotlinx-coroutines, Turbine (test).
- License: MIT.

## Data model — Room entity `Todo`
| field | type | notes |
|---|---|---|
| `id` | Long PK autogen | |
| `title` | String | |
| `description` | String | shown on detail screen |
| `iconKey` | String | key into bundled `TodoIcon` enum |
| `sortIndex` | Double | manual drag order = priority; Double allows insert-between without renumbering |
| `createdAt` | Long | epoch millis |
| `completedAt` | Long? | null = active; set = completed |

Derived (not stored): active (`completedAt == null`), completed-lingering
(set, within grace window), expired (past window → hidden from default query, kept in DB).

## Architecture (packages)
```
data/    Todo, TodoDao, TodoDatabase, TodoIcon, SettingsRepository (DataStore)
domain/  TodoRepository — Flow<List<Todo>> for active+lingering, Flow for completed,
         add/update/complete(id)/uncomplete(id)/reorder(list), no delete
ui/      TodoListScreen, TodoDetailScreen, TodoEditScreen, CompletedScreen, SettingsScreen,
         ViewModels exposing immutable UiState via StateFlow; NavHost
widget/  StromschnelleWidget (Glance), TodoWidgetReceiver, ToggleCompleteAction
work/    ReapWorker — periodic; recomputes what is expired (query-time cutoff), never deletes
```

## Key behaviors
- **Default list query:** `completedAt IS NULL OR completedAt >= (now - grace)`, ordered by
  `completedAt IS NULL DESC` (active first), then `sortIndex ASC`. Completed-lingering appear
  struck-through at the bottom. "Expired" is purely a query cutoff — no data mutation.
- **Complete/uncomplete:** set/clear `completedAt`. Rank preserved via `sortIndex`.
- **Reorder:** drag in app rewrites `sortIndex` (midpoint between neighbors).
- **Widget:** read + checkbox toggle (`actionRunCallback` → ToggleCompleteAction → repository →
  `StromschnelleWidget.updateAll`), row tap → `actionStartActivity` deep link to detail.
- **Settings:** global grace period (DataStore), default 24h.
- **Reaping:** because expiry is a query cutoff, the "reaper" is just a periodic
  `WidgetUpdate`/list refresh so lingering items disappear at the right time; nothing deleted.

## Testing
- In-memory Room + Turbine: repository active/lingering/completed partitioning, complete/uncomplete
  rank preservation, reorder midpoint logic, grace-cutoff boundary.
- Build must assemble (`./gradlew assembleDebug`).

## Icons
Bundled `TodoIcon` enum mapping keys → Material vector drawables (a curated ~12-icon set),
rendered in both Compose and Glance.
