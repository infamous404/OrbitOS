# Upstream patches

OrbitOS is a long-lived fork of ThingsBoard Community Edition. This file records where the fork
starts, how upstream fixes are taken in, and **every change made to an upstream file**, with the reason.

New OrbitOS files (under `orbitos/`, `*/orbitos/` packages and folders, root docs) are not listed
here: they never conflict with upstream.

## 1. Fork point

| Item | Value |
|---|---|
| Upstream | https://github.com/thingsboard/thingsboard |
| Tag | `v4.3.1.6` |
| Commit | `c77335f4a5` (2026-09-28) |
| Licence at that commit | Apache License 2.0 (top-level `LICENSE`, checked) |
| Branch in this repo | `main`, created from the tag |

## 2. Rules for taking upstream code (ground rule 4)

1. Upstream fixes may be merged **only from the `lts-4.3` branch or `v4.3.x` tags**.
2. **Every patch is licence-checked before merging.** Run:
   ```bash
   git fetch upstream
   python orbitos/scripts/upstream_license_check.py upstream/lts-4.3
   ```
   The check refuses any commit that is not on `lts-4.3` / a `v4.3.x` tag, whose top-level `LICENSE`
   is not Apache-2.0, that touches a file with a non-Apache SPDX identifier, or that adds Business
   Source License text.
3. **Nothing from 4.4.0 or later, `lts-4.4` or `master` is ever merged or copied.** Those are under the
   Business Source License 1.1. The `upstream` remote in this repo is set to fetch only `lts-4.3`
   and `v4.3*` tags, and pushing to it is disabled:
   ```bash
   git remote add upstream https://github.com/thingsboard/thingsboard.git
   git config --unset-all remote.upstream.fetch
   git config --add remote.upstream.fetch '+refs/heads/lts-4.3:refs/remotes/upstream/lts-4.3'
   git config --add remote.upstream.fetch '+refs/tags/v4.3*:refs/tags/v4.3*'
   git config remote.upstream.tagOpt --no-tags
   git config remote.upstream.pushurl DISABLED
   ```
4. Upstream says `lts-4.3` gets security fixes **until 2027-07-20**. After that date, or as soon as
   `lts-4.3` stops being Apache-2.0, security fixes are backported by hand (section 4).
5. If the check fails, or you are unsure, do not merge. Record the refusal in section 6 and ask for
   legal review.

## 3. How to merge an upstream patch

1. `git fetch upstream`
2. `python orbitos/scripts/upstream_license_check.py upstream/lts-4.3` must end with "All commits are Apache-2.0".
3. `git checkout -b upstream-sync-YYYY-MM-DD main` then `git merge upstream/lts-4.3`.
4. Resolve conflicts. For files listed in section 5, keep the OrbitOS change and re-apply it on top of upstream.
5. Run `python orbitos/scripts/rebrand.py` (upstream may add new strings with the old product name),
   then `python orbitos/scripts/rebrand.py --check`.
6. Build and test (`docker build -f orbitos/docker/Dockerfile .`, CI).
7. Add a line to section 6 and to `CHANGELOG.md`.

## 4. Security fixes after 2027-07-20 (manual backport)

1. Watch the CVE sources: GitHub Dependabot / `trivy` results from CI (`orbitos-security` workflow),
   and the advisories for the main dependencies listed in section 7.
2. For a vulnerable **dependency**: bump the version in `pom.xml` or `ui-ngx/package.json`, rebuild, run tests.
3. For a vulnerable **ThingsBoard code path**: write the fix ourselves. Do **not** copy the fix from
   ThingsBoard 4.4+ (BUSL). Reading a public CVE description or advisory is fine; copying BUSL code is not.
   When in doubt, get legal review before merging.
4. Log the fix in section 6 and `CHANGELOG.md`.

## 5. Changes to upstream files

Each row: file(s), what changed, why. "Deleted" means the file is removed from the fork.

### 5.1 Call-home removal (Phase 1)

| File(s) | Change | Why |
|---|---|---|
| `application/.../controller/CommunityGrantController.java`, `application/.../service/community_grant/**`, `common/data/.../community_grant/**`, `dao/.../subscription/TbClusterStore.java`, their tests and `application/src/test/resources/community-grant/**` | Deleted | The Community Grant flow contacted `license.thingsboard.io` and could download and run a signed program on the server. |
| `ui-ngx/src/app/core/http/community-grant.service.ts`, `.../home-page/community-grant-dialog.component.*`, `.../home-page/ce-grant-tokens.scss`, `ui-ngx/src/app/shared/models/ce-grant/**`, `ui-ngx/src/assets/ce-grant/**` | Deleted | UI of the same feature. |
| `.../home-page/version-info.component.{html,ts,scss}`, `.../home-page/home-page-widgets.module.ts` | Restored from `v4.3.1.5` (Apache-2.0), then the "contact us" link to the vendor and the release-notes link were removed | Same widget as before the grant feature was added. |
| `application/.../service/update/DefaultUpdateService.java` | Deleted, replaced by `org.thingsboard.server.orbitos.offline.OfflineUpdateService` | It contacted `updates.thingsboard.io` every hour with a persistent instance ID. |
| `application/src/main/resources/thingsboard.yml` | Removed the `updates:` and `community-grant:` blocks; added one line `spring.config.import: "optional:classpath:orbitos.yml"` | The removed blocks configured deleted features. All other OrbitOS defaults live in `orbitos.yml`, so this file stays close to upstream. |
| `application/.../service/iot_hub/IotHubRestClient.java` | Refuses every request when `iot-hub.base-url` is empty (the OrbitOS default) | No outbound calls to the IoT Hub marketplace. The feature is kept, off, because it is wired into ~130 UI files that upstream still patches; deleting it would make every upstream merge conflict. |
| `ui-ngx/src/app/core/services/menu.models.ts` | IoT Hub menu shown only if a base URL is configured; Trendz settings menu hidden | IoT Hub off by default; Trendz is a separate commercial product. |
| `ui-ngx/.../iot-hub/iot-hub-actions.service.ts` | Added `enabled` (true only if a base URL is configured) | One switch for all IoT Hub entry points. |
| `devices-table-config.resolver.ts`, `rulechains-table-config.resolver.ts`, `widget-types-table-config.resolver.ts`, `alarm-rules-table-config.ts`, `calculated-fields-table-config.ts` | "Add from IoT Hub" action enabled only if IoT Hub is enabled | Hide entry points. |
| `ui-ngx/.../dashboard-page/dashboard-widget-select.component.{ts,html}` | IoT Hub tab and "browse IoT Hub" tip hidden when IoT Hub is off | Hide entry points. |
| `ui-ngx/src/assets/dashboard/tenant_admin_home_page.json` | Removed the IoT Hub card; cards below moved up 15 rows | Hide entry points. |
| `ui-ngx/src/app/modules/home/home.component.html` | Removed the GitHub star badge | It called `api.github.com` from every user's browser. |
| Help pages | `ui.help.base-url` defaults to `/assets` (in `orbitos.yml`, no upstream file change) | Help came from `raw.githubusercontent.com/thingsboard/thingsboard-ui-help`, a repository with no licence. |
| Edge support | `edges.enabled` defaults to `false` (in `orbitos.yml`) | Opens a gRPC port and shows menus for a separate product (deferred, harvest item TB-11). |

Left in place on purpose (inert):

- `ActionType.COMMUNITY_GRANT_*` enum values (server and UI) and their translation keys: existing audit-log rows from an upstream database may use them.
- `ThingsboardSecurityConfiguration` payload limit for `/api/communityGrant/offline/run`: the path no longer exists.
- Table `tb_cluster` and the cluster id written at install (`SqlEntityDatabaseSchemaService`, `V4_3_1_6Migration`): local data only, keeps the database schema identical to upstream 4.3.1.6 so upgrades and migrations still work.
- The `ce-grant-waiting` icon in `icon.models.ts`.

### 5.2 Branding (Phase 1)

| File(s) | Change | Why |
|---|---|---|
| `ui-ngx/src/main.ts` | Loads runtime branding before Angular starts | Name, logo and favicon come from `branding.yml` on the server. |
| `ui-ngx/src/app/core/translate/translate-default-loader.ts` | Merges `assets/orbitos/locale/<lang>.json` over upstream strings | OrbitOS strings live in their own files. |
| `ui-ngx/src/app/core/translate/translate-default-parser.ts` | `{{brandName}}` resolves to the configured name in every string | Runtime brand name in all UI text. |
| `ui-ngx/src/app/core/services/help.service.ts` | Same placeholder in help pages | Runtime brand name in help. |
| `ui-ngx/src/app/shared/components/logo.component.ts`, `ui-ngx/src/app/modules/home/home.component.ts` | Default logo from branding | Runtime logo. |
| `ui-ngx/src/app/modules/login/pages/login/login.component.{html,ts}`, `ui-ngx/src/app/modules/login/login.module.ts` | Logo links to the configured website (or nowhere); adds title, subtitle, footer text and legal links | Branded login page; privacy link (Law 25). |
| `ui-ngx/.../dashboard-page/dashboard-page.component.html` | "Powered by" uses the brand name, no vendor link or version | Branding; version is not exposed on public dashboards. |
| `ui-ngx/.../admin/two-factor-auth-settings.component.ts` | Default authenticator issuer = brand name | Branding. |
| `ui-ngx/src/app/app.component.ts` | Console banner uses the brand name | Branding. |
| `ui-ngx/.../dashboard/dashboard-form.component.html`, `make-dashboard-public-dialog.component.html` | Social share hashtag `thingsboard` removed | Branding. |
| `ui-ngx/src/app/shared/components/footer.component.{html,ts}` | Copyright line uses the brand name (component is unused upstream) | Branding. |
| `ui-ngx/src/index.html` | Default title and favicon | Shown before branding loads. Title is set by `rebrand.py`. |
| `ui-ngx/src/scss/constants.scss`, `ui-ngx/src/theme.scss` | Colors and palettes come from the generated `_orbitos-brand.scss` | Brand colors from `branding.yml`. |
| `ui-ngx/src/assets/logo_title_white.svg`, `ui-ngx/src/assets/logo_white.svg` | Replaced by the brand logo (by `rebrand.py`) | Built-in fallback logo. |
| `ui-ngx/src/assets/locale/*.json` (27 files), `ui-ngx/src/assets/help/en_US/**` (5 files) | Product name replaced by `{{brandName}}` (by `rebrand.py`) | Runtime brand name. |
| `application/src/main/resources/templates/*.ftl` (10 files) | Product name replaced by `${brandName}` (by `rebrand.py`) | Runtime brand name in emails. |
| `application/src/main/resources/i18n/messages.properties` | Product name replaced by `{brandName}` (by `rebrand.py`) | Runtime brand name in email subjects. |
| `README.md`, `security.md` → `SECURITY.md` | Replaced by OrbitOS versions (bilingual SECURITY, `README.fr.md` added) | Upstream text describes the upstream product and its vendor contacts. |
| `.gitignore` | Ignores `orbitos/docker/.env` and smoke-test logs | Secrets must never be committed. |
| `img/logo.png` | Deleted (unused) | Upstream trademark logo; Apache-2.0 gives no trademark rights. |
| `application/src/main/data/json/system/**` (7 files) | Product name replaced by the brand name; "ThingsBoard IoT Gateway" → "IoT Gateway", "ThingsBoard Edge" → "Edge" (by `rebrand.py`) | Built-in widget descriptions. Written into the database at install, so a later brand change needs a reinstall or data upgrade for these 7 texts. |

### 5.3 Languages: English and French (Phase 2)

New OrbitOS files (no conflict risk): `ui-ngx/src/app/orbitos/{i18n,translations,server-messages,language-switcher.component}.ts`,
`ui-ngx/src/assets/orbitos/locale/*.json`, `ui-ngx/src/assets/orbitos/languages.json`, `orbitos/i18n/*`,
`application/src/main/resources/i18n/messages_fr.properties`, `application/src/main/resources/templates/*_fr.ftl`,
`ui-ngx/src/assets/help/fr_FR/**`, `application/.../orbitos/i18n/MailLocaleResolver.java`.

| File(s) | Change | Why |
|---|---|---|
| `ui-ngx/esbuild/tb-esbuild-plugins.ts` | Languages offered = `assets/orbitos/languages.json` (en_US, en_CA, fr_FR, fr_CA) | Only fully translated languages; no mixed screens. Other upstream locale files stay in the repo. |
| `ui-ngx/src/app/core/translate/translate-default-loader.ts` | Loads base language + OrbitOS strings + regional differences | fr_CA/en_CA only hold what differs. |
| `ui-ngx/src/app/core/settings/settings.utils.ts` | Remembers the language in the browser, uses it before the browser default, reloads once when date/number formats must change | Language saved per user and per browser; dates follow the language. |
| `ui-ngx/src/app/core/core.module.ts` | `LOCALE_ID` from the language of the page load | French dates and numbers. |
| `ui-ngx/src/app/core/utils.ts` (`formatValue`), `ui-ngx/src/app/shared/models/widget-settings.models.ts` (`ValueFormatProcessor.formatValue`) | Decimal separator per language (`12,5` in French) | Numbers in the user's language. Display only. Custom widget code that parses `ctx.utils.formatValue()` output must not assume a dot. |
| `ui-ngx/src/app/core/interceptors/global-http-interceptor.ts` | Error messages pass through `translateServerMessage` | Common server messages (sign-in, permissions, password rules) in the user's language. |
| `ui-ngx/src/app/core/services/help.service.ts` | Regional language falls back to its base language's help (fr_CA → fr_FR) | Help pages shared by both French variants. |
| `ui-ngx/src/app/modules/home/home.component.html`, `home.module.ts`, `login.component.html`, `login.module.ts` | Language switcher in the top bar and on the login page | Visible language choice. |
| `application/.../service/mail/DefaultMailService.java` | Subjects and templates in the recipient's language (was `Locale.US`); usage-limit labels from `messages*.properties` with the old English text as default | French emails. English output unchanged. |
| `application/src/main/resources/templates/*.ftl` | "by Thingsboard." → `${brandName}` (the Phase 1 name pattern missed a name followed by a full stop) | Branding fix. |
| `application/src/main/resources/templates/state.{enabled,warning,disabled}.ftl` | Remote images from `media.thingsboard.io` replaced by an inline brand header and text symbols | **Outbound call found in Phase 2:** opening these emails loaded images from the vendor's server (and showed its logo). |
| `ui-ngx/.../home-page/doc-links-widget.component.ts` | Default "Documentation" link names are translation keys | Home page fully French. The links still point to upstream online docs until Phase 6. |

### 5.4 Setup, secure defaults, offline (Phase 3)

| File(s) | Change | Why |
|---|---|---|
| `application/.../service/install/DefaultSystemDataLoaderService.java` (`createSysAdmin`) | The system administrator is created as `sysadmin@setup.invalid` with an unknown random password, instead of `sysadmin@thingsboard.org` / `sysadmin` | No default password. The first-run setup page (`org.thingsboard.server.orbitos.setup`) gives it a real login. |
| `application/.../service/install/InstallScripts.java` (`createRuleChainFromFile`) | Rule chain files pass through `DefaultDataTranslator` before they are saved | Default rule chains in the platform language (French node names). English output unchanged. |
| `application/.../service/entitiy/tenant/DefaultTbTenantService.java` (`save`) | After a new organization is created, `DefaultDataTranslator.translateNewTenant` translates its default notification templates, rules and recipient groups | Default notifications in the platform language. Exact-text dictionary; anything without an entry stays in English, so upstream changes cannot break tenant creation. |
| `ui-ngx/src/app/modules/login/login-routing.module.ts` | Adds the `/setup` route | First-run setup page. |
| `ui-ngx/src/app/modules/login/pages/login/login.component.ts` | Sends visitors to `/setup` while the server has no account | A new server cannot be signed into before setup. |
| `ui-ngx/src/styles.scss` | Imports `scss/_orbitos-accents.scss` | Small visual accents (thin cyan lines, monospace for codes). Text colors unchanged. |
| `.gitignore`, `.dockerignore` | Ignore `.env` and `backups/` at the repository root | Secrets and backups never committed or sent to the image build. |

New files only (no upstream change): `compose.yaml`, `.env.example`, `orbitos/docker/{Caddyfile,tools/backup.sh,tools/restore.sh}`, the
setup service and page, `ContentSecurityPolicyFilter` (offline browser policy), `DatabaseState` (first-start check),
`orbitos/docs/{en,fr}/install.md`. The old development compose file `orbitos/docker/compose.yml` was removed.

### 5.5 Privacy and security essentials (Phase 4)

| File(s) | Change | Why |
|---|---|---|
| `application/.../service/security/system/DefaultSystemSecurityService.java` (`validateUserCredentials`) | If the password is still the one generated by the platform (`ForcedPasswordChange`), answer "password expired" with a reset token | Forced change of the platform administrator's generated password at the first sign-in. Reuses the upstream expired-password page. |
| `ui-ngx/.../pages/user/users-table-config.resolver.ts` | Adds two row actions from `OrbitosPrivacyService`: export personal data, erase account | Privacy requests (access, erasure). |

Default changes without upstream edits (in `orbitos.yml`): `audit-log.sink.type=orbitos-chain`
(hash chain), retention `sql.ttl.ts.ts_key_value_ttl=365 d`, `sql.ttl.events.events_ttl=30 d`,
`sql.ttl.audit_logs.ttl=365 d`. Applied by the setup page (`SecureDefaults`): two-factor providers
(authenticator app, backup codes), 12-character passwords, lockout after 10 failures, organization
profile retention 365/365/30 days. The installer and upgrader run with `audit-log.sink.type=none`.

### 5.6 Interoperability (Phase 5)

No upstream file changed. The API reference page is rebranded at runtime by
`org.thingsboard.server.orbitos.branding.OpenApiBranding` (same name rule as `rebrand.py`), and its
title, contact and licence link come from `swagger.*` defaults in `orbitos.yml`. Upstream pointed the
licence link at its `master` branch, which is no longer Apache-2.0; it now points to the Apache licence text.

### 5.7 Docs, add-ons, final clean-up (Phase 6)

| File(s) | Change | Why |
|---|---|---|
| `ui-ngx/src/main.ts` | Calls `installDocLinkRedirect()` | About 150 help links pointed to the upstream product's website. They now open the OrbitOS guides on this server (`/docs`), mapped by topic. One hook instead of editing each link. |
| `ui-ngx/src/app/modules/login/login-routing.module.ts` | Adds the `/docs` routes | In-app documentation page (works offline, signed in or not). |
| `ui-ngx/src/assets/dashboard/sys_admin_home_page.json` | "Functions" card: removed the "Switch to PE" button (link to the vendor's pricing page) and the paid-edition features; lists OrbitOS features instead | No advertising for another product. |
| `.github/ISSUE_TEMPLATE/*`, `pull_request_template.md` | Replaced by OrbitOS templates (EN/FR) | Upstream templates named the vendor's services and assigned issues to a vendor maintainer. |

Default changes without upstream edits: `mobileApp.googlePlayLink` and `appStoreLink` empty
(`orbitos.yml`); the setup page turns off the home-page QR card that advertised the vendor's mobile app.
New files only: gateway add-on (`orbitos/gateway/`), MQTT TLS certificate creation (`entrypoint.sh`),
`DocsController`, docs page, French help pages generated by `orbitos/i18n/help_fr.py`.

### 5.8 After version 1: logo, look, fixes (2026-10-03)

| File | Change | Why |
|---|---|---|
| `application/src/main/resources/banner.txt` | OrbitOS banner instead of the upstream one | The upstream product name was printed at every start. |
| `application/.../install/ThingsboardInstallService.java` | 4 log lines: product name removed ("Starting installation...") | Same: logs read by administrators. |
| `application/.../ThingsboardServerApplication.java` | 1 log line: "Started OrbitOS in N seconds" | Same. |
| `application/src/main/resources/templates/state.*.ftl` (6 files) | Email header color: new brand color `#0F2347` | New logo and colors. |
| `ui-ngx/src/app/shared/components/logo.component.ts` | No crash when the logo is shown before sign-in (link to `/`) | Browser console error on the sign-in, setup and docs pages. |
| `ui-ngx/src/assets/logo_title_white.svg`, `logo_white.svg` | New logo (written by `rebrand.py`) | New logo. |
| `docker/README.md`, `msa/tb/README.md`, `msa/black-box-tests/README.md` | Replaced by short notes: not used by OrbitOS, see the OrbitOS guides | They named the upstream product and described its default password. |

OrbitOS files only: new logo (`orbitos/branding/`), colors (`branding.yml`), cyber accents
(`_orbitos-accents.scss`), language switcher fix, CoAP test client, OPC UA simulator.



| Date | Upstream ref | Commits | Licence check | Notes |
|---|---|---|---|---|
| 2026-10-02 | fork at `v4.3.1.6` | — | passed | Initial fork. |
| — | `upstream/lts-4.3` (2 commits after the fork point) | 2 | passed (2026-10-02) | Not merged yet: header-template move and an lts-4.2 merge. To be merged in a later sync. |

## 7. CVE watch list (main dependencies)

Spring Boot 3.5.x / Spring Framework 6.2.x, Netty 4.1.x, Jackson 2.21.x, PostgreSQL JDBC driver,
Apache Kafka client, Eclipse Californium (CoAP), Eclipse Leshan (LwM2M), Netty MQTT, FreeMarker,
ZooKeeper, gRPC/Protobuf, Angular 20.x, Angular Material, RxJS, jQuery, ACE editor, maplibre-gl,
TinyMCE (if present), Node.js 22 (build only), the `eclipse-temurin:17-jre` and `postgres:16` images.
CI scans the full dependency tree; this list is for people reading advisories by hand.
