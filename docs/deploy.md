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
| Local stack | `compose.yaml` (`db` + `app`) | Host-published ports, for development only |
| Production realm export | `keycloak/realm-dopamenu.production.json` | Import directly into the central Keycloak |
| Development realm export | `keycloak/realm-dopamenu.json` | Local only; contains the `demo` user and localhost URIs |
| Login theme | `keycloak/themes/dopamenu/` | Mount into whichever Keycloak serves the realm |
| Schema | `src/main/resources/db/migration/` | Flyway runs on boot; no manual step |

## What central-infra must provide

1. **A tunnel route** for the app hostname (the design uses
   `dopamenu.dogeared.dev`) to `http://dopamenu-app:8080` on `tnra-shared`.
   The Cloudflare hostname is proxied by the tunnel, so TLS is the edge's job;
   no certificate or Caddy block is needed here.
2. **A `dopamenu` realm** imported from
   `keycloak/realm-dopamenu.production.json`, served by the central Keycloak.
   That file is the production artifact; `keycloak/realm-dopamenu.json` remains
   the local development realm and is not imported here.

   Import it with the admin console (**Create realm**, then **Resource file**) or
   `kcadm create realms -f keycloak/realm-dopamenu.production.json`. If a
   `dopamenu` realm already exists from an earlier trial, delete it first:
   Keycloak does not overwrite an existing realm on import. It already carries
   the production settings:

   - Client `dopamenu-app` (public, standard flow, PKCE `S256`) with the
     exact redirect URI
     `https://dopamenu.dogeared.dev/login/oauth2/code/keycloak`, web origin
     `https://dopamenu.dogeared.dev`, and post-logout
     `https://dopamenu.dogeared.dev/*`.
   - Frontend URL `https://auth.dogeared.dev`, so the issuer is
     `https://auth.dogeared.dev/realms/dopamenu` for the browser and for the
     back-channel exchange through `KEYCLOAK_INTERNAL_URL`.
   - `sslRequired: external`, self-registration on, duplicate emails off,
     login with email on, email verification and forgot-password on.
   - No users and no client secret. The client is public, so the repository has
     nothing reusable to commit or rotate.

   The login theme must be mounted on the Keycloak that serves the realm
   (`keycloak/themes/dopamenu/`); without it the `dopamenu` login theme falls
   back to the stock theme.

   One value is intentionally not in the file, because it belongs to the host:

   - **SMTP.** Email settings are per realm. Fill in Host, Port, credentials and
     a From address under Realm settings, then Email, and use
     **Test connection**. Registration fails closed until mail works, so walk a
     real signup through the mailed verification link before launch.

3. **The app service**, built from this repo, joined to `tnra-shared`, with:

   ```yaml
   environment:
     DB_URL: jdbc:postgresql://<host>:5432/dopamenu
     DB_USER: dopamenu
     DB_PASSWORD: <secret>
     KEYCLOAK_PUBLIC_URL: https://auth.dogeared.dev
     KEYCLOAK_INTERNAL_URL: http://keycloak:8080
     KEYCLOAK_REALM: dopamenu
     KEYCLOAK_CLIENT_ID: dopamenu-app
   ```

   `KEYCLOAK_PUBLIC_URL` must be the URL the browser uses and must match the
   realm's Frontend URL, because it is the token issuer. `KEYCLOAK_INTERNAL_URL`
   is the container-to-container URL used for token exchange and JWKS. There is
   no client secret: `KEYCLOAK_CLIENT_ID` names the public client, and PKCE
   protects the authorization code exchange.

4. **A database.** central-infra runs no shared Postgres, so the app keeps its
   own `postgres:17-alpine` service with a named volume, exactly as the root
   `compose.yaml` does.

### The admin path

The design asked for `/admin*` and `/realms/master*` to be denied publicly.
That is satisfied structurally, not by a rule. The app hostname routes only to
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

   `share_view`, `menu_copied` and `order_started` should all be present for the
   tested token; `order_completed` appears once the order started in step 2 is
   completed.

The four-week experiment clock starts when the link is posted into the named
channel, not when this deploy succeeds.
