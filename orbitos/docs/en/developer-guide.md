# Developer guide

*[Version française](../fr/developer-guide.md)*

How OrbitOS is built, tested and kept in sync with its upstream project.

## Layout

| Path | What |
|---|---|
| `orbitos/` | Everything OrbitOS adds: Docker files, scripts, branding, docs, translations, gateway add-on |
| `application/src/main/java/org/thingsboard/server/orbitos/` | OrbitOS server code (setup, branding, privacy, audit chain, offline policy, docs) |
| `ui-ngx/src/app/orbitos/` | OrbitOS web code (setup page, docs page, language switcher, privacy actions) |
| `ui-ngx/src/assets/orbitos/` | OrbitOS translations (`locale/`) and languages list |
| `application/src/main/resources/orbitos.yml` | Every OrbitOS default that differs from upstream |
| Everything else | Upstream code (Apache-2.0). Each change is listed in `UPSTREAM_PATCHES.md` |

Rule of thumb: put new code under an `orbitos` folder; change an upstream file only when there is
no other way, keep the change small, and log it in `UPSTREAM_PATCHES.md`.

## Build and test

Only Docker is needed.

```bash
docker compose build orbitos                                       # the image (15-30 min the first time)
docker build -f orbitos/docker/Dockerfile --target test .          # unit tests of OrbitOS code
bash orbitos/scripts/smoke-test.sh                                 # fresh install end to end (about 10 min)
```

The smoke test starts a separate stack (`orbitos-smoke`, port 18080), runs about 35 checks
(setup, security, privacy, MQTT both ways, gateway, backups, audit tampering) and removes it.
`KEEP_RUNNING=1` leaves it running.

Quick checks, no build needed (Python 3):

```bash
python orbitos/scripts/rebrand.py --check              # branding applied, no upstream product name
python orbitos/scripts/i18n_check.py                   # translations complete
python orbitos/scripts/i18n_regional.py --check        # fr_CA / en_CA files up to date
python orbitos/scripts/upstream_license_check.py --tree  # only Apache-2.0 code
```

CI (`.github/workflows/orbitos-ci.yml`) runs all of these on every push.

## Common changes

- **Brand**: [Your brand](branding.md). **Language**: [Add a language](add-a-language.md).
- **A device protocol**: start from `orbitos/scripts/mqtt_device.py` (MQTT client in about 200
  lines, no dependencies) or the IoT Gateway connectors (`orbitos/gateway/`).
- **A setting**: add it to `orbitos.yml` with an environment variable, and to `.env.example`.
- **A screen**: standalone Angular components in `ui-ngx/src/app/orbitos/`; text keys under
  `orbitos.*` in `ui-ngx/src/assets/orbitos/locale/en_US.json` and `fr_FR.json`.

## Upstream updates

OrbitOS follows the upstream `lts-4.3` branch, Apache-2.0, until 2027-07-20. Before merging any
upstream change, run the licence check on it:

```bash
python orbitos/scripts/upstream_license_check.py upstream/lts-4.3
```

It refuses anything that is not Apache-2.0. **Never** copy code from upstream 4.4 or later
(different licence). The full procedure is in `UPSTREAM_PATCHES.md`.

## Publishing an image

`.github/workflows/orbitos-publish.yml` builds, tests, signs and pushes to the GitHub Container
Registry. It is off until a maintainer sets the repository variable `ORBITOS_PUBLISH=true`, then
runs on version tags (`v1.0.0`).

## Licence

Apache-2.0. Keep the SPDX header on every file. Third-party code must be under a permissive
licence and listed in `NOTICE`.
