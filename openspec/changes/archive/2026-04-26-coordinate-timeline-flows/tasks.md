## 1. Internal ArticlesLoadState

- [x] 1.1 [RED] Write failing test: uiState readIds is preserved during refresh (readIds non-empty while isLoading is true)
- [x] 1.2 [GREEN] Introduce private `ArticlesLoadState` data class and `_articlesState: MutableStateFlow<ArticlesLoadState>`; derive `uiState` via `combine(_articlesState, readStateRepository.getReadIds())` using `stateIn(Eagerly)`; update `refresh()` and remove old `_uiState` writes

## 2. Atomic state assembly

- [x] 2.1 [RED] Write failing test: articles and readIds are never observed in inconsistent state — emit multiple readIds values via FakeReadStateRepository while articles are loading; assert each collected uiState has internally consistent articles + readIds
- [x] 2.2 [GREEN] Minimal adjustments to combine() wiring to pass

## 3. Error handling preserves readIds

- [x] 3.1 [RED] Write failing test: when repository throws, uiState has error set and readIds unchanged
- [x] 3.2 [GREEN] Ensure failure branch in refresh() writes to `_articlesState` (not a separate state flow) so readIds from combine() is always current
