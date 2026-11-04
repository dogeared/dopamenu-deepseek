# Dev proxy (Caddy + wildcard TLS)

Shared HTTPS front door for dev containers on this machine. `.dev` is on the
browser HSTS preload list, so `*.dev.dogeared.dev` can't be served over plain HTTP.

## One-time setup

1. DNS: `*.dev.dogeared.dev` must resolve to this machine's LAN IP. Either add a
   Cloudflare `A` record `*.dev` → the LAN IP set to **DNS only** (grey cloud; it exposes the
   private IP in public DNS but nothing is reachable through it), or, to publish nothing, point
   the names at the LAN IP in your router / Pi-hole / local resolver instead (split DNS). The cert
   is issued by DNS challenge either way, so no inbound access from the internet is needed.
2. Create a Cloudflare API token (My Profile → API Tokens) with
   `Zone:Zone:Read` and `Zone:DNS:Edit`, limited to the `dogeared.dev` zone.
3. Set `BIND_IP` in `.env` to the machine's LAN IP, and do **not** port-forward 80/443 on
   your router. Caddy also returns 403 to any client outside private address ranges.
4. On the machine:

   ```bash
   docker network create proxy
   cp .env.example .env      # paste the token, set BIND_IP
   docker compose up -d --build
   docker compose logs -f caddy   # wait for "certificate obtained successfully"
   ```

`restart: unless-stopped` brings it back after reboots; the cert lives in the
`caddy-data` volume and renews automatically.

## Adding a project

Put `sites/<project>.caddy` next to `dopamenu.caddy`, attach the project's containers
to the external `proxy` network, then `docker compose exec caddy caddy reload --config /etc/caddy/Caddyfile`.
