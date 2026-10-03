# Phase 5 status: interoperability (trimmed)

Date: 2026-10-03. Labels: **VERIFIED** (run in this session), **UNVERIFIED** (written, not run), **NOT DONE**.
Scope (owner, 2026-10-03): documented APIs, one real protocol verified end to end, gateways documented
only. Everything else from the original Phase 5 list is **DEFERRED**.

Evidence: `orbitos/scripts/smoke-test.sh` (30 end-to-end checks on a fresh install, all passed),
25 unit tests (all passed), WebSocket checked from the browser.

| Item | Status | Evidence |
|---|---|---|
| **Acceptance: one real protocol end to end — MQTT** | VERIFIED | Device publishes over MQTT → value read back through the REST API |
| Two-way command: REST API → platform → MQTT device → answer back to the REST caller | VERIFIED | Smoke test (`getStatus` with parameters echoed back) |
| Unknown device token refused (MQTT and HTTP) | VERIFIED | MQTT: connection refused, code 5 (manual run); HTTP: smoke test |
| HTTP device API | VERIFIED | Smoke test and setup page "Send a test value" |
| CoAP device API | UNVERIFIED | Upstream feature, documented; no CoAP client in the test |
| WebSocket live updates (`/api/ws`) | VERIFIED | Browser: current values, then a new value pushed live |
| REST API reference page (`/swagger-ui.html`), offline, rebranded | VERIFIED | Smoke test: 448 endpoints, title "OrbitOS REST API", product name replaced at runtime (2,635 mentions); browser: no external requests |
| Device simulator `orbitos/scripts/mqtt_device.py` (Python standard library only) | VERIFIED | Used by the smoke test for both directions |
| Guides: Connect devices, APIs and webhooks (EN, FR) | VERIFIED (written) | `orbitos/docs/{en,fr}/connect-devices.md`, `api.md` |
| Outgoing webhooks (rule chain "REST API call" node) | UNVERIFIED | Documented |
| LoRaWAN through ChirpStack | UNVERIFIED (documented only) | No LoRaWAN hardware; by design not bundled |
| Modbus / OPC UA / BACnet through the IoT Gateway | UNVERIFIED (documented only) | No industrial hardware; by design not bundled |
| MQTT over TLS (8883) | NOT DONE | Documented as not set up yet |
| Common asset/attribute data model (Smart Data Models inspired) | DEFERRED | Scope freeze |
| NGSI-LD API facade (harvest FI-1) | DEFERRED | Scope freeze |
| Adapter skeletons | DEFERRED | No gap found that MQTT, HTTP, CoAP or the IoT Gateway does not cover |

Upstream files changed in this phase: none (new class `OpenApiBranding`, defaults in `orbitos.yml`).
