## Context

`TimelineViewModel` currently manages two independent coroutine jobs in `init`: one that collects `readStateRepository.getReadIds()` and one launched by `refresh()` that calls `articleRepository.getArticles()`. Both jobs write to the same `MutableStateFlow<TimelineUiState>` via `update {}`, creating a window where articles and read-state are briefly out of sync. Test setup must carefully order coroutine execution to avoid flakiness.

`ArticleRepository.getArticles()` is a one-shot `suspend fun`, not a `Flow`. `ReadStateRepository.getReadIds()` is a `Flow<Set<String>>`.

## Goals / Non-Goals

**Goals:**
- Merge article and read-state into `TimelineUiState` atomically via `combine()`
- Eliminate the temporal coupling between the two coroutine jobs
- Simplify test setup — a single flow collector is enough to observe all state transitions
- Preserve the `refresh()` trigger model (on-demand fetch, not continuous polling)

**Non-Goals:**
- Changing `ArticleRepository` to return a `Flow<List<Article>>` — that is a separate interface change with broader impact
- Supporting optimistic or incremental article updates
- Any change to `ReadStateRepository`

## Decisions

### Internal `MutableStateFlow` for articles load state

Since `ArticleRepository.getArticles()` is a suspend function, it cannot be directly passed to `combine()`. Instead, introduce a private `_articlesState: MutableStateFlow<ArticlesLoadState>` where `ArticlesLoadState` is a sealed class (or simple data class) holding `articles`, `isLoading`, and `error`.

`refresh()` updates `_articlesState` as it fetches. The public `uiState` is derived by `combine(_articlesState, readStateRepository.getReadIds())` and exposed via `stateIn(SharingStarted.Eagerly)`.

**Alternative considered:** Wrapping each `getArticles()` call in `flow { emit(...) }` and re-subscribing on refresh. Rejected: requires managing subscription lifecycle explicitly and leaks the implementation detail that articles are one-shot.

**Alternative considered:** Changing `ArticleRepository` to `Flow<List<Article>>`. Rejected: out of scope for this change; it would require updating `DefaultArticleRepository`, all its dependencies, and existing tests.

### `stateIn` with `SharingStarted.Eagerly`

The combined flow is turned into a `StateFlow` via `stateIn(viewModelScope, SharingStarted.Eagerly, TimelineUiState())`. `Eagerly` matches the existing behavior (collection starts in `init`, not lazily on first subscriber).

### No `ArticlesLoadState` data class exposed publicly

`ArticlesLoadState` is an internal implementation detail of the ViewModel; it is not part of `TimelineUiState` or any public API.

## Risks / Trade-offs

- [Extra intermediate state object] → Kept private and minimal; does not leak to UI layer
- [Initial `isLoading = true` must still be the first emitted state] → `stateIn` initial value is `TimelineUiState()` which has `isLoading = true` by default — no change in behavior
- [Tests that use `uiState.first()` directly] → `stateIn(Eagerly)` ensures the StateFlow is hot from construction; existing test patterns continue to work
