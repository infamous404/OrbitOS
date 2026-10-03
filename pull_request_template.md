## What and why / Quoi et pourquoi

## Checklist

- [ ] New code is under an `orbitos` folder, or the upstream file change is small and listed in `UPSTREAM_PATCHES.md`.
- [ ] Only Apache-2.0 or other permissive code; any reuse is recorded in `NOTICE` (`python orbitos/scripts/upstream_license_check.py --tree`).
- [ ] Text in English **and** French (`python orbitos/scripts/i18n_check.py`), plain language.
- [ ] No new outbound network call, or it is off by default and documented.
- [ ] Tests: unit test and/or a check in `orbitos/scripts/smoke-test.sh`.
- [ ] Docs updated in `orbitos/docs/en` and `orbitos/docs/fr`; `CHANGELOG.md` updated.
