# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.1] - 2026-11-06

### Added

- **Version in the footer.** Every page footer now shows the running application
  version (for example `v0.3.1`), read from the build metadata.

## [0.3.0] - 2026-11-06

### Changed

- **Stay signed in for 30 days.** Once you authenticate, the app session and
  the Keycloak SSO session now stay alive for up to 30 days instead of expiring
  after 30 minutes idle, so you do not have to sign in again on every visit.

## [0.2.0] - 2026-11-05

### Added

- **Ordering now follows the dish's timer setting.** Without **Pair with a
  timer**, **Order** opens a short form for the minutes you actually spent,
  with the before/after moods and an optional note, and writes the entry
  straight to History without starting a countdown. With the timer paired,
  **Order** starts the countdown as before.
- **Correct the clock when you finish.** The completion screen of a timed order
  now has an editable **Minutes spent** field, prefilled from the timer, so an
  interruption or a longer session is recorded as it really was.
- Timer dishes still offer **Log it** for something you already finished. That
  writes a completed entry without touching a running timer.
- Entries logged without a timer carry a **Logged** pill in History and show
  the reported minutes, so they read differently from timed orders.

## [0.1.6] - 2026-11-05

### Added

- A **Sign out** pill in the header, beside the signed-in user's name, on every
  signed-in page. The menu and history pages had the control only at the bottom
  of the drawer, while the public site already showed it next to the name.

### Fixed

- **Create account** now opens the registration form directly. Both entry points
  (the landing-page hero and the public nav) still link to `/register`, but that
  route now targets Keycloak's `protocol/openid-connect/registrations` endpoint.
  The previous `kc_action=register` hint is ignored by Keycloak 26, so the
  button landed on the login form and left the user to find the Register link.

## [0.1.5] - 2026-11-04

### Added

- `keycloak/realm-dopamenu.production.json`: a production realm export that can
  be imported directly instead of applying manual realm-hygiene steps. It has no
  users, a public client with no secret, `sslRequired: external`, only the public
  redirect URIs, the issuer pinned to `https://auth.dogeared.dev`, and email
  verification and forgot-password enabled.

### Changed

- The app is a public OAuth client using the authorization code flow with PKCE,
  and no client secret. `KEYCLOAK_CLIENT_SECRET` is gone from the environment and the config,
  both realm exports mark `dopamenu-app` as public, and the docs no longer ask
  for a secret.
- `docs/deploy.md` and the README now point at the production realm export; the
  development export `keycloak/realm-dopamenu.json` stays local-only.
- `docs/deploy-synology.md` is the manual deploy runbook for the NAS. It covers
  the `linux/amd64` rebuild, the image transfer, the realm and theme install, the
  Container Manager project, the tunnel route and the host's `scp` and `sudo`
  quirks.

## [0.1.4] - 2026-11-04

### Added

- Public, anonymous menu pages at `/m/{token}`: a shared menu renders without an
  account, marks itself `noindex`, and offers one-tap ordering plus "Copy this
  menu" into a real account.
- Share controls on the menu editor: create, copy, rotate and revoke one live
  link per owner, with the author's name shown on the public page.
- `/register` route and a "Create account" call to action on the public site;
  the sign-in request asks Keycloak to open registration instead of login.
- Token-attributed events (`share_view`, `order_started`, `order_completed`,
  `menu_copied`) so a single link's funnel can be read from the database.

### Changed

- The Keycloak realm allows self-registration and uses the `dopamenu` login
  theme, so someone arriving from a shared menu can create an account.
- `mvn verify` now runs one HTTP-level test against a Testcontainers Postgres,
  so it needs a working Docker daemon.
- The Docker dev stack listens on port 8083 and reaches Keycloak by container
  name, matching the shared local Keycloak and the dev reverse proxy. The
  bundled realm also admits `localhost:8083` and the dev-proxy host.
- The README now documents the production build and deploy path: the image,
  runtime environment, production realm and client settings, and the login
  theme. It also makes enabling Keycloak's email verification the launch
  requirement, since the production Keycloak already sends mail.

## [0.1.3] - 2026-11-04

### Added

- `docs/designs/public-menu-share-link.md`: the approved and eng-reviewed design
  for a public, anonymous menu share link with view, copy, start and completion
  attribution. Application behavior is unchanged in this release.

## [0.1.2] - 2026-10-27

### Changed

- The landing page now also thanks Eric Tivers, with a link to the ADHD reWired
  YouTube channel.
- Jessica McCabe's name on the landing page now links to the How to ADHD
  YouTube channel.

## [0.1.1] - 2026-10-27

### Added

- Keycloak login theme (`keycloak/themes/dopamenu`) that matches the app's
  look: wordmark, colours, fonts, inputs and pill buttons. It extends
  `keycloak.v2` and is mounted into the dev Keycloak container.

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

### Fixed

- The public login button points at `/login`, which forwards to the Keycloak
  authorization endpoint instead of producing a missing-route error.

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

[Unreleased]: https://github.com/dogeared/dopamenu-deepseek/compare/v0.1.2...HEAD
[0.1.2]: https://github.com/dogeared/dopamenu-deepseek/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/dogeared/dopamenu-deepseek/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/dogeared/dopamenu-deepseek/releases/tag/v0.1.0
[0.0.3]: https://github.com/dogeared/dopamenu-deepseek/releases/tag/v0.0.3
[0.0.2]: https://github.com/dogeared/dopamenu-deepseek/releases/tag/v0.0.2
[0.0.1]: https://github.com/dogeared/dopamenu-deepseek/releases/tag/v0.0.1
