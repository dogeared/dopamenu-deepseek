# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-10-27

### Added

- Public landing page at `/` that explains what a dopa-menu is, links to the
  original How to ADHD video, and thanks the author. It stays reachable after
  signing in and includes a login button for signed-out visitors.
- A persistent footer on every page: “made with ❤️ by dogeared”, linking to the
  GitHub repository.

### Changed

- The menu now lives at `/menu`; successful logins land there.
- `Home` is part of the signed-in navigation, and the brand links to the landing
  page.

## [0.0.3] - 2026-10-27

### Added

- Unit tests for the domain model, services, current-user resolution, and
  Keycloak configuration.
- UI tests that drive the Vaadin component tree: menu, history, manage-menu,
  order flow, form validation, layout, mood picker, cards, and course sections.
- Line coverage now exceeds 99% of measured code.

### Changed

- Enforce a 90% line-coverage gate in the build. Only framework wiring
  (`SecurityConfig`, `AppShell`, `DopaMenuApplication`) is excluded from the
  coverage report.

### Fixed

- Resuming an already-running order now shows that order's title and course
  instead of the item the user tried to start.

## [0.0.2] - 2026-10-27

### Fixed

- Prevent the push and merged-pull-request workflow runs from cancelling each
  other, which left the build badge red and the coverage badge unpublished.

## [0.0.1] - 2026-10-27

### Added

- Initial release of Dopa Menu: courses, order flow, history, menu management,
  per-user data, and Keycloak authentication.
- GitHub Actions CI workflow that runs the test suite on every push and on merged
  pull requests.
- Automatic `v<version>` tag creation when a pull request is merged, using the
  version from `pom.xml` (or an explicit `release x.y.z` in the merge commit).
- JaCoCo coverage reporting with an automatically published coverage badge.
- Version, build status, and coverage badges in the README.

### Changed

- Set the project version to `0.0.1`.

[Unreleased]: https://github.com/dogeared/dopamenu-deepseek/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/dogeared/dopamenu-deepseek/releases/tag/v0.1.0
[0.0.3]: https://github.com/dogeared/dopamenu-deepseek/releases/tag/v0.0.3
[0.0.2]: https://github.com/dogeared/dopamenu-deepseek/releases/tag/v0.0.2
[0.0.1]: https://github.com/dogeared/dopamenu-deepseek/releases/tag/v0.0.1
