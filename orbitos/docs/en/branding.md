# Change the branding

*[Version française](../fr/branding.md)*

Everything about the brand lives in one folder: `orbitos/branding/`.

| File | What it is |
|---|---|
| `branding.yml` | Name, colors, login texts, footer, legal links |
| `logo.svg` | Logo in the top bar and on the login page (light logo, shown on the main color) |
| `favicon.svg` | Small icon in the browser tab |
| `extra/` | Other versions of the OrbitOS logo, not used by the app: for light backgrounds, stacked (README), icon only, PNG copies |

You never need to edit code.

## Change the name, logo, texts or links (no rebuild)

1. Edit `orbitos/branding/branding.yml` and/or replace `logo.svg` and `favicon.svg`.
   Logos can be SVG, PNG or ICO, 1 MB maximum.
2. Give the folder to the server. With Docker Compose, add this to the `orbitos` service:
   ```yaml
   volumes:
     - ./my-branding:/etc/orbitos/branding:ro
   ```
3. Restart: `docker compose restart orbitos`.
4. Reload the page in your browser.

What changes: page title, logo, favicon, login page, footer, every place the product name appears
in the interface, help pages and emails.

Seven built-in widget descriptions are written into the database at install time. They keep the
name used when the image was built (see the next section).

## Change the colors (rebuild needed)

Colors are compiled into the interface, so they need a new image.

1. Edit `colors` in `orbitos/branding/branding.yml` (`primary`, `secondary`, `accent`, as `#RRGGBB`).
2. Build the image again:
   ```bash
   docker compose build orbitos
   ```
3. Restart with the new image.

The build checks contrast. If white text on your primary color is too hard to read
(below 4.5:1, the WCAG AA level), it prints a warning. Pick a darker primary color.

## Text in several languages

Each text has one value per language:

```yaml
login:
  title:
    en: "Welcome to {brandName}"
    fr: "Bienvenue dans {brandName}"
    fr_CA: "Bienvenue sur {brandName}"   # optional: only for French (Canada)
```

The interface looks for the exact language (`fr_CA`), then the base language (`fr`), then English.
`{brandName}` is replaced by the name, `{year}` by the current year.

## Legal links

Fill in the links you have. An empty link (`""`) is hidden.

```yaml
links:
  privacy: "https://example.ca/privacy"
  terms: ""
  accessibility: "https://example.ca/accessibility"
  support: "mailto:support@example.ca"
```

Quebec's Law 25 expects your privacy policy to be easy to find: set `privacy`.
(This is a technical note, not legal advice.)

## For developers: the rebrand script

`python orbitos/scripts/rebrand.py` applies `branding.yml` to the source tree: it replaces the
upstream product name with placeholders, copies the built-in logo and favicon, generates the color
file, and sets the default page title. The Docker build runs it automatically.

- `--dry-run` shows what would change.
- `--check` fails if anything would change or an upstream name is still visible (used by CI).
- Running it twice changes nothing the second time.
- Files that must keep the upstream name (LICENSE, NOTICE, copyright headers) are never touched.
