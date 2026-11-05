# Deploying to the Synology NAS

This is the runbook for the manual deploy of `dopamenu` as a Container Manager
project on the Synology DS2422+ (`192.168.68.112`). It complements
[`deploy.md`](deploy.md), which is the contract between this repository and
`dogeared-central-infra`. This file is the sequence of commands, including the
two host quirks that cost time the first time.

The automated pipeline will replace steps 1 to 6. Until then, a manual deploy
means build on the Mac, load the image on the NAS, and let Container Manager
run it.

## What the NAS already provides

| Thing | Value |
| --- | --- |
| Host | `192.168.68.112`, SSH user `dogeared` |
| SSH key | `~/.ssh/id_rsa_synology` |
| Architecture | `linux/amd64` (AMD Ryzen V1500B) |
| Keycloak project | `/volume1/docker/keycloak-central`, container `keycloak-central`, LAN port `8180` |
| Shared network | `nas-shared`, an external bridge network owned by no project |
| Keycloak DNS name | `keycloak` on `nas-shared` (alias for `keycloak-central`) |
| Tunnel | Cloudflare tunnel `nas`, connector `cloudflared-nas`, also on `nas-shared` |
| App project | `/volume1/docker/dopamenu`, container `dopamenu-app` |
| Public hostnames | `dopamenu.dogeared.dev` and `auth.dogeared.dev` |

## Two host quirks, up front

Both of these look like network failures and are not.

| Symptom | Cause | Fix |
| --- | --- | --- |
| `subsystem request failed on channel 0` from `scp` | Synology leaves the SFTP subsystem disabled, and OpenSSH 9+ makes `scp` use it by default | Add `-O` to use the legacy SCP transport, or pipe the file over plain SSH |
| `sudo: docker: command not found` | `sudo` resets `PATH` to a `secure_path` that omits `/usr/local/bin`, where Container Manager installs the Docker CLI | Call `/usr/local/bin/docker` explicitly, or use `sudo env PATH=$PATH docker` |

The `WARNING: connection is not using a post-quantum key exchange algorithm`
line is harmless and appears on every connection to this host.

---

## 1. Build the image for `linux/amd64`

A Mac on Apple Silicon builds `linux/arm64` by default. That image loads on the
NAS but fails at start with an exec-format error, so the platform flag is not
optional.

```bash
cd ~/Projects/dopamenu-deepseek

docker buildx build --platform linux/amd64 --load -t dopamenu:0.1.5 .
# without buildx: docker build --platform linux/amd64 -t dopamenu:0.1.5 .

docker image inspect dopamenu:0.1.5 --format '{{.Os}}/{{.Architecture}}'
```

The inspect must print `linux/amd64`. The Maven stage runs under emulation, so
expect several minutes on the first build.

Your local development stack is unaffected. It runs the image that
`docker compose build` produces (`dopamenu-app`), not this tag.

## 2. Copy the image to the NAS

```bash
docker save dopamenu:0.1.5 | gzip > /tmp/dopamenu-0.1.5-amd64.tar.gz
ls -lh /tmp/dopamenu-0.1.5-amd64.tar.gz    # roughly 150 to 200 MB

ssh -i ~/.ssh/id_rsa_synology dogeared@192.168.68.112 \
  'mkdir -p /volume1/docker/dopamenu'
```

Then transfer it. Use the `-O` flag because the SFTP subsystem is off:

```bash
scp -O -i ~/.ssh/id_rsa_synology /tmp/dopamenu-0.1.5-amd64.tar.gz \
  dogeared@192.168.68.112:/volume1/docker/dopamenu/
```

If `-O` also fails, pipe the file over plain SSH, which needs no SFTP at all:

```bash
ssh -i ~/.ssh/id_rsa_synology dogeared@192.168.68.112 \
  'cat > /volume1/docker/dopamenu/dopamenu-0.1.5-amd64.tar.gz' \
  < /tmp/dopamenu-0.1.5-amd64.tar.gz
```

Check the transfer before loading, because a truncated tarball fails later with
an unrelated-looking error:

```bash
shasum -a 256 /tmp/dopamenu-0.1.5-amd64.tar.gz

ssh -i ~/.ssh/id_rsa_synology dogeared@192.168.68.112 \
  'sha256sum /volume1/docker/dopamenu/dopamenu-0.1.5-amd64.tar.gz'
```

The two hashes must match.

## 3. Load the image and check the network

```bash
ssh -t -i ~/.ssh/id_rsa_synology dogeared@192.168.68.112 \
  'sudo /usr/local/bin/docker load -i /volume1/docker/dopamenu/dopamenu-0.1.5-amd64.tar.gz'
```

Verify the image and the shared network in one pass:

```bash
ssh -t -i ~/.ssh/id_rsa_synology dogeared@192.168.68.112 \
  'sudo /usr/local/bin/docker image inspect dopamenu:0.1.5 --format "{{.Os}}/{{.Architecture}}"
   sudo /usr/local/bin/docker network ls | grep nas-shared'
```

Expect `linux/amd64` and a `nas-shared` row. If the network is missing, create
it in Container Manager under **Network**, as a bridge named `nas-shared`,
before continuing. It is shared with `keycloak-central` and must not belong to
either project.

Also check which Compose you have, because DSM ships the standalone binary:

```bash
ssh -i ~/.ssh/id_rsa_synology dogeared@192.168.68.112 \
  'ls -l /usr/local/bin/docker*; /usr/local/bin/docker-compose version 2>/dev/null | head -2'
```

The rest of this file writes `docker compose`. If the plugin is missing, use
`/usr/local/bin/docker-compose` instead.

## 4. Import the realm and install the login theme

The NAS Keycloak runs with `command: start` and no `--import-realm`, so dropping
a file into `import/` does nothing. Import against the running container.

Copy the theme. The production realm sets `loginTheme: dopamenu`, and Keycloak
falls back to the stock theme when the directory is absent.

```bash
cd ~/Projects/dopamenu-deepseek

tar -C keycloak/themes -czf - dopamenu \
  | ssh -i ~/.ssh/id_rsa_synology dogeared@192.168.68.112 \
      'mkdir -p /volume1/docker/keycloak-central/themes && tar -C /volume1/docker/keycloak-central/themes -xzf -'

ssh -i ~/.ssh/id_rsa_synology dogeared@192.168.68.112 \
  'cat > /tmp/dopamenu-realm.json' < keycloak/realm-dopamenu.production.json
```

Mount the theme by adding one line to the `keycloak` service's `volumes:` in
`/volume1/docker/keycloak-central/compose.yaml`, next to the other theme mounts:

```yaml
      - ./themes/dopamenu:/opt/keycloak/themes/dopamenu:ro
```

Then import the realm. The admin password is `KEYCLOAK_ADMIN_PASSWORD` in
`/volume1/docker/keycloak-central/.env`.

```bash
ssh -t -i ~/.ssh/id_rsa_synology dogeared@192.168.68.112

sudo -i
docker exec keycloak-central /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 --realm master --user admin --password '<password>'

docker cp /tmp/dopamenu-realm.json keycloak-central:/tmp/dopamenu-realm.json
docker exec keycloak-central /opt/keycloak/bin/kcadm.sh create realms \
  -f /tmp/dopamenu-realm.json

docker exec keycloak-central /opt/keycloak/bin/kcadm.sh get realms --fields realm
```

Keycloak does not overwrite an existing realm. If an earlier trial left one
behind, delete it first and repeat the create:

```bash
docker exec keycloak-central /opt/keycloak/bin/kcadm.sh delete realms/dopamenu
```

Recreate Keycloak so it picks up the new theme mount:

```bash
cd /volume1/docker/keycloak-central
docker compose up -d keycloak
```

That recreates only the Keycloak container. Its Postgres volume and every other
realm are untouched.

## 5. Create the project files

Create `/volume1/docker/dopamenu/compose.yaml` from the block below, either over
SSH with an editor or with File Station.

```yaml
name: dopamenu

services:
  db:
    image: postgres:17-alpine
    container_name: dopamenu-db
    restart: unless-stopped
    environment:
      POSTGRES_DB: dopamenu
      POSTGRES_USER: dopamenu
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - ./postgres:/var/lib/postgresql/data
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U dopamenu -d dopamenu']
      interval: 5s
      timeout: 3s
      retries: 30

  app:
    image: dopamenu:0.1.5
    container_name: dopamenu-app
    restart: unless-stopped
    depends_on:
      db:
        condition: service_healthy
    environment:
      DB_URL: jdbc:postgresql://db:5432/dopamenu
      DB_USER: dopamenu
      DB_PASSWORD: ${DB_PASSWORD}
      KEYCLOAK_PUBLIC_URL: https://auth.dogeared.dev
      KEYCLOAK_INTERNAL_URL: http://keycloak:8080
      KEYCLOAK_REALM: dopamenu
      KEYCLOAK_CLIENT_ID: dopamenu-app
    networks:
      - default
      - nas-shared
    healthcheck:
      test: ['CMD', 'curl', '-fsS', 'http://localhost:8080/actuator/health']
      interval: 10s
      timeout: 4s
      start_period: 60s
      retries: 5

networks:
  nas-shared:
    external: true
```

There are no published ports. The tunnel reaches `dopamenu-app:8080` over
`nas-shared`, and the app reaches Keycloak as `keycloak:8080` on the same
network. `KEYCLOAK_PUBLIC_URL` is the browser-facing issuer and must match the
realm's Frontend URL. `KEYCLOAK_INTERNAL_URL` is the back-channel used for the
token exchange and JWKS. There is no client secret, because the client is public
and PKCE protects the exchange.

Create `/volume1/docker/dopamenu/.env` with a real password, generated locally:

```bash
openssl rand -hex 24
```

```
DB_PASSWORD=<the generated value>
```

Put both files in place. Creating them on the NAS with an editor is fine too, or
copy them from wherever you wrote them locally:

```bash
scp -O -i ~/.ssh/id_rsa_synology /path/to/compose.yaml /path/to/.env \
  dogeared@192.168.68.112:/volume1/docker/dopamenu/
```

## 6. Create the Container Manager project

In DSM, open **Container Manager**, then **Project**, then **Create**.

| Field | Value |
| --- | --- |
| Project name | `dopamenu` |
| Path | `/volume1/docker/dopamenu` |
| Source | Use the existing compose file |

Container Manager runs `docker compose up -d`. Because `dopamenu:0.1.5` is
already in the local image store it does not pull anything. If it reports a pull
failure, the tag is missing and step 3 did not finish.

Some Container Manager versions only detect a file named `docker-compose.yml`
when you choose the existing-file option. Keep `compose.yaml`, because
`nas-deploy.sh` looks for that name, and add a link if the picker does not see
it:

```bash
cd /volume1/docker/dopamenu
ln -s compose.yaml docker-compose.yml
```

Confirm from the shell:

```bash
sudo -i
cd /volume1/docker/dopamenu
docker compose ps
docker exec dopamenu-app curl -fsS http://localhost:8080/actuator/health
```

The health endpoint returns `{"status":"UP"}`, and Flyway has already created the
schema on first boot.

## 7. Route the tunnel

In the Cloudflare dashboard, open **Networking**, then **Tunnels**, then the
`nas` tunnel, then **Public Hostname**, then add a hostname.

| Field | Value |
| --- | --- |
| Subdomain | `dopamenu` |
| Domain | `dogeared.dev` |
| Type | `HTTP` |
| URL | `dopamenu-app:8080` |

The hostname must be on the `nas` tunnel, not on the main `dogeared` tunnel,
or two connectors will compete for it. The connector already sits on
`nas-shared`, so the bare container name resolves.

## 8. Verify

From outside the LAN:

```bash
curl -sI https://dopamenu.dogeared.dev/                 # 200
curl -s  https://dopamenu.dogeared.dev/actuator/health  # {"status":"UP"}
curl -sI https://dopamenu.dogeared.dev/menu             # 302 to https://auth.dogeared.dev/realms/dopamenu/...
```

Then open `https://dopamenu.dogeared.dev/menu` in a browser. The redirect should
land on `auth.dogeared.dev` with the `dopamenu` login theme.

Self-service signup is not usable yet, because the realm sets `verifyEmail` and
SMTP is unconfigured. Create a user in the `dopamenu` realm in the admin console,
tick **Email verified**, and log in to prove the round trip.

## 9. Configure email

In the Keycloak admin console, select the `dopamenu` realm, open **Realm
settings**, then **Email**. Fill in host, port, credentials and a From address,
then use **Test connection**.

Walk a real signup through the mailed verification link before announcing the
site. Registration fails closed until mail works, so a broken SMTP setting looks
like a broken registration form.

## 10. Clean up

```bash
rm /tmp/dopamenu-0.1.5-amd64.tar.gz

ssh -t -i ~/.ssh/id_rsa_synology dogeared@192.168.68.112 \
  'rm /volume1/docker/dopamenu/dopamenu-0.1.5-amd64.tar.gz /tmp/dopamenu-realm.json'
```

Keep the loaded image until the deploy has been up for a day. It is the rollback
target.

## Upgrading and rolling back

The compose file pins an exact tag, so an upgrade is a new build, a new load, a
one-line edit, and a restart.

```bash
# on the Mac
docker buildx build --platform linux/amd64 --load -t dopamenu:0.1.6 .
docker save dopamenu:0.1.6 | gzip > /tmp/dopamenu-0.1.6-amd64.tar.gz
scp -O -i ~/.ssh/id_rsa_synology /tmp/dopamenu-0.1.6-amd64.tar.gz \
  dogeared@192.168.68.112:/volume1/docker/dopamenu/

# on the NAS
sudo -i
cd /volume1/docker/dopamenu
docker load -i dopamenu-0.1.6-amd64.tar.gz
sed -i 's|image: dopamenu:0.1.5|image: dopamenu:0.1.6|' compose.yaml
docker compose up -d app
```

To roll back, point the tag at the previous image, which is still loaded, and
restart the same way.

When the pipeline lands, this whole runbook collapses. The image becomes
`ghcr.io/dogeared/dopamenu:latest`, and `deploy/nas-deploy.sh` in
`dogeared-central-infra` pulls it on its timer once `dopamenu` is added to its
`PROJECTS` list.

## Troubleshooting

| Symptom | Likely cause |
| --- | --- |
| `exec format error` on start | Image is `linux/arm64`; rebuild with `--platform linux/amd64` |
| Container Manager cannot pull the image | Tag never loaded; rerun `docker load` and check `docker images` |
| App returns 500 on login, logs show `invalid_client` | Realm client is confidential, or the app still has an old `KEYCLOAK_CLIENT_SECRET` |
| Login page is unstyled | Theme directory not mounted, or Keycloak was not recreated after the mount |
| Redirect lands on `localhost` or a container hostname | `KEYCLOAK_PUBLIC_URL` does not match the realm's Frontend URL |
| `connection refused` from the app to Keycloak | App is not on `nas-shared`, or the `keycloak` alias is missing |
| `subsystem request failed on channel 0` | Use `scp -O`, or pipe with `ssh 'cat > file'` |
| `sudo: docker: command not found` | Use `/usr/local/bin/docker`, or `sudo env PATH=$PATH docker` |
