# OrbitOS version 1: final status report

Date: 2026-10-03. Labels: **VERIFIED** (run and checked in this project), **UNVERIFIED** (written,
not run), **NOT DONE**, **DEFERRED** (cut on purpose by the owner's scope freeze of 2026-10-02:
simple, fast setup, lightweight where possible).

Item-by-item evidence is in the phase reports: [Phase 1](PHASE1_STATUS.md) · [Phase 2](PHASE2_STATUS.md) ·
[Phase 3](PHASE3_STATUS.md) · [Phase 4](PHASE4_STATUS.md) · [Phase 5](PHASE5_STATUS.md) · [Phase 6](PHASE6_STATUS.md) ·
[After version 1](POST_V1_STATUS.md) (logo, look, language fix, CoAP and OPC UA checks).

This is not legal advice. A qualified Canadian privacy lawyer or privacy officer should review
OrbitOS and how you use it (Law 25, PIPEDA, GDPR, CCPA, Bill 96) before production use.

## What OrbitOS is

A fork of ThingsBoard Community Edition 4.3.1.6 (Apache-2.0), with no code from 4.4 or later.
Same platform features (devices, dashboards, rules, alarms, multi-tenancy, APIs), plus:
English and French (Canada and France), one-command install, no default passwords, no calls
home, offline by default, privacy and security tools, and your own brand.

## How it was verified

| Check | Result |
|---|---|
| Unit tests of OrbitOS code (`docker build --target test`) | VERIFIED: 25 tests, 0 failures |
| End-to-end test of a fresh install (`orbitos/scripts/smoke-test.sh`) | VERIFIED: 31 checks, all passed (2026-10-03) |
| Branding check, licence check, translation checks, French help check | VERIFIED: all pass |
| Browser walk-throughs (setup, French screens, dashboards, privacy buttons, docs page) | VERIFIED (see phase reports) |
| CI and publish workflows on GitHub | UNVERIFIED: no GitHub repository yet |

## Summary by goal

| Goal | Status | Notes |
|---|---|---|
| Apache-2.0 only; every upstream patch licence-checked | VERIFIED | `upstream_license_check.py` refuses BUSL code (tested on the real v4.4 commit) |
| No upstream trademarks in the UI and user docs | VERIFIED | Attribution only in LICENSE, NOTICE, README. Upstream developer READMEs replaced by OrbitOS notes. Edge install instructions still name it (only shown if Edge is turned on; off by default): NOT DONE |
| Configurable brand | VERIFIED | `orbitos/branding/branding.yml` or `ORBITOS_BRAND_NAME` |
| No telemetry, no calls home | VERIFIED | Update checker, Community Grant, IoT Hub, remote help, GitHub badge, email images, vendor mobile-app card, paid-edition upsell: all removed or off. Gateway add-on: update checks and DNS change removed |
| Works local and offline | VERIFIED | Browser policy allows only this server; maps and other hosts opt-in |
| English and French, fr-CA natural | VERIFIED | 9,700 UI strings, emails, default data, 20 help pages, all guides. Owner reviews fr-CA |
| Easy install | VERIFIED | `docker compose up -d`, setup page, about 3 minutes with a built image |
| Under 15 minutes from a fresh machine | PARTLY | Yes with a published image; building from source adds 15-30 minutes. Publishing needs a target registry (workflow ready, off) |
| Lightweight | VERIFIED | About 1.3 GB of RAM running |
| Secure defaults | VERIFIED | Generated secrets, no exposed database, forced change of the generated admin password, 12-character passwords, lockout, two-factor available |
| Tamper-evident audit log | VERIFIED | Changed and deleted entries detected |
| Privacy tools (Law 25, PIPEDA) | VERIFIED | Export and erasure of an account; retention defaults |
| Device protocols | VERIFIED: MQTT both ways, HTTP, CoAP, WebSocket API | LoRaWAN documented only |
| Encrypted MQTT, industrial protocols (gateway add-on) | VERIFIED: MQTT over TLS; Modbus and OPC UA through the gateway (simulated machines) | BACnet UNVERIFIED (no simulator) |
| WCAG 2.1 AA | PARTLY | OrbitOS screens checked; automated contrast check of the main upstream screens, problems found fixed. Full audit (screen reader, every dialog): NOT DONE |
| Bilingual docs: quick start, install, admin, developer, devices, APIs, privacy | VERIFIED (written) | Also in the app at `/docs` |

## Deferred by the scope freeze

NGSI-LD API, Smart Data Models, Edge offline sync, Kubernetes manifests, consent records,
incident register, privacy-officer setting, PIA template, extra roles (operator, viewer,
auditor), SSO examples (Keycloak), quota presets, extra dashboard templates, developer help pages
in French (script and widget reference, about 13,000 lines), French version of `IDEA_HARVEST.md`.

## Known limits

- Upstream security fixes for this licence line end on 2027-07-20; after that, OrbitOS must backport fixes itself.
- Audit chain: detects tampering, cannot prevent it; keep a copy of the latest hash elsewhere.
- Privacy erasure keeps audit details the person typed until the audit retention ends; backups keep their content.
- The OPC UA library (LGPL) is not bundled: the OPC UA connector downloads it once.
- MQTT certificate is self-signed unless you provide one.

## Deliverables

| File | Status |
|---|---|
| Repository `C:\dev\orbitos` (branch `main`) | Ready; not pushed (no target given) |
| `C:\dev\orbitos-v1.zip` | Ready (`git archive` of the final commit) |
| `README.md`, `README.fr.md`, `LICENSE`, `NOTICE`, `UPSTREAM_PATCHES.md`, `CHANGELOG.md`, `SECURITY.md`, `IDEA_HARVEST.md` | Present |
