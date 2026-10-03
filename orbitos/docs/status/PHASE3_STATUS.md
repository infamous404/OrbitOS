# Phase 3 status: easy, safe, offline-first setup

Date: 2026-10-02. Labels: **VERIFIED** (run in this session), **UNVERIFIED** (written, not run), **NOT DONE**.
Scope freeze (owner, 2026-10-02): simple, fast setup, lightweight where possible. Items marked
**DEFERRED** were cut on purpose.

Evidence: `orbitos/scripts/smoke-test.sh` (18 end-to-end checks on a fresh install, all passed),
19 unit tests (all passed), a browser walk-through of the setup page and the sample dashboard.

| Item | Status | Evidence / notes |
|---|---|---|
| One-command install: `docker compose up -d` (database created on first start) | VERIFIED | Smoke test; fresh start to healthy in 2 min 43 s with a built image |
| Memory use | VERIFIED | 1.16 GB server + 0.1 GB database after start (`docker stats`) |
| `.env.example` (all optional, bilingual comments, no secrets) | VERIFIED (written, used by compose) | |
| Health check: `docker compose ps` shows `healthy`; `/api/noauth/orbitos/health` | VERIFIED | Bug found and fixed: during the first install port 8080 answers with redirects, which looked healthy; the check now requires "UP" |
| Generated secrets: random database password on first start, kept in a private volume | VERIFIED | Smoke test (no password configured anywhere) |
| No default passwords: upstream `sysadmin@thingsboard.org` / `sysadmin` gone | VERIFIED | Smoke test: that login returns 401 |
| Setup protected by a one-time code printed only in the server log; new code after 10 wrong tries | VERIFIED (code, wrong code) / UNVERIFIED (10-tries rotation) | |
| Database not exposed; all ports on 127.0.0.1 by default | VERIFIED | Smoke test: no published database port |
| TLS ready to enable (`docker compose --profile https up -d`, Caddy, local certificate by default) | UNVERIFIED | Written and documented; not started in this session |
| MQTT over TLS | NOT DONE | Documented as not set up yet |
| Telemetry off / no call-home | VERIFIED | Smoke test re-checks updates, IoT Hub, Community Grant, vendor hosts in the log |
| First-run setup page: language, organization, your account, platform administrator | VERIFIED | Browser (English) and smoke test (French, fr_CA) |
| First device, sample dashboard, "Send a test value" | VERIFIED | Browser: value shown live on the dashboard (21.5 °C, 41 %). Bug found and fixed: the button went through the signed-in HTTP layer |
| Default data in the chosen language (root rule chain, notification templates, rules, recipient groups) | VERIFIED | Smoke test (French names); unit test: every default notification text has a French version |
| New organizations created later use the platform language | VERIFIED by unit test / UNVERIFIED by click | Same code path as setup |
| Backup command | VERIFIED | Smoke test |
| Restore command (refuses while the server runs) | VERIFIED (restore) / UNVERIFIED (refusal) | Smoke test: data after restore = data at backup time |
| Upgrade and rollback documented | UNVERIFIED | `orbitos/docs/{en,fr}/install.md`; no newer version exists to upgrade to yet |
| Offline mode: browser may only load data from the server (CSP); online maps and other hosts opt-in | VERIFIED | Smoke test (header); browser: dashboard and live updates work with no policy violations |
| Map widgets without `ORBITOS_ONLINE_MAPS` | UNVERIFIED by eye | Tiles are blocked by design; the map shows no background |
| Brand name setting `ORBITOS_BRAND_NAME` | VERIFIED by unit test | |
| Small "cyber" accents (cyan lines, monospace codes, dark sample dashboard) | VERIFIED (browser) | Text colors checked for contrast on the setup card (5.1:1 to 6.2:1) |
| Language switcher after a sign-out | VERIFIED fixed | Bug found: the last user's saved language overrode the switcher on the login and setup pages |
| Kubernetes manifests | DEFERRED | Scope freeze |
| Deferred sync (store-and-forward) | DEFERRED | Scope freeze (Edge sync, harvest TB-11) |
| Acceptance: fresh machine to a working dashboard in under 15 minutes following the README | PARTLY | With a built image: about 5 minutes (2 min 43 s start + setup). Building from source adds 15 to 30 minutes, so a first install from source does **not** meet 15 minutes. Publishing a ready-made image would; that needs a registry, which needs your decision |

## Known limits

- Building from source needs about 4 GB of RAM; running needs about 1.3 GB.
- The platform administrator starts with the same password as the organization administrator (documented; change it after sign-in).
- If the very first start is interrupted during the database install, the server stops with a clear message; the fix is `docker compose down -v` and start again (documented).
- The install guide uses `git clone <your copy>`: there is no public repository yet.
