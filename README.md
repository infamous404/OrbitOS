# OrbitOS

*[Version française](README.fr.md)*

OrbitOS is an open-source IoT platform for Canada and beyond: connect devices, collect their data,
see it on dashboards and act on it with rules. It works in English and French, runs fully on your
own computer or server, and never sends data or usage information to anyone.

> **Status: early development (Phase 1 of 6).** Not ready for production. See [CHANGELOG.md](CHANGELOG.md).

## What makes it different

- **Local and private.** No telemetry, no update checks, no marketplace calls. Every outbound
  network call is listed in [UPSTREAM_PATCHES.md](UPSTREAM_PATCHES.md) and is off by default.
- **Your brand.** Name, logo, colors, login text and legal links come from one file:
  [`orbitos/branding/branding.yml`](orbitos/branding/branding.yml). See the
  [branding guide](orbitos/docs/en/branding.md).
- **Bilingual.** English and French (Canada and France) are first-class. *(Work in progress: Phase 2.)*
- **Open licence.** Apache License 2.0, forever. See [Licence](#licence).

## Try it (developers)

You need Docker. Everything else is built inside Docker.

1. Build the image (about 20 minutes the first time):
   ```bash
   docker build -f orbitos/docker/Dockerfile -t orbitos/orbitos:dev .
   ```
2. Create `orbitos/docker/.env` with one line: `ORBITOS_DB_PASSWORD=` followed by a long random value.
3. Create the database (first time only):
   ```bash
   docker compose -f orbitos/docker/compose.yml --profile install run --rm orbitos-install
   ```
4. Start:
   ```bash
   docker compose -f orbitos/docker/compose.yml up -d
   ```
5. Open http://localhost:8080.

Phase 1 still uses the upstream default administrator account. A one-command install with
generated passwords and a first-run wizard comes in Phase 3.

## Licence

OrbitOS is licensed under the [Apache License 2.0](LICENSE).

OrbitOS is a fork of [ThingsBoard](https://github.com/thingsboard/thingsboard) Community Edition
4.3.1.6, the last Apache-2.0 release line. It does not contain code from ThingsBoard 4.4 or later,
which use a different licence. OrbitOS is not affiliated with or endorsed by ThingsBoard, Inc.
See [NOTICE](NOTICE) and [UPSTREAM_PATCHES.md](UPSTREAM_PATCHES.md).

## Security

See [SECURITY.md](SECURITY.md).
