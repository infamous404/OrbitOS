# Phase 1 status: fork, branding, white-label

Date: 2026-10-02. Labels: **VERIFIED** (run in this session), **UNVERIFIED** (written, not run), **NOT DONE**.

## Acceptance

| Criterion | Status | Evidence |
|---|---|---|
| Builds | VERIFIED | `docker build -f orbitos/docker/Dockerfile .` succeeded (Java 17 + Angular build inside Docker) |
| Starts | VERIFIED | `orbitos/scripts/smoke-test.sh`: install on a fresh PostgreSQL 16, server starts, 10/10 checks pass |
| No stray upstream product names in the UI | VERIFIED for: login page, tenant home page, full tenant menu (EN and FR), page title, favicon, English UI strings file | Browser check + `rebrand.py --check` (scans all UI templates and code strings) |
| No stray upstream names in docs, except attribution files | VERIFIED for OrbitOS docs (README EN/FR, SECURITY, CHANGELOG, branding guides). **Exception:** upstream developer docs not shipped to users still mention it: `docker/README.md`, `msa/**/README.md`, `TEST_FAST.md`, `.github/ISSUE_TEMPLATE/*`, `pull_request_template.md` | To be replaced in Phase 6 (Contributing guide) |

## Items

| Item | Status | Notes |
|---|---|---|
| Fork at `v4.3.1.6`, branch `main`, upstream remote limited to `lts-4.3` + `v4.3*` tags, push disabled | VERIFIED | `C:\dev\orbitos` |
| Fork and patch process documented | VERIFIED (written) | `UPSTREAM_PATCHES.md` sections 1-4 |
| Licence check for every upstream patch | VERIFIED | Passes the 2 pending `lts-4.3` commits; **refuses** the real BUSL `v4.4` commit (3 reasons); `--tree` passes |
| Every core edit logged with reason | VERIFIED (written) | `UPSTREAM_PATCHES.md` section 5 |
| Call home: update checker removed | VERIFIED | `/api/admin/updates` answers "no update" without network; class deleted |
| Call home: Community Grant removed | VERIFIED | Endpoint returns 404; code, tests, UI deleted |
| Call home: IoT Hub off by default and hidden | VERIFIED | `iotHubBaseUrl` empty; no IoT Hub in menu or home page; server client refuses calls |
| Call home: remote help pages | VERIFIED (config) | `ui.help.base-url=/assets` |
| Call home: GitHub star badge removed | VERIFIED | Not in page |
| No vendor hosts in server log | VERIFIED | Smoke test |
| Edge support off by default | VERIFIED (config) | Not in menu |
| Branding in one file (`orbitos/branding/branding.yml`) | VERIFIED | Name, logo, favicon, login title/subtitle, footer, legal links served by `/api/noauth/branding` |
| Brand name in UI strings, help, emails, email subjects at runtime | VERIFIED for UI strings and title. Emails and help: **UNVERIFIED** (code in place, no email sent, help page not opened) | |
| Colors from `branding.yml` | VERIFIED | Generated palette compiled; login and menu use the OrbitOS blue/orange |
| Logo SVG, favicon, color theme, branded login page | VERIFIED | Screenshot of login page |
| Logo/SVG served safely | VERIFIED | `Content-Security-Policy: ... sandbox`, `nosniff`; path traversal and bad types refused (unit test) |
| Rebrand script: idempotent, `--dry-run`, `--check`, allowlist | VERIFIED | Second run: "Nothing to change"; `--check` exit 0 |
| Branding unit tests | VERIFIED | `BrandingServiceTest`: 7 run, 0 failures (`docker build --target test`) |
| Bilingual branding guide | VERIFIED (written) | `orbitos/docs/en/branding.md`, `orbitos/docs/fr/branding.md` |
| CI: branding check, licence check, build, unit tests, smoke test | UNVERIFIED | `.github/workflows/orbitos-ci.yml` (not run: no GitHub repo yet) |
| CI: weekly dependency scan (Trivy) | UNVERIFIED | `.github/workflows/orbitos-security.yml` |
| Changing the brand at runtime by mounting another folder | UNVERIFIED | Code path tested with the default folder only |

## Known items left for later phases

- Default admin account is still `sysadmin@thingsboard.org` / `sysadmin` (Phase 3: generated admin, first-run wizard).
- Help (?) links in the UI still open the upstream online documentation (Phase 6: OrbitOS docs).
- The home page's "connect mobile app" card points to the upstream mobile app (mobile is out of scope; decide in Phase 3 whether to hide it).
- Unused `ce-grant.*` and `iot-hub.*` translation keys remain (Phase 2: unused-key check).
- Seven built-in widget descriptions keep the brand name used at build time.
