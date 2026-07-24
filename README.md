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

### From the command line (no IDE required)

Requirements:

- **JDK 17** (AGP 8.7 does not run on JDK 24/25). Point `JAVA_HOME` at a
  17 install, e.g. `export JAVA_HOME=/usr/lib/jvm/java-17-temurin-jdk`.
- **Android SDK** with `platforms;android-35` and `build-tools;35.0.0`.
  Either set `ANDROID_HOME`/`ANDROID_SDK_ROOT`, or create a `local.properties`
  file in the project root containing `sdk.dir=/path/to/Android/Sdk`
  (`local.properties` is git-ignored). Missing SDK packages are downloaded
  automatically on first build once licenses are accepted
  (`sdkmanager --licenses`).

Then build and test with the committed Gradle wrapper:

```bash
./gradlew assembleDebug        # produces app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # runs the repository unit tests
```

This build has been verified end-to-end from the CLI (APK assembled, unit
tests green) with JDK 17 + SDK 35.

### From Android Studio

1. Open the project root in Android Studio (Koala/Ladybug or newer) and let it
   sync Gradle. The Gradle wrapper (`gradle/wrapper/gradle-wrapper.jar`) is
   committed, so no bootstrapping step is needed.
2. Run the `app` configuration on a device or emulator (`minSdk 26`).
3. To test the widget, long-press the home screen, add the Stromschnelle
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
