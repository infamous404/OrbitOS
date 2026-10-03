# Phase 6 status: docs, add-ons, version 1

Date: 2026-10-03. Labels: **VERIFIED** (run in this session), **UNVERIFIED** (written, not run), **NOT DONE**, **DEFERRED**.

Evidence: `orbitos/scripts/smoke-test.sh` on a fresh install, **31 checks, all passed**; 25 unit
tests passed; browser checks of the docs page (EN, FR) and the help-link redirect.

| Item | Status | Evidence |
|---|---|---|
| IoT Gateway add-on (`--profile gateway`) | VERIFIED | Smoke test: simulated Modbus meter → gateway → platform, values in range |
| Gateway: no update checks, no public DNS, no run-time downloads for Modbus/BACnet | VERIFIED | Patch present in the image; no download or update lines in the gateway log |
| Gateway: OPC UA and BACnet connectors | UNVERIFIED | Not tested with devices; OPC UA library (LGPL) downloads on first use, by design |
| Encrypted MQTT on 8883, certificate created offline | VERIFIED | Smoke test: value stored over TLS; connection refused when the certificate is not trusted |
| Own certificate (PEM) for MQTT | UNVERIFIED | Documented (`compose.override.yaml`) |
| In-app docs at `/docs` (EN, FR, offline) | VERIFIED | Browser: menu, guide, anchors, links between guides, French version |
| Help links redirected to the OrbitOS guides (about 150 links) | VERIFIED | Browser: vendor links rewritten to the matching guide (rules, devices, email, getting started, fallback) |
| Guides: quick start, admin, developer, index (EN/FR); devices and install updated | VERIFIED (written) | `orbitos/docs/{en,fr}/` |
| 20 admin help pages in French (notifications, date formats, certificates) | VERIFIED | `orbitos/i18n/help_fr.py --check`: 0 untranslated lines; CI check added |
| Developer help pages in French (script and widget reference, about 13,000 lines) | NOT DONE | Stay in English |
| Paid-edition upsell removed from the platform admin home page | VERIFIED (file) | 2-line change in `sys_admin_home_page.json`; not looked at in the browser |
| Vendor mobile-app QR card hidden, store links empty | VERIFIED (setup succeeds with the change) / UNVERIFIED (home page not looked at) | Bug found and fixed during testing: setup failed while saving this setting |
| Issue and PR templates rewritten (EN/FR) | VERIFIED (written) | |
| Upstream developer docs not shipped to users (`docker/README.md`, `msa/**/README.md`, `TEST_FAST.md`) | NOT DONE | Still name the upstream product |
| NOTICE: gateway, Python packages, PostgreSQL, Caddy | VERIFIED (written) | |
| `IDEA_HARVEST.md` in the repository with version-1 status | VERIFIED (written) | English only |
| Dashboard templates beyond the sample dashboard | DEFERRED | Scope freeze |
| Final report | VERIFIED (written) | [FINAL_STATUS.md](FINAL_STATUS.md) |
| Zip of the repository | VERIFIED | `C:\dev\orbitos-v1.zip` (`git archive` of the final commit) |
