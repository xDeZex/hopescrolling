# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## General rules

Never `git push` unless explicitly told to.

Each PR must have exactly one commit. All fixes and follow-ups go into the same commit via `git commit --amend`, not as new commits. If the branch contains multiple unrelated commits, first fetch and check whether any earlier PRs have already merged into `main` (`git fetch origin && git log origin/main..HEAD`) — the branch may look clean once main is up to date. Only if genuinely unrelated commits remain should you create a separate PR for each by cherry-picking onto a fresh branch from `main`.

When asked to "automerge": fetch origin, check `git log origin/main..HEAD` and open PRs (`gh pr list`) to understand the current state, then create a PR for the latest commit and enable automerge (`gh pr merge --auto --rebase`).

After every commit, launch a subagent to review it (`git show HEAD`) and report findings before proceeding. If the commit touches any amended files, re-run the review on the amended commit before considering the work done.

## Commands

`gradlew` is patched to fall back to Android Studio's JDK when `JAVA_HOME` is not set — no `JAVA_HOME` export needed locally. `org.gradle.java.home` is intentionally absent from `gradle.properties` so CI can use its own JDK.

```bash
./gradlew :app:assembleDebug                    # build debug APK
./gradlew :app:testDebugUnitTest                # JVM unit tests
./gradlew :app:connectedDebugAndroidTest        # instrumented tests (requires emulator/device)
./gradlew :app:testDebugUnitTest --tests "com.hopescrolling.ThemeColorTest"  # single test class
./gradlew :app:testDebugUnitTest -Pscreenshots --tests "com.hopescrolling.ScreenshotTest"  # screenshot tests (excluded from normal runs)
```

After writing or changing any Compose UI code, run `./gradlew :app:testDebugUnitTest -Pscreenshots --tests "com.hopescrolling.ScreenshotTest"` and use the Read tool to inspect the PNGs in `screenshots/` to visually verify the layout. The directory is wiped and regenerated on each run.

Emulator: `Pixel_7a` (Android 15). Launch app after installing:
```bash
./gradlew :app:installDebug
/c/Users/ollib/AppData/Local/Android/Sdk/platform-tools/adb shell am start -n com.hopescrolling/.MainActivity
```

## Architecture

Single-module Android app (`com.hopescrolling`), Kotlin + Jetpack Compose, MVVM.

**Entry point**: `MainActivity` → `HopescrollingApp()` → `HopescrollingTheme` → `AppNavigation()`

**Navigation** (`ui/navigation/AppNavigation.kt`): Two routes managed by a single `NavHost` inside a `Scaffold`. The top bar conditionally shows a settings icon (on Timeline) or a back arrow (on Feed Manager) based on the current back-stack route.

**Screens** (`ui/screens/`): `TimelineScreen` and `FeedManagerScreen` are currently placeholders. Future work adds ViewModels and content per the issues below.

**Theme** (`ui/theme/`): Single OLED dark theme only — `#000000` background (`OledBlack`), no light mode, no dynamic color. All palette values live in `Color.kt`; `Theme.kt` assembles them into a `darkColorScheme`.

**Testing convention**: Unit tests (`src/test/`) run on the JVM and test pure logic. Instrumented tests (`src/androidTest/`) use `createComposeRule()` and test behavior through Compose semantics/test tags. Test tags are the contract between UI and tests — don't remove them.

## Planned modules (not yet implemented)

See GitHub issues for implementation order:
- **#4** `RssParser` — HTTP fetch + RSS 2.0 / Atom XML parsing → article data objects
- **#5** `FeedSourceRepository` — DataStore-backed CRUD for feed source URLs/names
- **#6** `FeedManagerScreen` + `FeedManagerViewModel` — wired to FeedSourceRepository
- **#7** `ArticleRepository` — parallel fetch, merge, sort, deduplicate via RssParser + FeedSourceRepository
- **#8** `TimelineScreen` + `TimelineViewModel` + `ArticleCard` — full article list with loading/error states
- **#9** `ReadStateRepository` — Room DB tracking read article IDs, wired into TimelineViewModel
