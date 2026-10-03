# Install, back up, upgrade

*[Version française](../fr/install.md)*

## What you need

- A computer or server with **Docker** (Docker Desktop on Windows or macOS, Docker Engine on Linux).
- **4 GB of RAM** to build the image the first time. To run it, OrbitOS needs about **2 GB**.
- About 5 GB of disk space.

## Install (one command)

```bash
git clone <your copy of the OrbitOS repository> orbitos
cd orbitos
docker compose up -d
```

The first time, Docker builds OrbitOS from the source code. It takes about 15 to 30 minutes,
depending on your computer. Later starts take about a minute.

Then:

1. Open **http://localhost:8080**. The setup page opens.
2. Choose your language.
3. Enter the **setup code**. For security, it is only shown in the server log:
   ```bash
   docker compose logs orbitos
   ```
   Look for "First-run setup" and copy the code (`XXXX-XXXX-XXXX`).
4. Create your account: organization name, your email and a password (12 characters or more).
5. Keep "Create a sample device and dashboard" checked, then **Finish setup**.
6. Click **Send a test value**, then **Sign in and open the dashboard**. You should see the value.

That's it. To check that everything is healthy:

```bash
docker compose ps
```

The `orbitos` line says `healthy` when the server and the database work.

### Two accounts

| Account | Used for | Login |
|---|---|---|
| Organization administrator | Devices, dashboards, rules, users: daily work | your email |
| Platform administrator | Organizations, platform settings (mail server, security) | `sysadmin@local.invalid` (or the address you chose) |

Your account uses the password you chose. The platform administrator gets its own random
password, shown **once** at the end of setup (save it in a password manager); it must be changed
at its first sign-in. See [privacy and security controls](privacy-security.md) for two-factor
sign-in, the audit log, retention and privacy requests.

## Settings

All settings are optional. Copy `.env.example` to `.env`, edit it, then run `docker compose up -d` again.

| Setting | Default | Meaning |
|---|---|---|
| `ORBITOS_BIND` | `127.0.0.1` | Who can connect. `127.0.0.1` = this computer only. `0.0.0.0` = your network too (needed for real devices). |
| `ORBITOS_HTTP_PORT`, `ORBITOS_MQTT_PORT`, `ORBITOS_COAP_PORT` | 8080, 1883, 5683 | Ports on this computer. |
| `ORBITOS_DEFAULT_LANGUAGE` | `en_US` | Language for emails and default data, until the setup page sets it. |
| `ORBITOS_BRAND_NAME` | (from `branding.yml`) | Product name shown everywhere. See the [branding guide](branding.md). |
| `ORBITOS_ONLINE_MAPS` | `false` | `true` lets map widgets load free online map tiles. |
| `ORBITOS_ALLOWED_HOSTS` | (empty) | Other web hosts the interface may load data from. |
| `ORBITOS_JAVA_OPTS` | `-Xms256m -Xmx1g` | Server memory. |
| `ORBITOS_MQTT_TLS`, `ORBITOS_MQTTS_PORT` | `false`, 8883 | Encrypted MQTT; see [Connect devices](connect-devices.md). |
| `ORBITOS_GATEWAY_TOKEN` | (empty) | Token of the IoT Gateway add-on; see [Connect devices](connect-devices.md#gateway). |
| `ORBITOS_IMAGE` | (built from source) | A published image to use instead of building. |

## Use a published image

If your organization publishes OrbitOS images (see the developer guide), you can skip the build:
set `ORBITOS_IMAGE=ghcr.io/<owner>/orbitos:<version>` in `.env`, then run
`docker compose pull orbitos` and `docker compose up -d --no-build`. Start-up then takes about 3 minutes.

## Secure defaults

- **No default password.** The database password is random and generated on first start.
  The administrator account is created on the setup page, protected by a one-time code.
- **Nothing exposed by default.** Ports listen on this computer only. The database has no
  port outside Docker.
- **No outbound calls.** The server never contacts the internet on its own. The web interface
  may only load data from your server (a browser rule, "Content Security Policy"). Map widgets
  show no map tiles until you set `ORBITOS_ONLINE_MAPS=true`.

## HTTPS

To serve OrbitOS over HTTPS on ports 443 and 80:

```bash
docker compose --profile https up -d
```

- On a local network (default): a certificate from a local authority, no internet needed.
  Browsers show a warning until you trust that authority: see Caddy's "local HTTPS" documentation.
- With a public domain name: in `.env`, set `ORBITOS_DOMAIN=iot.example.ca` and
  `ORBITOS_TLS=you@example.ca`. A free certificate is requested from Let's Encrypt (this
  contacts the internet).

MQTT over TLS (port 8883) is not set up yet.

## Back up and restore

Back up (the server can keep running):

```bash
docker compose run --rm backup
```

The file is written to the `backups` folder, for example `backups/orbitos-20261002-101500.dump`.
Copy it to another disk or computer.

Restore (replaces all data with the backup):

```bash
docker compose stop orbitos
docker compose run --rm restore orbitos-20261002-101500.dump
docker compose start orbitos
```

The backup contains the database: devices, telemetry, dashboards, users, settings.

## Upgrade

1. Back up: `docker compose run --rm backup`
2. Get the new version: `git pull` (or unpack the new release in the same folder).
3. Build it and upgrade the database:
   ```bash
   docker compose build orbitos
   docker compose stop orbitos
   docker compose run --rm orbitos upgrade
   docker compose up -d
   ```

### Roll back

If something goes wrong after an upgrade:

1. Go back to the previous version: `git checkout <previous tag>`, then `docker compose build orbitos`.
2. Restore the backup taken in step 1 (see "Back up and restore").
3. `docker compose up -d`

## Uninstall

```bash
docker compose down        # stop, keep the data
docker compose down -v     # stop and DELETE all data (database, generated password)
```

## Troubleshooting

| Problem | What to do |
|---|---|
| The page doesn't open | `docker compose ps`: wait until `orbitos` is `healthy` (first start: a few minutes). Then `docker compose logs orbitos`. |
| Lost the setup code | `docker compose logs orbitos`. After 10 wrong tries a new code is printed. |
| "An earlier installation did not finish" | The first start was interrupted. Remove the data and start again: `docker compose down -v`, then `docker compose up -d`. |
| Devices on the network can't connect | Set `ORBITOS_BIND=0.0.0.0` in `.env` and check your firewall. |
