# Changelog / Journal des modifications

## Unreleased: after version 1 (logo, look, fixes)

### Changed / Modifications
- New OrbitOS logo (vector, sharp at any size) and colors: deep navy, blue and cyan. PNG copies in `orbitos/branding/extra/`.
  / Nouveau logo OrbitOS (vectoriel, net à toute taille) et couleurs : bleu nuit, bleu et cyan. Copies PNG dans `orbitos/branding/extra/`.
- Subtle "cyber" look: dark side menu with a faint grid, cyan active marker, glow lines, dark sign-in page with a grid, clear keyboard focus.
  / Style « cyber » discret : menu latéral sombre avec une grille légère, repère cyan, lignes lumineuses, page de connexion sombre, focus clavier bien visible.
- Server log: OrbitOS banner and messages; clearer message after an unfinished installation (gives the exact command).
  / Journal du serveur : bannière et messages OrbitOS; message plus clair après une installation inachevée (donne la commande exacte).

### Fixed / Corrections
- Language menu after sign-in: the choice was replaced by the old one when the page reloaded.
  / Menu de langue après la connexion : le choix était remplacé par l'ancien au rechargement de la page.
- Browser console error on the sign-in, setup and docs pages (logo link).
- Signing in to an account set to another language left you on the sign-in page.
  / La connexion à un compte réglé dans une autre langue laissait sur la page de connexion.
- Docs page: logo size, language button contrast, phone layout. Dark style also on password-reset and two-factor pages.
  / Page d'aide : taille du logo, contraste du bouton de langue, affichage sur téléphone. Style sombre aussi sur les pages de réinitialisation et de double authentification.
- Windows clones keep Linux line endings for container scripts (`.gitattributes`).
  / Les clones sous Windows gardent les fins de ligne Linux pour les scripts des conteneurs.
  / Erreur dans la console du navigateur sur les pages de connexion, de configuration et d'aide (lien du logo).

### Added / Ajouts
- CoAP test device (`orbitos/scripts/coap_device.py`) and CoAP check in the end-to-end test; optional OPC UA check with a simulated machine (`ORBITOS_TEST_OPCUA=1`).
  / Appareil CoAP de test et vérification CoAP dans le test complet; vérification OPC UA facultative avec une machine simulée.

## Unreleased: Phase 6 (docs, add-ons, version 1)

### Added / Ajouts
- Optional IoT Gateway add-on (`--profile gateway`): Modbus, OPC UA, BACnet and more; no calls home; simulated Modbus meter to try it.
  / Module facultatif IoT Gateway : Modbus, OPC UA, BACnet et plus; aucun appel vers l'extérieur; compteur Modbus simulé pour l'essayer.
- Encrypted MQTT on port 8883 (`ORBITOS_MQTT_TLS=true`), certificate created offline on first start.
  / MQTT chiffré sur le port 8883, certificat créé hors ligne au premier démarrage.
- In-app documentation at `/docs` (EN/FR, offline); help links open it instead of the upstream website.
  / Documentation intégrée à `/docs` (FR/EN, hors ligne); les liens d'aide l'ouvrent au lieu du site d'origine.
- Guides: quick start, administrator guide, developer guide, docs index. 20 help pages translated to French.
  / Guides : démarrage rapide, guide de l'administrateur, guide du développeur, index. 20 pages d'aide traduites en français.

### Removed / Retraits
- Paid-edition upsell on the platform administrator's home page; vendor mobile-app QR card and store links.
  / Publicité pour l'édition payante sur l'accueil de l'administrateur; carte QR et liens vers l'application mobile du fournisseur.

## Unreleased: Phase 5 (interoperability, trimmed)

### Added / Ajouts
- MQTT verified end to end in both directions (values in, commands out and answered); dependency-free device simulator `orbitos/scripts/mqtt_device.py`.
  / MQTT vérifié de bout en bout dans les deux sens; simulateur d'appareil sans dépendance `orbitos/scripts/mqtt_device.py`.
- Guides: connect devices (MQTT, HTTP, CoAP, LoRaWAN with ChirpStack, Modbus/OPC UA/BACnet with the IoT Gateway), REST and WebSocket APIs, webhooks.
  / Guides : connecter des appareils, API REST et WebSocket, webhooks.
- API reference page rebranded, no vendor contact; licence link points to the Apache licence.
  / Page de référence de l'API à notre image, sans contact du fournisseur d'origine.

## Unreleased: Phase 4 essentials (privacy and security)

### Added / Ajouts
- Platform administrator: random password shown once at setup, forced change at first sign-in.
  / Administrateur de la plateforme : mot de passe aléatoire affiché une seule fois, changement obligatoire à la première connexion.
- Two-factor sign-in available by default (authenticator app, backup codes); 12-character passwords; lockout after 10 failures.
  / Authentification à deux facteurs offerte par défaut (application, codes de secours); mots de passe de 12 caractères; verrouillage après 10 échecs.
- Tamper-evident audit log (SHA-256 hash chain) with `docker compose run --rm orbitos verify-audit`.
  / Journal d'audit à inviolabilité vérifiable (chaîne d'empreintes SHA-256), vérifiable avec `verify-audit`.
- Retention defaults: telemetry and alarms 365 days, audit log 365 days, events 30 days, RPC 30 days.
  / Durées de conservation par défaut : télémesures et alarmes 365 jours, journal d'audit 365 jours, événements 30 jours, RPC 30 jours.
- Privacy requests: export personal data (JSON) and erase an account (audit names pseudonymized).
  / Demandes relatives aux renseignements personnels : exportation (JSON) et effacement d'un compte (noms pseudonymisés dans le journal d'audit).
- GHCR publish workflow, off until a maintainer enables it (`ORBITOS_PUBLISH=true`); signed images with SBOM and provenance.
  / Flux de publication GHCR, désactivé tant qu'un responsable ne l'active pas; images signées avec SBOM et provenance.

## Unreleased: Phase 3 (easy, safe, offline-first setup)

### Added / Ajouts
- One command: `docker compose up -d`. The database is created on first start; its password is generated.
  / Une seule commande : `docker compose up -d`. La base de données est créée au premier démarrage; son mot de passe est généré.
- First-run setup page: language, organization, your account, platform administrator, sample device and dashboard, test value. Protected by a one-time code printed in the server log.
  / Page de configuration initiale : langue, organisation, votre compte, administrateur de la plateforme, appareil et tableau de bord d'exemple, valeur de test. Protégée par un code à usage unique affiché dans le journal du serveur.
- Default rule chain and notifications created in the chosen language (French or English).
  / Chaîne de règles et notifications par défaut créées dans la langue choisie (français ou anglais).
- Offline browser policy: the web interface only loads data from your server; `ORBITOS_ONLINE_MAPS` and `ORBITOS_ALLOWED_HOSTS` open it up.
  / Politique hors ligne du navigateur : l'interface ne charge des données que depuis votre serveur; `ORBITOS_ONLINE_MAPS` et `ORBITOS_ALLOWED_HOSTS` l'élargissent.
- Health check (`docker compose ps`), backup and restore commands, optional HTTPS (Caddy, local certificate by default), upgrade and rollback guide.
  / Vérification de l'état (`docker compose ps`), commandes de sauvegarde et de restauration, HTTPS facultatif (Caddy, certificat local par défaut), guide de mise à jour et de retour en arrière.
- `ORBITOS_BRAND_NAME` setting; server memory capped at 1 GB by default.
  / Paramètre `ORBITOS_BRAND_NAME`; mémoire du serveur limitée à 1 Go par défaut.
- Small visual accents: thin cyan lines, monospace for codes and values, a dark sample dashboard.
  / Petites touches visuelles : fines lignes cyan, police à chasse fixe pour les codes et les valeurs, tableau de bord d'exemple sombre.

### Removed / Retraits
- The upstream default account `sysadmin@thingsboard.org` / `sysadmin`. / Le compte par défaut d'origine.

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
