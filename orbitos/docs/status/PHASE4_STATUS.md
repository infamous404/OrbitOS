# Phase 4 status: privacy and security essentials

Date: 2026-10-03. Labels: **VERIFIED** (run in this session), **UNVERIFIED** (written, not run), **NOT DONE**.
Scope (owner, 2026-10-02): essentials only. Everything else from the original Phase 4 list is **DEFERRED**.
This is not legal advice; a privacy lawyer or privacy officer should review before production use.

Evidence: `orbitos/scripts/smoke-test.sh` (27 end-to-end checks on a fresh install, all passed),
23 unit tests (all passed), browser walk-through in French (setup page, forced change, users list).

## Controls

| Control | Status | Evidence |
|---|---|---|
| Platform admin password generated (20 characters), shown once on the setup page, never stored in clear | VERIFIED | Smoke test; browser (final setup step) |
| Organization admin's password does not open the platform admin account | VERIFIED | Smoke test (401) |
| Forced change of the generated password at the first sign-in | VERIFIED | Smoke test (API: "expired", reset token); browser: sign-in opens the "choose a new password" page, new password works, old one refused |
| New password must have 12+ characters | VERIFIED | Smoke test (`too-short` refused) |
| Password policy default: 12 characters; lockout after 10 failed sign-ins | VERIFIED (settings) / UNVERIFIED (lockout triggered) | Smoke test reads the settings |
| Two-factor sign-in available (authenticator app, backup codes), not forced | VERIFIED | Smoke test: enables TOTP, then password alone gives only a pre-verification token; a real TOTP code completes sign-in |
| Backup codes | UNVERIFIED | Provider enabled; not used in a test |
| "Enforce two-factor" switch for administrators | UNVERIFIED | Upstream setting, documented |
| Audit log hash chain (SHA-256) on every new entry | VERIFIED | Smoke test: chain intact via API and via `docker compose run --rm orbitos verify-audit` |
| Tampering detected: changed entry, deleted entry | VERIFIED | Smoke test edits and deletes rows in the database; the check reports `changed` and `deleted` with the entry ID |
| Inserted entry detected | VERIFIED by code / UNVERIFIED by test | Check for audit rows without a chain link |
| Chain survives backup and restore, and privacy pseudonymization | VERIFIED | Smoke test: intact after erase + restore |
| Retention defaults: telemetry 365 d, alarms 365 d, RPC 30 d (organization profile) | VERIFIED (settings) | Smoke test reads the default profile |
| Retention defaults: audit log 365 d, events 30 d, telemetry fallback 365 d (server) | UNVERIFIED (deletion) | Settings in `orbitos.yml`; actual deletion after a year cannot be observed in a test |
| Privacy export (JSON: account, sign-in status, preferences, audit entries, notifications, customer) | VERIFIED | Smoke test; browser: button downloads, confirmation message |
| Privacy erasure (account, sign-in data, 2FA, preferences, notifications deleted; audit names pseudonymized) | VERIFIED | Smoke test: account gone, email no longer in audit names |
| Access rules: own organization only; no self-erasure; no platform-admin erasure | VERIFIED | Smoke test (404 / 403) |
| Export/erase buttons in the users list, confirmation dialog in French | VERIFIED (browser) | |
| GHCR publish workflow (tests, smoke test, push, SBOM, provenance, keyless signature), off unless `ORBITOS_PUBLISH=true` | UNVERIFIED | YAML validated; never run: nothing published |
| `ORBITOS_IMAGE` to use a published image instead of building | UNVERIFIED | No published image exists yet |
| Guide "Privacy and security controls" (EN, FR) | VERIFIED (written) | `orbitos/docs/{en,fr}/privacy-security.md` |

## Known limits (documented)

- The audit chain detects tampering; someone with full database access could rebuild it. Keep the `latestHash` elsewhere.
- Erasure does not rewrite text the person typed inside audit details; it ends with the audit retention period.
- Device data is not linked to people by the platform; privacy requests about device data are handled per device.
- Backups keep their content; old backup files must be deleted by the operator.
- Single-server design for the audit chain (one process appends links).

## Deferred (scope freeze)

Consent records, confidentiality-incident register, privacy-officer contact setting, PIA template,
RBAC roles beyond upstream (operator, viewer, auditor), SSO (OIDC/SAML), per-tenant quotas review,
pseudonymization beyond audit names.
