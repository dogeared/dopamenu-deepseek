# Dev proxy (Caddy + wildcard TLS)

Shared HTTPS front door for dev containers on this machine. `.dev` is on the
browser HSTS preload list, so `*.dev.dogeared.dev` can't be served over plain HTTP.

## One-time setup

1. DNS (Cloudflare): add an `A` record `*.dev` → this machine's LAN IP, set to
   **DNS only** (grey cloud). Cloudflare can't proxy a private address.
2. Create a Cloudflare API token (My Profile → API Tokens) with
   `Zone:Zone:Read` and `Zone:DNS:Edit`, limited to the `dogeared.dev` zone.
3. On the machine:

   ```bash
   docker network create proxy
   cp .env.example .env      # paste the token
   docker compose up -d --build
   docker compose logs -f caddy   # wait for "certificate obtained successfully"
   ```

`restart: unless-stopped` brings it back after reboots; the cert lives in the
`caddy-data` volume and renews automatically.

## Adding a project

Put `sites/<project>.caddy` next to `dopamenu.caddy`, attach the project's containers
to the external `proxy` network, then `docker compose exec caddy caddy reload --config /etc/caddy/Caddyfile`.
