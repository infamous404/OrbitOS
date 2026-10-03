# Guide du développeur

*[English version](../en/developer-guide.md)*

Comment OrbitOS est construit, testé et maintenu à jour avec son projet d'origine.

## Organisation

| Chemin | Contenu |
|---|---|
| `orbitos/` | Tout ce qu'ajoute OrbitOS : fichiers Docker, scripts, image de marque, documentation, traductions, module IoT Gateway |
| `application/src/main/java/org/thingsboard/server/orbitos/` | Code serveur d'OrbitOS (configuration, image de marque, confidentialité, chaîne d'audit, politique hors ligne, documentation) |
| `ui-ngx/src/app/orbitos/` | Code Web d'OrbitOS (page de configuration, page de documentation, sélecteur de langue, actions de confidentialité) |
| `ui-ngx/src/assets/orbitos/` | Traductions d'OrbitOS (`locale/`) et liste des langues |
| `application/src/main/resources/orbitos.yml` | Chaque valeur par défaut d'OrbitOS qui diffère du projet d'origine |
| Tout le reste | Code d'origine (Apache-2.0). Chaque modification est listée dans `UPSTREAM_PATCHES.md` |

Règle générale : mettez le nouveau code dans un dossier `orbitos`; ne modifiez un fichier
d'origine que s'il n'y a pas d'autre moyen, gardez la modification petite et consignez-la dans `UPSTREAM_PATCHES.md`.

## Construire et tester

Seul Docker est nécessaire.

```bash
docker compose build orbitos                                       # l'image (15 à 30 min la première fois)
docker build -f orbitos/docker/Dockerfile --target test .          # tests unitaires du code OrbitOS
bash orbitos/scripts/smoke-test.sh                                 # installation neuve de bout en bout (environ 10 min)
```

Le test de fumée démarre une pile séparée (`orbitos-smoke`, port 18080), fait environ 35
vérifications (configuration, sécurité, confidentialité, MQTT dans les deux sens, passerelle,
sauvegardes, altération du journal d'audit) puis la supprime. `KEEP_RUNNING=1` la laisse en marche.

Vérifications rapides, sans construction (Python 3) :

```bash
python orbitos/scripts/rebrand.py --check              # image de marque appliquée, aucun nom de produit d'origine
python orbitos/scripts/i18n_check.py                   # traductions complètes
python orbitos/scripts/i18n_regional.py --check        # fichiers fr_CA / en_CA à jour
python orbitos/scripts/upstream_license_check.py --tree  # uniquement du code Apache-2.0
```

L'intégration continue (`.github/workflows/orbitos-ci.yml`) les exécute à chaque envoi.

## Modifications courantes

- **Image de marque** : [Votre image de marque](branding.md). **Langue** : [Ajouter une langue](add-a-language.md).
- **Un protocole d'appareil** : partez de `orbitos/scripts/mqtt_device.py` (client MQTT d'environ
  200 lignes, sans dépendance) ou des connecteurs de la IoT Gateway (`orbitos/gateway/`).
- **Un paramètre** : ajoutez-le à `orbitos.yml` avec une variable d'environnement, et à `.env.example`.
- **Un écran** : composants Angular autonomes dans `ui-ngx/src/app/orbitos/`; clés de texte sous
  `orbitos.*` dans `ui-ngx/src/assets/orbitos/locale/en_US.json` et `fr_FR.json`.

## Mises à jour du projet d'origine

OrbitOS suit la branche `lts-4.3` du projet d'origine, sous Apache-2.0, jusqu'au 2027-07-20.
Avant de fusionner une modification d'origine, faites-la passer par la vérification de licence :

```bash
python orbitos/scripts/upstream_license_check.py upstream/lts-4.3
```

Elle refuse tout ce qui n'est pas sous Apache-2.0. **Ne copiez jamais** de code de la version 4.4
ou ultérieure (licence différente). La procédure complète est dans `UPSTREAM_PATCHES.md`.

## Publier une image

`.github/workflows/orbitos-publish.yml` construit, teste, signe et publie dans le GitHub
Container Registry. Il est désactivé tant qu'un responsable n'a pas défini la variable de dépôt
`ORBITOS_PUBLISH=true`; il s'exécute ensuite sur les étiquettes de version (`v1.0.0`).

## Licence

Apache-2.0. Gardez l'en-tête SPDX dans chaque fichier. Le code de tiers doit être sous licence
permissive et inscrit dans `NOTICE`.
