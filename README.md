# Stromschnelle

Stromschnelle ("rapids" — fast-flowing) is a priority-sorted to-do app for
Android with a home-screen widget, built for getting things done at a
glance and re-ordering priorities as fast as they change.

## Features

- Priority-sorted to-do list — reorder items by drag, priority is purely
  positional (no due dates forcing sort order).
- Each to-do has a title, an optional description, and an icon chosen from
  a small curated icon set.
- Mark items complete without deleting them: completed to-dos stay visible
  for a configurable grace period, then drop out of the active list.
- Completed to-dos are **never deleted** — see Data retention below.
- Home-screen widget (Glance) showing the current priority-sorted list,
  so you can see and complete to-dos without opening the app.
- Deep link from the widget straight into a to-do's detail screen
  (`stromschnelle://todo/{id}`).
- Configurable settings (e.g. grace period) via DataStore.

## Tech stack

- Kotlin
- Jetpack Compose (Material 3) for the app UI
- Glance for the home-screen widget
- Room for local persistence
- DataStore (Preferences) for settings
- WorkManager for background maintenance (e.g. periodic widget refresh /
  grace-period sweeps)
- Plain Kotlin service locator for dependency injection (`AppContainer`,
  no Hilt/Dagger)

Architecture follows unidirectional data flow: Compose UI -> ViewModel
(exposing immutable `UiState` via `StateFlow`) -> `TodoRepository` ->
Room/DataStore. ViewModels obtain their dependencies via
`(application as StromschnelleApp).container`.

## Build & run

1. Open the project root in Android Studio (Koala/Ladybug or newer).
2. Let Android Studio sync Gradle. The binary `gradle/wrapper/gradle-wrapper.jar`
   is not committed to version control (binaries don't belong in git
   history); Android Studio regenerates it automatically on first sync.
   If you're building from the command line instead and the wrapper jar is
   missing, regenerate it with a locally installed Gradle:

   ```
   gradle wrapper --gradle-version 8.9
   ```

   then use `./gradlew` as usual (e.g. `./gradlew assembleDebug`).
3. Run the `app` configuration on a device or emulator (API level per
   `app/build.gradle.kts`).
4. To test the widget, long-press the home screen, add the Stromschnelle
   widget, and confirm it reflects the same priority-sorted list as the app.

## Data retention

Completed to-dos are never deleted from the database. Completing a
to-do only stamps it with a `completedAt` timestamp; the row remains in
`TodoDao`/`TodoDatabase` permanently. The visible list simply filters out
to-dos completed longer ago than the configurable grace period (default
24 hours), so the active list stays uncluttered while the full history is
retained for later review or data mining. There is intentionally no
delete operation anywhere in `TodoDao` or `TodoRepository`.

## Project structure

```
stromschnelle/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/de/nereide/stromschnelle/
│           ├── StromschnelleApp.kt        # Application, holds AppContainer
│           ├── AppContainer.kt            # service locator (DI)
│           ├── data/
│           │   ├── Todo.kt                # @Entity
│           │   ├── TodoIcon.kt            # icon enum
│           │   ├── TodoDao.kt             # @Dao, Flow-based queries
│           │   ├── TodoDatabase.kt        # @Database (Room)
│           │   └── SettingsRepository.kt  # DataStore-backed settings
│           ├── domain/
│           │   ├── TodoRepository.kt          # repository interface
│           │   └── DefaultTodoRepository.kt   # default implementation
│           ├── ui/                        # Compose screens & ViewModels
│           └── widget/                    # Glance widget + receiver
├── docs/                                  # design notes / specs
├── build.gradle.kts
├── settings.gradle.kts
└── LICENSE
```

(Some directories above, such as `ui/` and `widget/`, are populated by
other parts of the app and may not all exist yet at any given point in
development.)

## License

MIT — see [LICENSE](LICENSE).
