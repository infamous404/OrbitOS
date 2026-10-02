# Phase 2 status: English and French

Date: 2026-10-02. Labels: **VERIFIED** (run in this session), **UNVERIFIED** (written, not run), **NOT DONE**.
French translations are reviewed by the project owner (fr-CA).

## Acceptance: "switching language changes 100% of visible text, no mixed screens"

| Screen (tenant administrator, fr-CA) | Status | Evidence |
|---|---|---|
| Login page | VERIFIED | Switched EN-US → FR-CA with the menu; page reloads in French, `lang="fr-CA"`, "courriel" wording |
| Home page | VERIFIED | First scan found 2 English labels in the "Documentation" card; fixed with translation keys; re-scan of the new build: no English |
| Menu, devices, device profiles, alarms, dashboards, rule chains lists | VERIFIED | Automated scan of visible text: no English UI strings |
| Other screens (settings, widget editors, rule node forms, dialogs) | UNVERIFIED by eye | Covered by the key check: every English key has a French value (9,612 keys) |
| Data created by the server at tenant creation (root rule chain and node names, notification templates, default device profile name) | **NOT DONE** | Created once per tenant in English. Proposal: create this default data in `ORBITOS_DEFAULT_LANGUAGE` (Phase 3 first-run wizard chooses it) |

## Items

| Item | Status | Notes |
|---|---|---|
| Base languages en, fr with regional overrides en-CA, en-US, fr-CA, fr-FR, falling back to the base | VERIFIED | Loader merges base + OrbitOS + regional files; fr_CA holds 285 differences, en_CA 338 |
| fr-CA wording (courriel, téléverser, étiquette, Québec typography) | VERIFIED (generated, checked) | `orbitos/scripts/i18n_regional.py` + `orbitos/i18n/fr_CA.manual.json`; owner review pending |
| en-CA spelling (colour, centre, metre…) without touching HTML | VERIFIED | Bug found and fixed: first version changed CSS inside HTML |
| Dates per language (moment.js + Angular locale) | VERIFIED (locale active, `lang` and `LOCALE_ID` follow the language) | Upstream loaded no date locales at all |
| Decimal comma in French for widget values | UNVERIFIED | Code in place; no widget with data opened |
| Currency, units | NOT CHANGED | No currency display in the platform; unit system already follows the user setting or timezone (Canada → metric) |
| Language switcher, visible, saved per user and per browser, default from browser | VERIFIED (login page; saves to browser, reloads) | Saving to the profile when signed in: UNVERIFIED by click (same API as the profile page) |
| Only fully translated languages offered | VERIFIED | Menu shows the 4 languages; 26 partial upstream languages kept in the repo but not offered |
| Missing French UI strings completed | VERIFIED | 113 added, ~20 wrong "identical" values fixed, 596 identical values reviewed and allowlisted (units, technical terms) |
| Upstream French quality | PARTLY | Fixed what I saw (e.g. sign-in button said "Veuillez vous connecter"); a full review of 9,500 upstream strings is the owner's review |
| Emails in the recipient's language (10 templates, subjects, usage-limit labels) | VERIFIED | `MailTranslationsTest`: French for fr-CA and fr-FR, English unchanged, no upstream name |
| Default language for users without one (`ORBITOS_DEFAULT_LANGUAGE`) | UNVERIFIED | Config in place |
| Common server error messages translated (sign-in, permissions, password rules) | UNVERIFIED in browser | 18 messages mapped; other server messages stay in English |
| Notification templates | NOT DONE | See acceptance table |
| Help pages (non-developer) | VERIFIED (written) | 3 alarm-schedule pages in French. Not translated (Phase 6 list): notification placeholders (16 pages), date format codes, certificate hints, and all script/widget developer help |
| CI check: missing, untranslated, placeholders, unused OrbitOS keys, regional files up to date | VERIFIED locally | Unused **upstream** keys are counted, not failed (1,322 are built dynamically; deleting them would break upstream merges) |
| Hidden features excluded from the check (IoT Hub, Community Grant, Trendz) | VERIFIED | `orbitos/i18n/hidden-keys.txt` |
| "How to add a language" guide (EN, FR) | VERIFIED (written) | `orbitos/docs/{en,fr}/add-a-language.md` |
| Outbound call found: email images from `media.thingsboard.io` | VERIFIED fixed | Inline header and symbols; caught by the new email test |
