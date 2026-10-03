# OrbitOS: Idea Harvest

Status: **approved 2026-10-02**, with the trims listed under "Changes after review". Internal planning record, in English only (no French version: see FINAL_STATUS.md).

License column:

- **In base**: already in our Apache-2.0 fork base (ThingsBoard v4.3.1.6). We keep or adjust it.
- **Reusable (license)**: permissive licence, VERIFIED. We may reuse code or run it as a separate service, recorded in NOTICE.
- **Idea only**: AGPL. We study docs, API specs and behavior, then write original code. No copying or translating.
- **Spec**: open standard, implemented from the public specification.

Effort: S = days, M = 1–2 weeks, L = more than 2 weeks.

How the ideas were checked: ThingsBoard rows are VERIFIED from the code. OpenRemote and FIWARE rows are FROM DOCS (their websites, READMEs and specs).

## Status in version 1 (2026-10-03)

Scope freeze (owner, 2026-10-02): simple, fast setup, lightweight where possible. Details in
`orbitos/docs/status/FINAL_STATUS.md`.

| Item | Status |
|---|---|
| TB-1 rule engine, TB-12 notifications | Done: default rule chain and notifications created in French or English |
| TB-2 device profiles, first device | Done: setup page creates the first device and a dashboard |
| TB-4 audit log | Done: SHA-256 hash chain, `verify-audit` command |
| TB-7 monolith, OR-8 easy install | Done: `docker compose up -d`, generated secrets, setup code, optional HTTPS (Caddy) |
| TB-8 languages | Done: en-CA, en-US, fr-CA, fr-FR |
| TB-9 APIs | Done: API reference rebranded, REST and WebSocket verified, guides |
| TB-13 two-factor sign-in | Done: authenticator app and backup codes on by default (not forced) |
| TB-17 IoT Gateway | Done as an optional add-on (no calls home, Modbus and BACnet offline); verified with a Modbus simulator |
| CS-1 ChirpStack | Documented only (no hardware) |
| OR-9 docs split | Done: quick start, admin, developer, devices, APIs, privacy, in-app at `/docs` |
| TB-3 quotas, OR-3 isolation tests, OR-2 Keycloak example, TB-5 templates, TB-6 OTA docs | Deferred (scope freeze) |
| FI-1 NGSI-LD, FI-2 Smart Data Models | Deferred (scope freeze) |
| TB-11 Edge | Documented as deferred; Edge support off by default |

## Changes after review (2026-10-02)

The owner asked to drop anything that adds weight without serving the GOAL.

| ID | Was | Now | Reason |
|---|---|---|---|
| TB-11 | Defer (your call) | **Defer: document now, build later** | Owner decision |
| FI-1 | Adopt, Phase 5 | **Adopt, scheduled after Phase 3 (Phase 5)**, read-only first | Owner decision |
| OR-1 | Adopt, separate asset catalog | **Merged into FI-2** | One asset model, not two. Smart Data Models provide the types; OpenRemote's typed-attribute idea shapes how we store them |
| OR-2 | Adopt, bundled Keycloak service | **Adopt, reduced to docs + one tested example config** (S) | The base already has OIDC login. Bundling Keycloak adds a heavy service to every install. Anyone who needs SAML or LDAP plugs in Keycloak (or another IdP) through OIDC |
| FI-5 | Adopt, adapter-skeleton pattern | **Reject** | Phase 0 found no protocol gap that needs our own adapter. The IoT Gateway and ChirpStack cover the gaps; Zigbee/Z-Wave go through existing MQTT bridges |
| OR-7 | Defer (mobile apps) | **Reject** | Not in the GOAL |
| OR-11 | Defer (OpenTelemetry) | **Reject** | Prometheus/Grafana option already exists |
| FI-4 | Defer (Data Space Connector) | **Reject** | Too heavy for this project |

## Adopt

| ID | Source | Idea or practice | Why it helps OrbitOS | License status | Phase | Effort |
|---|---|---|---|---|---|---|
| TB-1 | ThingsBoard | Rule engine and rule chains | Core automation. Already works | In base | 2 (translate node names and help) | S |
| TB-2 | ThingsBoard | Device profiles, provisioning (pre-provisioned, claim, X.509) | Base for the "add first device" wizard | In base | 3 | M |
| TB-3 | ThingsBoard | Tenant profiles with rate limits and API-usage quotas | Per-tenant quotas with no new code; we add simple presets | In base | 4 | S |
| TB-4 | ThingsBoard | Audit log | Starting point for a hash-chained, tamper-evident log | In base | 4 | M |
| TB-5 | ThingsBoard | Dashboards and widgets | Templates for Phase 6. Widgets must be fully translated | In base | 2, 6 | M |
| TB-6 | ThingsBoard | OTA firmware and software updates | Already there. Only needs docs | In base | 6 | S |
| TB-7 | ThingsBoard | Monolith mode (one container, in-memory queue, Postgres) | Makes one-command install possible; the cluster stays optional | In base | 3 | M |
| TB-8 | ThingsBoard | i18n structure: JSON per locale, saved per user, browser fallback | Extended with fr-CA and en-CA instead of building new | In base | 2 | M |
| TB-9 | ThingsBoard | REST + WebSocket API with OpenAPI spec | Documented APIs. Swagger UI off by default | In base | 4, 5 | S |
| TB-12 | ThingsBoard | Notification center (email, SMS, in-app) | Templates need French versions | In base | 2 | M |
| TB-13 | ThingsBoard | 2FA (TOTP, email, backup codes) and OAuth2/OIDC login | Covers most of the MFA and SSO goal with no new code | In base | 4 | S |
| TB-17 | ThingsBoard IoT Gateway | Separate gateway for Modbus, OPC UA, BACnet, BLE, CAN, KNX, S7, OCPP… | Fills the industrial gaps without writing protocol code | Reusable (Apache-2.0), separate service | 5 | M |
| CS-1 | ChirpStack | LoRaWAN network server with a built-in ThingsBoard-API integration | Fills the LoRaWAN gap | Reusable (MIT), separate service | 5 | M |
| OR-2 | OpenRemote | Keycloak for SSO/MFA/federation | SAML and LDAP for those who need them, via the existing OIDC login. **Docs + one tested example config only** | Idea only (OpenRemote); Keycloak reusable (Apache-2.0) | 4 | S |
| OR-3 | OpenRemote | Strict per-realm (tenant) isolation | We already have tenants; we add **tests** that prove isolation | Idea only | 4 | S |
| OR-8 | OpenRemote | Easy install: one compose file, one hostname variable, self-signed TLS by default, reverse proxy | Model for `ORBITOS_HOSTNAME` and TLS-ready defaults. Their fixed default password (`admin/secret`) is **not** copied: we generate one | Idea only | 3 | M |
| OR-9 | OpenRemote | Docs split into quick start / user guide / developer guide / REST API | Clear docs layout for non-experts | Idea only | 6 | S |
| FI-1 | FIWARE / ETSI | NGSI-LD API facade (ETSI GS CIM 009 v1.9.1), **read-only first**: entity and temporal queries | Lets FIWARE-style systems read OrbitOS data | Spec (written from the ETSI spec, not from Orion code) | 5 (after Phase 3) | M |
| FI-2 | Smart Data Models (+ OR-1 idea) | Common domain schemas (Device, WeatherObserved, WaterQualityObserved, AgriParcel, Building, vehicle/fleet) used as asset types with typed attributes (unit, read-only, constraints) | One shared vocabulary for templates, the asset model and NGSI-LD | Reusable (CC-BY-4.0); attribution in NOTICE; **check each model's licence** | 5 | M |

## Defer

| ID | Source | Idea or practice | Why defer | License status | Effort |
|---|---|---|---|---|---|
| TB-10 | ThingsBoard | Entity version control (git sync) | Keep as-is; no work planned, not promoted in docs for non-experts | In base | — |
| TB-11 | ThingsBoard Edge | Edge node that works offline and syncs later | **Document now (Phase 3/6), build later.** It is a second full product to brand, translate and support | Reusable (Apache-2.0 at v4.3.1.6; recheck at pin) | L |
| TB-16 | ThingsBoard | AI/LLM rule nodes | Sends data to cloud LLMs (residency risk). Stays in the code but disabled; no work planned | In base | — |
| OR-4 | OpenRemote | "When-Then" form rules for non-experts | Ease-of-use win, but alarm rules in device profiles cover the simple cases for now | Idea only | L |

## Reject

| ID | Source | Idea or practice | Why reject | License status |
|---|---|---|---|---|
| TB-14 | ThingsBoard | Update checker | Calls home with a persistent instance ID. **Removed in Phase 1** | In base, remove |
| TB-15 | ThingsBoard | Community Grant enrollment and IoT Hub marketplace | Calls home; the grant flow can download and run a remote program. **Removed in Phase 1** | In base, remove |
| TB-18 | ThingsBoard | Runtime help from `thingsboard-ui-help` | Repo has **no licence** (all rights reserved) and is an outbound call. **Removed in Phase 1** | Not reusable |
| OR-1 | OpenRemote | Separate typed asset catalog | Merged into FI-2 | Idea only |
| OR-5 | OpenRemote | Alarms with assignee and severity | Already in base | n/a |
| OR-6 | OpenRemote | Manager / agent / edge-gateway split | Covered by the IoT Gateway (TB-17) and Edge (TB-11) | n/a |
| OR-7 | OpenRemote | Mobile "console" shell apps | Not in the GOAL | Idea only |
| OR-10 | OpenRemote | Insights dashboard builder | Already in base (dashboards) | n/a |
| OR-11 | OpenRemote | OpenTelemetry tracing | Extra weight; Prometheus/Grafana option exists | Idea only |
| FI-3 | FIWARE | Keyrock IdM + Wilma PEP proxy | A second auth layer adds complexity; built-in authz plus optional OIDC IdP is enough | Reusable (MIT), not needed |
| FI-4 | FIWARE | Data Space Connector | Too heavy for this project | Reusable (Apache-2.0), not needed |
| FI-5 | FIWARE IoT Agents | Adapter-skeleton pattern | No protocol gap needs our own adapter | Idea only |
| FI-6 | FIWARE | QuantumLeap time-series service | Base already stores time-series (Postgres or TimescaleDB) | Reusable (MIT), not needed |
| FI-7 | FIWARE | GDPR / consent components | None found in FIWARE core; we build consent records in Phase 4 | n/a |
