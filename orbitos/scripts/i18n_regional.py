#!/usr/bin/env python3
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
"""
Builds the regional UI string files from the base languages:

  fr_CA  = French (France) strings + Québec wording rules + orbitos/i18n/fr_CA.manual.json
  en_CA  = English (US) strings + Canadian spelling rules + orbitos/i18n/en_CA.manual.json

Only strings that differ from the base are written to
ui-ngx/src/assets/orbitos/locale/<lang>.json. Re-run after merging upstream changes.

Usage:
  python orbitos/scripts/i18n_regional.py           write the files
  python orbitos/scripts/i18n_regional.py --check   exit 1 if the files are out of date (CI)
"""
import argparse
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import i18n_check as base  # noqa: E402

I18N = base.ROOT / "orbitos/i18n"

# Never touch placeholders ({{email}}), HTML/attribute names, or identifiers.
NOT_IN_WORD = r"(?<![{\w.\-/])"
NOT_AFTER = r"(?![}\w\-/])"


def keep_case(match_text, replacement):
    return replacement[:1].upper() + replacement[1:] if match_text[:1].isupper() else replacement


# ---------- Québec French (Office québécois de la langue française usage) ----------

def fr_ca(text):
    # elided forms first: l'e-mail -> le courriel, d'e-mails -> de courriels
    def elided(m):
        article = {"l": "le", "d": "de"}[m.group(1).lower()]
        word = "courriels" if m.group(3) else "courriel"
        return keep_case(m.group(1), article) + " " + word
    text = re.sub(NOT_IN_WORD + r"([lLdD])(['’])e-?mail(s?)" + NOT_AFTER, elided, text)
    text = re.sub(NOT_IN_WORD + r"(e-?mail|mail)(s?)" + NOT_AFTER,
                  lambda m: keep_case(m.group(1), "courriel") + m.group(2), text, flags=re.IGNORECASE)
    # Québec typography: no space before ? ! ; (a space stays before the colon)
    text = re.sub(r"[   ]+([?!;])", r"\1", text)
    return text


# ---------- Canadian English ----------

EN_CA_WORDS = [
    (r"colou?r", "colour"), (r"colors", "colours"), (r"colored", "coloured"), (r"coloring", "colouring"),
    (r"center", "centre"), (r"centers", "centres"), (r"centered", "centred"),
    (r"behavior", "behaviour"), (r"behaviors", "behaviours"),
    (r"favorite", "favourite"), (r"favorites", "favourites"),
    (r"canceled", "cancelled"), (r"canceling", "cancelling"),
    (r"labeled", "labelled"), (r"labeling", "labelling"),
    (r"gray", "grey"), (r"grayscale", "greyscale"),
    (r"modeling", "modelling"), (r"traveled", "travelled"),
    (r"catalog", "catalogue"), (r"catalogs", "catalogues"),
    (r"(kilo|centi|milli|micro|nano|deci|hecto|deca)?meter", r"\1metre"),
    (r"(kilo|centi|milli|micro|nano|deci|hecto|deca)?meters", r"\1metres"),
    (r"(kilo|centi|milli|micro|deci|hecto)?liter", r"\1litre"),
    (r"(kilo|centi|milli|micro|deci|hecto)?liters", r"\1litres"),
]


def en_ca(text):
    for pattern, replacement in EN_CA_WORDS:
        regex = re.compile(NOT_IN_WORD + r"(" + pattern + r")" + NOT_AFTER, re.IGNORECASE)

        def sub(m, replacement=replacement, pattern=pattern):
            new = re.sub(r"^" + pattern + r"$", replacement, m.group(1), flags=re.IGNORECASE)
            if m.group(1).isupper():
                return new.upper()
            return keep_case(m.group(1), new.lower())
        text = regex.sub(sub, text)
    return text


REGIONS = {
    "fr_CA": ("fr_FR", fr_ca),
    "en_CA": ("en_US", en_ca),
}


TAG = re.compile(r"(<[^>]*>|&[#\w]+;)")


def outside_tags(text, rule):
    """Apply a wording rule to visible text only, never inside HTML tags, attributes or entities."""
    parts = TAG.split(text)
    return "".join(part if TAG.fullmatch(part) else rule(part) for part in parts)


def build(lang):
    base_lang, rule = REGIONS[lang]
    hidden = base.KeySet(base.patterns(base.HIDDEN_FILE))
    source = base.strings(base_lang)
    out = {}
    for key, value in source.items():
        if isinstance(value, str) and key not in hidden:
            new = outside_tags(value, rule)
            if new != value:
                out[key] = new
    manual_path = I18N / ("%s.manual.json" % lang)
    if manual_path.exists():
        out.update(json.loads(manual_path.read_text(encoding="utf-8")))
    return out


def nest(flat):
    root = {}
    for key in sorted(flat):
        node = root
        parts = key.split(".")
        for part in parts[:-1]:
            node = node.setdefault(part, {})
        node[parts[-1]] = flat[key]
    return root


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    stale = False
    for lang in REGIONS:
        content = json.dumps(nest(build(lang)), ensure_ascii=False, indent=2) + "\n"
        path = base.OVERLAY / ("%s.json" % lang)
        current = path.read_text(encoding="utf-8") if path.exists() else ""
        if current != content:
            stale = True
            if args.check:
                print("%s is out of date: run python orbitos/scripts/i18n_regional.py" % path.relative_to(base.ROOT))
            else:
                path.write_text(content, encoding="utf-8", newline="\n")
                print("Wrote %s (%d strings)" % (path.relative_to(base.ROOT).as_posix(), len(build(lang))))
    if args.check and stale:
        sys.exit(1)
    if not stale:
        print("Regional files are up to date.")


if __name__ == "__main__":
    main()
