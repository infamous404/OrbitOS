# After version 1: logo, look, fixes and extra checks

Date: 2026-10-03. Labels: **VERIFIED** (run in this session), **UNVERIFIED** (written, not run),
**PARTLY**, **NOT DONE**.

Requested by the owner after testing version 1 on their PC.

| Item | Status | Evidence |
|---|---|---|
| Language menu after sign-in did not change the language | VERIFIED fixed | Cause: the page reloaded before the choice was saved, so the old profile language won. Browser: EN-US → FR-CA → EN-CA, each one kept after the reload |
| New logo (vector redraw of the owner's PNG): top bar, sign-in page, browser tab, README | VERIFIED | Browser and headless screenshots. PNG copies (transparent): `orbitos/branding/extra/` |
| New colors: navy `#0F2347`, blue `#1D5BFF`, cyan `#0891B2` | VERIFIED | `rebrand.py`: white text on the main color 15:1 |
| Subtle "cyber" look: dark side menu with faint grid, cyan marker on the active page, glow line under the top bar, small cyan tick on home cards, dark sign-in page with grid and glow, glow on button hover | VERIFIED | Browser (home, sign-in). Decoration only: no text color made lighter |
| Visible keyboard focus ring (cyan, 3.7:1) | VERIFIED (style present) | Not tested with every control |
| OrbitOS startup banner and log lines instead of the upstream name | VERIFIED | Server log: banner, "Started OrbitOS in 48 seconds" |
| Unfinished-install message gives the exact command (`docker compose down -v`) | VERIFIED (code) | Message changed; the unfinished state itself not re-created |
| Browser console error on the sign-in, setup and docs pages | VERIFIED fixed | Logo link before sign-in |
| CoAP devices | VERIFIED | New `orbitos/scripts/coap_device.py` (Python standard library); smoke test: value stored, unknown token refused |
| OPC UA through the gateway add-on | VERIFIED | Simulated machine (`orbitos/gateway/opcua_simulator.py`) → gateway → OrbitOS, values received. Optional check `ORBITOS_TEST_OPCUA=1` (needs internet once: the OPC UA library is LGPL and not bundled) |
| BACnet | UNVERIFIED | No simulator |
| Upstream developer files naming the upstream product (`docker/README.md`, `msa/tb/README.md`, `msa/black-box-tests/README.md`, `SECURITY.md`) | VERIFIED | Replaced by short OrbitOS notes. `TEST_FAST.md` only contains Java package names (code, not branding) |
| Edge install instructions (name the upstream Edge product) | NOT DONE | Only shown if Edge is turned on; Edge is off by default and deferred |
| Accessibility check | PARTLY | Automated contrast check of all visible text on sign-in, home, devices, dashboards, rule chains, alarms: 4 upstream problems fixed (notification badge, home usage labels and icons, inactive tab labels); missing screen-reader label added (show password); logo now read as "OrbitOS". Not done: screen-reader walk-through, every dialog and widget, a full WCAG 2.1 AA audit |
| Unit tests and end-to-end test | see below | |

## Test run (2026-10-03, fresh install, rebuilt image)

`ORBITOS_TEST_OPCUA=1 bash orbitos/scripts/smoke-test.sh`: **33 checks, all passed** (31 before,
plus CoAP and OPC UA). Unit tests: 25, 0 failures. Translation, French help and branding checks: pass.
