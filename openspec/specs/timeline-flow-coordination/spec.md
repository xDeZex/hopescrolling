## ADDED Requirements

### Requirement: Timeline state is assembled atomically from both flows
`TimelineViewModel` SHALL derive `uiState` by combining the article load state and the read-state flow using `combine()`, so that every emission of `uiState` reflects a consistent snapshot of both sources.

#### Scenario: Both flows emit concurrently
- **WHEN** the article load state updates while read-state is also updating
- **THEN** `uiState` SHALL only emit states where articles and readIds are from the same combined snapshot — never a partial mix of old and new values

#### Scenario: Initial state before any fetch
- **WHEN** `TimelineViewModel` is constructed
- **THEN** `uiState` SHALL immediately emit `TimelineUiState(isLoading = true, articles = emptyList(), readIds = emptySet())`

### Requirement: Refresh triggers a new article fetch without disrupting read-state observation
`TimelineViewModel.refresh()` SHALL cancel any in-progress article fetch, set `isLoading = true`, and re-fetch articles — while the read-state flow continues uninterrupted.

#### Scenario: Refresh while read-state has items
- **WHEN** `readIds` contains one or more IDs and `refresh()` is called
- **THEN** `uiState` during loading SHALL still reflect the current `readIds` (not reset to empty)

#### Scenario: Successful refresh
- **WHEN** `refresh()` completes successfully
- **THEN** `uiState` SHALL emit `isLoading = false` with the newly fetched articles and the current `readIds`

### Requirement: Article fetch errors are surfaced without affecting read-state
When `ArticleRepository.getArticles()` throws, `TimelineViewModel` SHALL update `uiState.error` and set `isLoading = false`, while `readIds` retains its current value.

#### Scenario: Repository throws during refresh
- **WHEN** `ArticleRepository.getArticles()` throws a `RuntimeException`
- **THEN** `uiState` SHALL emit `isLoading = false`, `error` set to the exception message, and `readIds` unchanged

### Requirement: Marking an article read updates uiState.readIds
Calling `markRead(articleId)` SHALL persist the ID via `ReadStateRepository` and the combined flow SHALL propagate the updated `readIds` into the next `uiState` emission.

#### Scenario: Mark a single article read
- **WHEN** `markRead("some-id")` is called
- **THEN** `uiState.readIds` SHALL contain `"some-id"` in the next emission
