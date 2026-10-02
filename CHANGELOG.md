# Changelog / Journal des modifications

## Unreleased: Phase 2 (English and French)

### Added / Ajouts
- Four languages: English (Canada, US), French (Canada, France). Regional variants hold only the differences.
  / Quatre langues : anglais (Canada, É.-U.), français (Canada, France). Les variantes régionales ne contiennent que les différences.
- Language switcher on the login page and in the top bar; saved per user and per browser; default from the browser.
  / Sélecteur de langue sur la page de connexion et dans la barre du haut; enregistré par utilisateur et par navigateur; langue du navigateur par défaut.
- French and Canadian date and number formats. / Formats de date et de nombre français et canadiens.
- About 130 French strings added or fixed; Québec wording (courriel, téléverser, typography) for fr_CA; Canadian spelling for en_CA.
  / Environ 130 textes français ajoutés ou corrigés; usage québécois (courriel, téléverser, typographie) pour fr_CA; orthographe canadienne pour en_CA.
- Emails in the recipient's language (10 French templates); default language setting `ORBITOS_DEFAULT_LANGUAGE`.
  / Courriels dans la langue du destinataire (10 modèles français); langue par défaut `ORBITOS_DEFAULT_LANGUAGE`.
- Common server error messages translated. / Messages d'erreur courants du serveur traduits.
- CI translation checks; "Add a language" guide. / Vérifications des traductions en intégration continue; guide « Ajouter une langue ».

## Unreleased: Phase 1 (fork, branding, white-label)

### Added / Ajouts
- Fork of ThingsBoard CE `v4.3.1.6` (Apache-2.0). / Embranchement de ThingsBoard CE `v4.3.1.6` (Apache-2.0).
- Runtime branding from one file, `orbitos/branding/branding.yml`: name, logo, favicon, login title and subtitle, footer, legal links. Colors apply at the next image build.
  / Image de marque définie dans un seul fichier, `orbitos/branding/branding.yml` : nom, logo, favicon, titre et sous-titre de connexion, pied de page, liens légaux. Les couleurs s'appliquent à la prochaine construction de l'image.
- `orbitos/scripts/rebrand.py` (idempotent, `--dry-run`, `--check`) and `orbitos/scripts/upstream_license_check.py` (licence check for every upstream patch).
- Docker build (`orbitos/docker/Dockerfile`) and development compose file.
- CI: build, branding check, licence check, dependency scanning.

### Removed / Retraits
- All calls home: update checker, Community Grant enrollment (could download and run a program), IoT Hub marketplace (off and hidden), remote help pages, GitHub star badge.
  / Tous les appels vers l'extérieur : vérification des mises à jour, inscription « Community Grant » (pouvait télécharger et exécuter un programme), boutique IoT Hub (désactivée et masquée), pages d'aide distantes, badge GitHub.
- Edge support is off by default. / La prise en charge d'Edge est désactivée par défaut.
- Trendz settings menu hidden. / Menu des paramètres Trendz masqué.
