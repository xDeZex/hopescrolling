# hopescrolling

An Android RSS feed reader built with Kotlin and Jetpack Compose.

## Features

- OLED dark theme
- Add and manage RSS/Atom feed sources (planned)
- Unified timeline of articles across all feeds (planned)
- Mark articles as read (planned)

## Tech stack

- Kotlin + Jetpack Compose (MVVM)
- Room (read state persistence)
- DataStore (feed source storage)
- `java.net.HttpURLConnection` + XML parsing for RSS/Atom feeds

## Build

```bash
./gradlew :app:assembleDebug          # debug APK
./gradlew :app:testDebugUnitTest      # unit tests
./gradlew :app:connectedDebugAndroidTest  # instrumented tests (requires emulator/device)
```

Emulator: Pixel 7a (Android 15).
