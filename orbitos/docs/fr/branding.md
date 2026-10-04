# Changer l'image de marque

*[English version](../en/branding.md)*

Tout ce qui touche l'image de marque se trouve dans un seul dossier : `orbitos/branding/`.

| Fichier | Contenu |
|---|---|
| `branding.yml` | Nom, couleurs, textes de connexion, pied de page, liens légaux |
| `logo.svg` | Logo de la barre du haut et de la page de connexion (logo clair, affiché sur la couleur principale) |
| `favicon.svg` | Petite icône dans l'onglet du navigateur |
| `extra/` | Autres versions du logo OrbitOS, non utilisées par l'application : pour fond clair, empilée (README), icône seule, copies PNG |

Vous n'avez jamais à modifier le code.

## Changer le nom, le logo, les textes ou les liens (sans reconstruire)

1. Modifiez `orbitos/branding/branding.yml` ou remplacez `logo.svg` et `favicon.svg`.
   Les logos peuvent être en SVG, PNG ou ICO, 1 Mo au maximum.
2. Donnez le dossier au serveur. Avec Docker Compose, ajoutez ceci au service `orbitos` :
   ```yaml
   volumes:
     - ./ma-marque:/etc/orbitos/branding:ro
   ```
3. Redémarrez : `docker compose restart orbitos`.
4. Rechargez la page dans votre navigateur.

Ce qui change : le titre de la page, le logo, le favicon, la page de connexion, le pied de page,
chaque endroit où le nom du produit apparaît dans l'interface, les pages d'aide et les courriels.

Sept descriptions de widgets intégrés sont écrites dans la base de données à l'installation.
Elles gardent le nom utilisé lors de la construction de l'image (voir la section suivante).

## Changer les couleurs (reconstruction nécessaire)

Les couleurs sont compilées dans l'interface : il faut donc une nouvelle image.

1. Modifiez `colors` dans `orbitos/branding/branding.yml` (`primary`, `secondary`, `accent`, au format `#RRGGBB`).
2. Reconstruisez l'image :
   ```bash
   docker compose build orbitos
   ```
3. Redémarrez avec la nouvelle image.

La construction vérifie le contraste. Si du texte blanc sur votre couleur principale est trop
difficile à lire (sous 4,5:1, le niveau AA des WCAG), un avertissement s'affiche.
Choisissez alors une couleur principale plus foncée.

## Textes en plusieurs langues

Chaque texte a une valeur par langue :

```yaml
login:
  title:
    en: "Welcome to {brandName}"
    fr: "Bienvenue dans {brandName}"
    fr_CA: "Bienvenue sur {brandName}"   # facultatif : seulement pour le français du Canada
```

L'interface cherche d'abord la langue exacte (`fr_CA`), puis la langue de base (`fr`), puis l'anglais.
`{brandName}` est remplacé par le nom, et `{year}` par l'année en cours.

## Liens légaux

Remplissez les liens dont vous disposez. Un lien vide (`""`) est masqué.

```yaml
links:
  privacy: "https://exemple.ca/confidentialite"
  terms: ""
  accessibility: "https://exemple.ca/accessibilite"
  support: "mailto:soutien@exemple.ca"
```

La Loi 25 du Québec s'attend à ce que votre politique de confidentialité soit facile à trouver :
renseignez `privacy`. (Ceci est une note technique, pas un avis juridique.)

## Pour les développeurs : le script de changement de marque

`python orbitos/scripts/rebrand.py` applique `branding.yml` au code source : il remplace le nom du
produit d'origine par des marqueurs, copie le logo et le favicon intégrés, génère le fichier des
couleurs et règle le titre par défaut de la page. La construction Docker le lance automatiquement.

- `--dry-run` montre ce qui changerait.
- `--check` échoue si quelque chose changerait ou si un nom d'origine est encore visible (utilisé par l'intégration continue).
- Le lancer deux fois ne change rien la deuxième fois.
- Les fichiers qui doivent garder le nom d'origine (LICENSE, NOTICE, en-têtes de droit d'auteur) ne sont jamais modifiés.
