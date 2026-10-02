#!/usr/bin/env python3
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
"""
OrbitOS rebrand script.

Reads orbitos/branding/branding.yml and:
  1. replaces the upstream product name in user-visible files with a placeholder
     ({{brandName}}, ${brandName} or {brandName}) that is filled in at runtime,
     or with the brand name itself where nothing runs at display time (built-in widget data);
  2. copies the logo and favicon into the UI's built-in assets (used before the server answers);
  3. generates the UI color palette (ui-ngx/src/scss/_orbitos-brand.scss) from the brand colors;
  4. sets the default page title in ui-ngx/src/index.html.

Running it twice changes nothing the second time (idempotent).

Usage (from the repository root):
  python orbitos/scripts/rebrand.py            apply changes
  python orbitos/scripts/rebrand.py --dry-run  show what would change, change nothing
  python orbitos/scripts/rebrand.py --check    exit 1 if anything would change or a stray
                                               upstream name is visible (for CI)

Requires PyYAML: pip install pyyaml
"""
import argparse
import colorsys
import fnmatch
import re
import shutil
import sys
from pathlib import Path

try:
    import yaml
except ImportError:
    sys.exit("PyYAML is required: pip install pyyaml")

ROOT = Path(__file__).resolve().parents[2]
BRANDING_DIR = ROOT / "orbitos" / "branding"

# The upstream product name as a word. Not matched inside URLs, e-mail addresses, file names,
# package names or identifiers (thingsboard.io, org.thingsboard, flutter_thingsboard_app, tbVersion...).
PRODUCT_NAME = re.compile(r"(?<![\w./@:-])(?:ThingsBoard|Thingsboard|THINGSBOARD)(?![\w@/-]|\.\w)")

# Upstream companion products keep a neutral name instead of taking the brand name.
NEUTRAL_NAMES = [
    (re.compile(r"(?<![\w./@:-])(?:ThingsBoard|Thingsboard) IoT Gateway\b"), "IoT Gateway"),
    (re.compile(r"(?<![\w./@:-])(?:ThingsBoard|Thingsboard) Edge\b"), "Edge"),
]

# Lines that are attribution and must keep the upstream name.
KEEP_LINE = re.compile(r"SPDX-|Copyright The Thingsboard Authors")

# Files that must never be rewritten (attribution and upstream records).
ALLOWLIST = [
    "LICENSE", "LICENSES/*", "NOTICE", "UPSTREAM_PATCHES.md", "CHANGELOG.md",
    "README.md", "README.fr.md", "security.md", "SECURITY.md",
    # Edge install instructions name the real upstream Edge product (Edge support is off by default).
    "application/src/main/data/json/edge/*",
]

LITERAL = object()  # replace with the brand name itself

# (description, glob patterns, replacement)
RULES = [
    ("UI translations", ["ui-ngx/src/assets/locale/*.json"], "{{brandName}}"),
    ("UI help pages", ["ui-ngx/src/assets/help/**/*.md"], "{{brandName}}"),
    ("Email templates", ["application/src/main/resources/templates/*.ftl"], "${brandName}"),
    ("Server messages", ["application/src/main/resources/i18n/messages*.properties"], "{brandName}"),
    ("Built-in widgets and settings", ["application/src/main/data/json/**/*.json",
                                       "application/src/main/data/json/**/*.md"], LITERAL),
]

# Extra places checked (not rewritten) for visible upstream names: UI templates and code strings.
CHECK_ONLY = ["ui-ngx/src/app/**/*.html", "ui-ngx/src/app/**/*.ts", "ui-ngx/src/index.html"]
CHECK_SKIP_LINE = re.compile(r"^\s*(//|\*|/\*)|SPDX-")
# Known, accepted mentions in UI code (developer-facing comments in strings, model names).
CHECK_ALLOW = [
    ("ui-ngx/src/app/modules/home/components/iot-hub/*", None),  # IoT Hub is off and hidden
    ("ui-ngx/src/app/shared/models/iot-hub/*", None),
]

GENERATED_SCSS = ROOT / "ui-ngx/src/scss/_orbitos-brand.scss"
INDEX_HTML = ROOT / "ui-ngx/src/index.html"
ASSETS = {
    "logo": [ROOT / "ui-ngx/src/assets/logo_title_white.svg", ROOT / "ui-ngx/src/assets/logo_white.svg"],
    "favicon": [ROOT / "ui-ngx/src/assets/orbitos/favicon.svg"],
}


def rel(path):
    return path.relative_to(ROOT).as_posix()


def allowlisted(path):
    r = rel(path)
    return any(fnmatch.fnmatch(r, pattern) for pattern in ALLOWLIST)


def files_for(patterns):
    seen = set()
    for pattern in patterns:
        for path in sorted(ROOT.glob(pattern)):
            if path.is_file() and path not in seen and not allowlisted(path):
                seen.add(path)
                yield path


def replace_names(text, replacement):
    out, count = [], 0
    for line in text.splitlines(keepends=True):
        if KEEP_LINE.search(line):
            out.append(line)
            continue
        new = line
        for pattern, neutral in NEUTRAL_NAMES:
            new, n = pattern.subn(neutral, new)
            count += n
        new, n = PRODUCT_NAME.subn(lambda _: replacement, new)
        out.append(new)
        count += n
    return "".join(out), count


# ---------- colors ----------

def hex_to_rgb(value):
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))


def rgb_to_hex(rgb):
    return "#%02x%02x%02x" % tuple(max(0, min(255, round(c))) for c in rgb)


def mix(a, b, weight):
    """weight of b in the mix (0..1)"""
    return tuple(a[i] * (1 - weight) + b[i] * weight for i in range(3))


def luminance(rgb):
    def channel(c):
        c = c / 255
        return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4
    r, g, b = (channel(c) for c in rgb)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def contrast(a, b):
    la, lb = sorted((luminance(a), luminance(b)), reverse=True)
    return (la + 0.05) / (lb + 0.05)


def text_color(rgb):
    """White or black text, whichever reads better on this background."""
    return "white" if contrast(rgb, (255, 255, 255)) >= contrast(rgb, (0, 0, 0)) else "rgba(black, 0.87)"


def shades(hex_color):
    base = hex_to_rgb(hex_color)
    white = (255, 255, 255)
    dark = tuple(c * c / 255 for c in base)  # base multiplied by itself
    h, l, s = colorsys.rgb_to_hls(*(c / 255 for c in base))
    vivid = tuple(c * 255 for c in colorsys.hls_to_rgb(h, min(1, l * 1.1), min(1, s * 1.3)))
    return {
        "50": mix(white, base, 0.12), "100": mix(white, base, 0.30), "200": mix(white, base, 0.50),
        "300": mix(white, base, 0.70), "400": mix(white, base, 0.85), "500": base,
        "600": mix(dark, base, 0.87), "700": mix(dark, base, 0.70), "800": mix(dark, base, 0.54),
        "900": mix(dark, base, 0.25),
        "A100": mix(white, vivid, 0.35), "A200": mix(white, vivid, 0.70),
        "A400": vivid, "A700": mix(dark, vivid, 0.80),
    }


def palette_scss(name, values, overrides=None):
    overrides = overrides or {}
    lines = ["$%s: (" % name]
    for key, rgb in values.items():
        lines.append("  %s: %s," % (key, overrides.get(key, rgb_to_hex(rgb))))
    lines.append("  contrast: (")
    for key, rgb in values.items():
        lines.append("    %s: %s," % (key, text_color(rgb)))
    lines.append("  )")
    lines.append(");")
    return "\n".join(lines)


def generate_scss(colors):
    primary_hex = colors.get("primary", "#0B4F6C")
    secondary_hex = colors.get("secondary", primary_hex)
    accent_hex = colors.get("accent", "#E8772E")
    primary = shades(primary_hex)
    accent = shades(accent_hex)
    secondary = hex_to_rgb(secondary_hex)

    dark = dict(primary)
    dark["500"] = primary["200"]
    dark["600"] = secondary
    dark["800"] = primary["500"]
    dark["A100"] = primary["A100"]

    light = dict(primary)
    light["600"] = secondary

    header = ("// SPDX-FileCopyrightText: Copyright The OrbitOS Authors\n"
              "// SPDX-License-Identifier: Apache-2.0\n"
              "// Generated by orbitos/scripts/rebrand.py from orbitos/branding/branding.yml. Do not edit.\n")
    body = [
        "$orbitos-primary: %s;" % rgb_to_hex(primary["500"]),
        "$orbitos-secondary: %s;" % rgb_to_hex(secondary),
        "$orbitos-primary-light: %s;" % rgb_to_hex(primary["300"]),
        "$orbitos-hue3: %s;" % rgb_to_hex(primary["A100"]),
        "$orbitos-dark-primary: %s;" % rgb_to_hex(primary["200"]),
        "",
        palette_scss("orbitos-primary-palette", light),
        "",
        palette_scss("orbitos-dark-palette", dark),
        "",
        palette_scss("orbitos-accent-palette", accent),
        "",
    ]
    warnings = []
    ratio = contrast(primary["500"], (255, 255, 255))
    if ratio < 4.5:
        warnings.append("primary color %s has contrast %.1f:1 with white text; WCAG AA needs 4.5:1"
                        % (primary_hex, ratio))
    return header + "\n".join(body), warnings


# ---------- main ----------

def load_branding():
    with open(BRANDING_DIR / "branding.yml", encoding="utf-8") as f:
        data = yaml.safe_load(f) or {}
    name = str(data.get("name") or "OrbitOS").strip()
    return data, name


def main():
    parser = argparse.ArgumentParser(description="Apply orbitos/branding/branding.yml to the source tree.")
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--dry-run", action="store_true", help="show what would change, change nothing")
    mode.add_argument("--check", action="store_true", help="exit 1 if anything would change or a stray name remains")
    args = parser.parse_args()
    write = not (args.dry_run or args.check)

    data, brand_name = load_branding()
    changes = []  # (path, description)

    def update(path, new_text, description):
        old_text = path.read_text(encoding="utf-8") if path.exists() else None
        if old_text != new_text:
            changes.append((rel(path), description))
            if write:
                path.parent.mkdir(parents=True, exist_ok=True)
                # keep the file's own line endings (some upstream files use CRLF)
                crlf = path.exists() and b"\r\n" in path.read_bytes()
                path.write_text(new_text, encoding="utf-8", newline="\r\n" if crlf else "\n")

    # 1. product name -> placeholder or brand name
    for description, patterns, replacement in RULES:
        value = brand_name if replacement is LITERAL else replacement
        for path in files_for(patterns):
            text = path.read_text(encoding="utf-8")
            new_text, count = replace_names(text, value)
            if count:
                update(path, new_text, "%s: %d name(s)" % (description, count))

    # 2. assets
    for key, targets in ASSETS.items():
        source_name = data.get(key)
        if not source_name:
            continue
        source = BRANDING_DIR / source_name
        if source.suffix.lower() != ".svg":
            print("note: built-in %s copy skipped (only SVG is copied; the server still serves %s)" % (key, source_name))
            continue
        content = source.read_text(encoding="utf-8")
        for target in targets:
            update(target, content, "built-in %s" % key)

    # 3. colors
    scss, warnings = generate_scss(data.get("colors") or {})
    update(GENERATED_SCSS, scss, "UI colors")

    # 4. default page title
    html = INDEX_HTML.read_text(encoding="utf-8")
    new_html = re.sub(r"<title>[^<]*</title>", "<title>%s</title>" % brand_name, html, count=1)
    update(INDEX_HTML, new_html, "page title")

    # stray-name check in UI code
    stray = []
    for path in files_for(CHECK_ONLY):
        r = rel(path)
        if any(fnmatch.fnmatch(r, pattern) for pattern, _ in CHECK_ALLOW):
            continue
        for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            if not CHECK_SKIP_LINE.search(line) and PRODUCT_NAME.search(line):
                stray.append("%s:%d: %s" % (r, number, line.strip()[:120]))

    verb = "Would change" if not write else "Changed"
    for path, description in changes:
        print("%s %s (%s)" % (verb, path, description))
    for warning in warnings:
        print("warning: " + warning)
    for line in stray:
        print("stray upstream name: " + line)
    if not changes:
        print("Nothing to change: the tree already matches branding.yml (brand: %s)." % brand_name)

    if args.check and (changes or stray):
        sys.exit(1)


if __name__ == "__main__":
    main()
