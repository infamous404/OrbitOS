# Ajouter une langue

*[English version](../en/add-a-language.md)*

OrbitOS est offerte en français et en anglais, chacun avec une variante canadienne. Cette page
explique comment les fichiers de langue s'assemblent et comment en ajouter un.

## Comment les langues sont construites

| Couche | Fichier | Contenu |
|---|---|---|
| 1. Base | `ui-ngx/src/assets/locale/locale.constant-<base>.json` | Tous les textes d'origine (`en_US`, `fr_FR`, …) |
| 2. OrbitOS | `ui-ngx/src/assets/orbitos/locale/<base>.json` | Textes propres à OrbitOS et corrections |
| 3. Région | `ui-ngx/src/assets/orbitos/locale/<langue>.json` | Seulement ce qui diffère (`fr_CA`, `en_CA`) |

Les couches suivantes l'emportent. Un texte manquant utilise la langue de base, puis l'anglais.

Les langues offertes dans le menu sont listées dans `ui-ngx/src/assets/orbitos/languages.json`.

## Ajouter une variante régionale (exemple : français de Belgique, `fr_BE`)

1. Ajoutez `"fr_BE"` à `ui-ngx/src/assets/orbitos/languages.json`.
2. Créez `ui-ngx/src/assets/orbitos/locale/fr_BE.json` avec seulement les textes qui diffèrent, par exemple
   `{ "orbitos": { "powered-by": "Propulsé par {{brandName}}" } }`.
3. Ajoutez son nom dans `en_US.json` et `fr_FR.json` sous `language.locales` (`"fr_BE": "français (Belgique)"`).
4. Dates et nombres : ajoutez la locale Angular dans `ui-ngx/src/app/orbitos/i18n.ts`
   (`registerLocaleData` et `angularLocale`) et l'import de la locale moment.js.
5. Lancez `python orbitos/scripts/i18n_check.py`. Le résultat doit être « passed ».

Pour générer la variante à partir de règles (comme pour `fr_CA` et `en_CA`), ajoutez une règle à
`orbitos/scripts/i18n_regional.py`, puis lancez-le.

## Ajouter une nouvelle langue (exemple : espagnol, `es_ES`)

1. Un fichier d'origine `locale.constant-es_ES.json` existe peut-être déjà; sinon, copiez le fichier anglais et traduisez-le.
2. Ajoutez `"es_ES"` à `languages.json`, et `"es": "es_ES"` à `BASE_LANGUAGES` dans `ui-ngx/src/app/orbitos/i18n.ts`
   et à `BASES` dans `orbitos/scripts/i18n_check.py`.
3. Créez `ui-ngx/src/assets/orbitos/locale/es_ES.json` avec les textes OrbitOS (copiez `en_US.json` et traduisez).
4. Courriels : copiez chaque `application/src/main/resources/templates/<nom>.ftl` en `<nom>_es.ftl` et traduisez
   le texte (gardez tout ce qui est entre `${` et `}`). Ajoutez `application/src/main/resources/i18n/messages_es.properties`.
5. Pages d'aide (facultatif) : placez les pages traduites dans `ui-ngx/src/assets/help/es_ES/`, mêmes dossiers que `en_US`.
6. Lancez `python orbitos/scripts/i18n_check.py` et corrigez chaque ligne signalée.
   Les textes qui doivent rester identiques à l'anglais (unités, noms de produits) vont dans `orbitos/i18n/identical-allowlist.txt`.

## Règles pour les traducteurs

- Gardez les marqueurs tels quels : `{{nom}}`, `{brandName}`, `${brandName}`. La vérification échoue s'ils diffèrent.
- Ne traduisez pas les balises ni les attributs HTML.
- Le français du Canada suit l'Office québécois de la langue française : *courriel*, *téléverser*, pas d'espace avant `? ! ;`.
