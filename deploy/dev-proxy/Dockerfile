# Stock Caddy has no DNS-challenge providers; build one with the Cloudflare
# module so it can issue the *.dev.dogeared.dev wildcard (DNS-01) cert.
FROM caddy:builder AS build
RUN xcaddy build --with github.com/caddy-dns/cloudflare

FROM caddy:latest
COPY --from=build /usr/bin/caddy /usr/bin/caddy
