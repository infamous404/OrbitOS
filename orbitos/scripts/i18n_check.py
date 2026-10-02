#!/usr/bin/env python3
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
"""
Translation check for the OrbitOS UI.

How UI strings are built (same rules as the UI loader, ui-ngx/src/app/orbitos/i18n.ts):
  base file      ui-ngx/src/assets/locale/locale.constant-<base>.json   (upstream)
  + overlay      ui-ngx/src/assets/orbitos/locale/<base>.json            (OrbitOS strings and fixes)
  + regional     ui-ngx/src/assets/orbitos/locale/<lang>.json            (only if lang != base)
where <base> is en_US for English and fr_FR for French.

Checks (exit 1 if any fails):
  missing       a key exists in English but not in another language
  untranslated  a French value is identical to English and is not in orbitos/i18n/identical-allowlist.txt
  placeholders  a translation uses different {{placeholders}} than English
  orphans       an OrbitOS overlay or regional file has a key that English does not have
  unused        an OrbitOS-owned key (orbitos.*) is not used anywhere in the UI code
Upstream keys that look unused are only counted (many are built dynamically, and removing them
from upstream files would make every upstream merge conflict).

Usage:
  python orbitos/scripts/i18n_check.py                   run all checks
  python orbitos/scripts/i18n_check.py --export FILE     write missing/untranslated French keys to FILE (JSON) for translation
"""
import argparse
import fnmatch
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LOCALE = ROOT / "ui-ngx/src/assets/locale"
OVERLAY = ROOT / "ui-ngx/src/assets/orbitos/locale"
LANGUAGES_FILE = ROOT / "ui-ngx/src/assets/orbitos/languages.json"
ALLOWLIST_FILE = ROOT / "orbitos/i18n/identical-allowlist.txt"
HIDDEN_FILE = ROOT / "orbitos/i18n/hidden-keys.txt"
BASES = {"en": "en_US", "fr": "fr_FR"}
PLACEHOLDER = re.compile(r"\{\{\s*([\w.]+)\s*\}\}")


def flatten(obj, prefix=""):
    out = {}
    for key, value in obj.items():
        full = prefix + "." + key if prefix else key
        if isinstance(value, dict):
            out.update(flatten(value, full))
        else:
            out[full] = value
    return out


def read(path):
    return json.loads(path.read_text(encoding="utf-8")) if path.exists() else {}


def strings(lang):
    base = BASES[lang.split("_")[0]]
    result = flatten(read(LOCALE / ("locale.constant-%s.json" % base)))
    result.update(flatten(read(OVERLAY / ("%s.json" % base))))
    if lang != base:
        result.update(flatten(read(OVERLAY / ("%s.json" % lang))))
    return result


def patterns(path):
    if not path.exists():
        return []
    return [line.split("#")[0].strip() for line in path.read_text(encoding="utf-8").splitlines()
            if line.split("#")[0].strip()]


class KeySet:
    """Exact keys plus glob patterns."""

    def __init__(self, items):
        self.exact = {i for i in items if not any(ch in i for ch in "*?[")}
        self.globs = [i for i in items if i not in self.exact]

    def __contains__(self, key):
        return key in self.exact or any(fnmatch.fnmatch(key, g) for g in self.globs)


def allowlist():
    return KeySet(patterns(ALLOWLIST_FILE))


def ui_sources():
    for pattern in ("ui-ngx/src/app/**/*.ts", "ui-ngx/src/app/**/*.html", "ui-ngx/src/assets/**/*.json"):
        for path in ROOT.glob(pattern):
            if "/locale/" not in path.as_posix():
                yield path.read_text(encoding="utf-8", errors="replace")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--export", help="write French keys needing translation to this JSON file")
    args = parser.parse_args()

    languages = json.loads(LANGUAGES_FILE.read_text(encoding="utf-8"))
    hidden = KeySet(patterns(HIDDEN_FILE))
    english = {k: v for k, v in strings("en_US").items() if k not in hidden}
    allowed_identical = allowlist()
    problems = []

    for lang in languages:
        if lang == "en_US":
            continue
        values = strings(lang)
        missing = [k for k in english if k not in values]
        problems += ["%s missing: %s" % (lang, k) for k in missing]
        for key, en_value in english.items():
            value = values.get(key)
            if value is None or not isinstance(value, str):
                continue
            if set(PLACEHOLDER.findall(value)) != set(PLACEHOLDER.findall(en_value)):
                problems.append("%s placeholders differ: %s" % (lang, key))
            if lang.startswith("fr") and value == en_value and key not in allowed_identical \
                    and re.search(r"[A-Za-z]{2}", en_value):
                problems.append("%s untranslated: %s = %r" % (lang, key, en_value[:60]))

    # orphans in OrbitOS files
    for path in sorted(OVERLAY.glob("*.json")):
        for key in flatten(read(path)):
            if key not in english:
                problems.append("orphan key in %s: %s" % (path.relative_to(ROOT).as_posix(), key))

    # unused OrbitOS keys, and a count of upstream keys not found literally
    source = "\n".join(ui_sources())
    for key in english:
        if key.startswith("orbitos.") and key not in source:
            problems.append("unused OrbitOS key: %s" % key)
    upstream_not_found = sum(1 for key in english if not key.startswith("orbitos.") and key not in source)

    if args.export:
        french = strings("fr_FR")
        todo = {k: english[k] for k in english
                if k not in french or (french[k] == english[k] and k not in allowed_identical
                                        and re.search(r"[A-Za-z]{2}", english[k]))}
        Path(args.export).write_text(json.dumps(todo, ensure_ascii=False, indent=2), encoding="utf-8")
        print("Wrote %d keys to %s" % (len(todo), args.export))
        return

    for problem in problems:
        print(problem)
    print("\n%d English keys; languages: %s" % (len(english), ", ".join(languages)))
    print("%d upstream keys not found literally in UI code (info only: many are built dynamically)"
          % upstream_not_found)
    print("Translation check: %s (%d problem(s))" % ("FAILED" if problems else "passed", len(problems)))
    sys.exit(1 if problems else 0)


if __name__ == "__main__":
    main()
