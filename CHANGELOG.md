# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

[Unreleased]: https://github.com/dogeared/dopamenu-deepseek/compare/v0.0.2...HEAD
[0.0.2]: https://github.com/dogeared/dopamenu-deepseek/releases/tag/v0.0.2
[0.0.1]: https://github.com/dogeared/dopamenu-deepseek/releases/tag/v0.0.1
