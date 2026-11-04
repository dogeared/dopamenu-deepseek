# TODO

## Features

- [ ] **feature:** CRUD operations for menu sections — deferred by CEO review 2026-10-27 (D3.4). Sections are a hard-coded `Course` enum (`Course.java:7`) referenced in 13 files, 21 call sites; this is a data-model migration, not a UI task. Revisit only after a demand signal.
- [ ] **feature:** create printable dopa-menu from current configuration — deferred by CEO review 2026-10-27 (D3.3). A one-shot need is a weak subscription reason; build it as browser print CSS when it is free-tier work.
- [ ] **feature:** share read-only menu via unique share link — deferred by CEO review 2026-10-27 (D3.2) and planned as a FREE growth loop, not a paid feature. Cap free at one active link; charge later for unlimited or password-protected links.
- [ ] **feature:** integrate with Paddle for paid subscriptions — deferred by CEO review 2026-10-27 (D3.1). Reuse the tnra-billing service as a sidecar (1,766 main LOC, 96 tests, Paddle webhooks + entitlement gate); do not copy its classes. Blocked by a validated demand signal.
- [ ] **feature:** distinguish which capabilities are free and which are paid — deferred by CEO review 2026-10-27 (D3.1). The agreed line: the full loop and the existing 7-day stats stay free (D6); Plus sells 90-day patterns, best time of day, mood-lifting courses and a monthly reflection (D4).
- [ ] **feature:** webhook-based deposit confirmation — deferred by CEO review 2026-10-27 (D7). Manual weekly reconciliation is adequate under ~50 deposits; revisit when volume makes it slow.

## Bug fixes

- [ ] **bugfix:** fix logout route

## Chores

- [x] **chore:** update thanks on the homepage to include Eric Tivers
- [x] **chore:** create custom Keycloak login page to match styles with the app
- [ ] **chore:** harden insight inputs before any insight ships — explicit mood selection (remove the `3` default at `OrderDialog.java:61,76,120,227`), stop pre-filling the after-rating, store the user timezone (`UiText.java:46`), and define minimum samples plus the insufficient-data state. Prerequisite from CEO review 2026-10-27 (Codex #2). Deferred by eng review 2026-10-28 until an insight view exists; the definition part (minimum sample, insufficient-data state, delivery deadline) moves into `docs/experiments/monetization-validation.md`, and the timezone column waits for a consumer. Note the real file list: `MoodPicker` has no unselected state (`MoodPicker.java:22,46,53`) and `MenuOrderService.start` takes an `int` (`:36`).
- [ ] **chore:** guard the public `/plus` view counter against bots — from eng review 2026-10-28 (Performance P1). `/plus` is the only unauthenticated route that writes; a crawler or refresh loop inflates both the counter table and the 300-view denominator. Start at the counter repository call; the fix belongs at the edge or the filter chain, not in the view. Not needed at 300 views / 30 days.
- [ ] **chore:** add a date-bounded long-range order read path — from eng review 2026-10-28 (Architecture A3). `MenuOrderService.recentOrders` uses `findTop50ByOwnerSubOrderByStartedAtDesc` (`MenuOrderService.java:65`), so any in-app 90-day analysis would silently read only the newest 50 orders. The manual fulfilment query bypasses the cap; build this when an insight view actually consumes it.
