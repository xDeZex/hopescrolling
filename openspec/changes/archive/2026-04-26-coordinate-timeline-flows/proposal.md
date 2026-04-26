## Why

`TimelineViewModel` collects from two independent flows with no coordination between them — articles and read-state update shared `MutableStateFlow` state via separate coroutine jobs, creating a window of inconsistency during concurrent updates. Using `combine()` makes the merge explicit and atomic, and simplifies test setup by eliminating the need to sequence two separate collection jobs.

## What Changes

- Replace the dual-coroutine init pattern in `TimelineViewModel` with a single `combine()` on both flows
- `TimelineUiState` is assembled in one place from a unified combined flow
- `refresh()` triggers a re-fetch of articles; the combined flow ensures read-state is always in sync
- Tests can use a single coroutine scope / turbine collector instead of coordinating two separate jobs

## Capabilities

### New Capabilities

- `timeline-flow-coordination`: Atomic merging of article and read-state flows into `TimelineUiState` via `combine()`

### Modified Capabilities

<!-- none -->

## Impact

- `TimelineViewModel` — init block, `refresh()`, internal state management
- `TimelineUiState` — no structural change, but it is now populated atomically
- Tests for `TimelineViewModel` — simplified setup; concurrent update scenarios can now be tested reliably
