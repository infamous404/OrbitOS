# Add a language

*[Version française](../fr/add-a-language.md)*

OrbitOS offers English and French, each with a Canadian variant. This page explains how the
language files fit together and how to add one.

## How languages are built

| Layer | File | Holds |
|---|---|---|
| 1. Base | `ui-ngx/src/assets/locale/locale.constant-<base>.json` | All upstream strings (`en_US`, `fr_FR`, …) |
| 2. OrbitOS | `ui-ngx/src/assets/orbitos/locale/<base>.json` | OrbitOS strings and fixes |
| 3. Region | `ui-ngx/src/assets/orbitos/locale/<lang>.json` | Only what differs (`fr_CA`, `en_CA`) |

Later layers win. A missing string falls back to the base language, then to English.

The languages offered in the menu are listed in `ui-ngx/src/assets/orbitos/languages.json`.

## Add a regional variant (example: French for Belgium, `fr_BE`)

1. Add `"fr_BE"` to `ui-ngx/src/assets/orbitos/languages.json`.
2. Create `ui-ngx/src/assets/orbitos/locale/fr_BE.json` with only the strings that differ, for example
   `{ "orbitos": { "powered-by": "Propulsé par {{brandName}}" } }`.
3. Add its name in `en_US.json` and `fr_FR.json` under `language.locales` (`"fr_BE": "français (Belgique)"`).
4. Dates and numbers: add the Angular locale in `ui-ngx/src/app/orbitos/i18n.ts`
   (`registerLocaleData` and `angularLocale`) and the moment.js locale import.
5. Run `python orbitos/scripts/i18n_check.py`. It must say "passed".

To generate the variant from rules (as for `fr_CA` and `en_CA`), add a rule to
`orbitos/scripts/i18n_regional.py` and run it.

## Add a new language (example: Spanish, `es_ES`)

1. An upstream file `locale.constant-es_ES.json` may already exist; if not, copy the English file and translate it.
2. Add `"es_ES"` to `languages.json`, and `"es": "es_ES"` to `BASE_LANGUAGES` in `ui-ngx/src/app/orbitos/i18n.ts`
   and `BASES` in `orbitos/scripts/i18n_check.py`.
3. Create `ui-ngx/src/assets/orbitos/locale/es_ES.json` with the OrbitOS strings (copy `en_US.json` and translate).
4. Emails: copy each `application/src/main/resources/templates/<name>.ftl` to `<name>_es.ftl` and translate
   the text (keep everything between `${` and `}`). Add `application/src/main/resources/i18n/messages_es.properties`.
5. Help pages (optional): put translated pages in `ui-ngx/src/assets/help/es_ES/`, same folders as `en_US`.
6. Run `python orbitos/scripts/i18n_check.py` and fix every line it reports.
   Strings that are correctly identical to English (units, product names) go in `orbitos/i18n/identical-allowlist.txt`.

## Rules for translators

- Keep placeholders exactly: `{{name}}`, `{brandName}`, `${brandName}`. The check fails if they differ.
- Do not translate HTML tags or attributes.
- French for Canada follows the Office québécois de la langue française: *courriel*, *téléverser*, no space before `? ! ;`.
