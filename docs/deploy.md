# Deploying the public site

This repository ships an application image, a Keycloak realm export and the
database migrations. It does **not** own the public ingress.

Since 2026-11-04 the Cloudflare tunnel and the central Keycloak live in
`~/Projects/dogeared-central-infra` (one compose project, `central`, on the
`tnra-shared` Docker network). The earlier plan in
`docs/designs/public-menu-share-link.md` for a `deploy/public/` Caddy stack is
superseded: a second Caddy and a second Keycloak would duplicate what that repo
already runs. This file is the contract between the two repos.

## What this repo provides

| Artifact | Path / name | Notes |
| --- | --- | --- |
| Application image | `Dockerfile` at the repository root | Listens on `8080`; health at `/actuator/health` |
| Local stack | `compose.yaml` (`db` + `app`) | Loopback ports, for development only |
| Realm export | `keycloak/realm-dopamenu.json` | Realm `dopamenu`, client `dopamenu-app` |
| Login theme | `keycloak/themes/dopamenu/` | Mount into whichever Keycloak serves the realm |
| Schema | `src/main/resources/db/migration/` | Flyway runs on boot; no manual step |

## What central-infra must provide

1. **A tunnel route** for the app hostname (the design uses
   `dopamenu.dogeared.dev`) to `http://dopamenu-app:8080` on `tnra-shared`.
   The Cloudflare hostname is proxied by the tunnel, so TLS is the edge's job;
   no certificate or Caddy block is needed here.
2. **A `dopamenu` realm** imported from `keycloak/realm-dopamenu.json`, served
   by the central Keycloak. Because the Keycloak hostname is shared with the
   other tenants, the realm export must be re-imported with the public hostnames
   added to the client:
   - `redirectUris`: `https://dopamenu.dogeared.dev/*`
   - `webOrigins`: `https://dopamenu.dogeared.dev`
   - `post.logout.redirect.uris`: `https://dopamenu.dogeared.dev/*`

   That export is a **development** realm, not a production one. Before (or as
   part of) the import, apply these realm-hygiene steps. Skipping them hands out
   a known credential and a weakened transport policy on the first deploy:

   - **Remove the bundled user.** `realm-dopamenu.json` ships an enabled
     `demo` account with password `demo`. Delete the `users` entry before
     import (or delete the user in the console) — otherwise anyone with this
     repository can sign in to the public site as `demo`.
   - **Rotate the client secret.** The export hard-codes
     `dopamenu-secret` for `dopamenu-app`, which is also the application's
     documented default. Generate a new secret in Keycloak and set it only via
     `KEYCLOAK_CLIENT_SECRET` in the app service; never commit the production
     value.
   - **Re-enable transport security.** The export sets `sslRequired: none` for
     local HTTP. Set it to `external` so the realm requires TLS for all
     non-loopback clients.
   - **Remove the development redirect URIs.** The export's `redirectUris`
     include `http://localhost:8080/*`, `http://127.0.0.1:8080/*`,
     `http://localhost:8083/*` and the dev-proxy host. The step above says
     *add* the public hostnames; also *remove* the localhost and
     `*.dev.dogeared.dev` entries so a public client cannot be driven from a
     developer machine.
3. **The app service**, built from this repo, joined to `tnra-shared`, with:

   ```yaml
   environment:
     DB_URL: jdbc:postgresql://<host>:5432/dopamenu
     DB_USER: dopamenu
     DB_PASSWORD: <secret>
     KEYCLOAK_PUBLIC_URL: https://auth.<central-domain>
     KEYCLOAK_INTERNAL_URL: http://keycloak:8080
     KEYCLOAK_REALM: dopamenu
     KEYCLOAK_CLIENT_ID: dopamenu-app
     KEYCLOAK_CLIENT_SECRET: <the realm client's secret>
   ```

   `KEYCLOAK_PUBLIC_URL` must be the URL the browser uses, because it is the
   token issuer. `KEYCLOAK_INTERNAL_URL` is the container-to-container URL used
   for token exchange and JWKS.

4. **A database.** central-infra runs no shared Postgres, so the app keeps its
   own `postgres:17-alpine` service with a named volume, exactly as the root
   `compose.yaml` does.

### The admin path

The design asked for `/admin*` and `/realms/master*` to be denied publicly.
That is satisfied structurally, not by a rule: the app hostname routes only to
this application, which has no `/admin` or `/realms` handler. Keycloak is
reached at its own hostname, where the operator's existing admin console
protection already applies. Nothing extra needs to be configured for
`dopamenu.dogeared.dev`.

## Smoke test after the first deploy

Run these from outside the LAN:

```bash
curl -sI https://dopamenu.dogeared.dev/                     # 200, no X-Robots-Tag
curl -sI https://dopamenu.dogeared.dev/m/<token>            # 200, X-Robots-Tag: noindex
curl -sI https://dopamenu.dogeared.dev/menu                 # 302 to Keycloak
curl -s  https://dopamenu.dogeared.dev/actuator/health      # {"status":"UP"}
```

Then, in a private window on cellular data:

1. Open the share link with no account. The menu renders; four courses appear.
2. Tap **Order** on one dish, register through Keycloak, confirm a mood.
   The dish is copied into the new account and the order is running.
3. Sign out, open the link again, and press **Copy this menu**. The account's
   menu fills in one click; pressing it again changes nothing.
4. Rotate the link in **Manage**; the old token shows the dead page immediately.
5. Confirm the funnel in the database:

   ```sql
   select kind, count(*) from menu_event where token = '<token>' group by kind;
   ```

   `share_view`, `menu_copied`, `order_started` and `order_completed` should all
   be present for the tested token.

The four-week experiment clock starts when the link is posted into the named
channel, not when this deploy succeeds.
