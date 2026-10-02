# OrbitOS

*[English version](README.md)*

OrbitOS est une plateforme IdO (Internet des objets) libre, pensée pour le Canada et ailleurs :
branchez vos appareils, recueillez leurs données, consultez-les dans des tableaux de bord et
réagissez grâce à des règles. Elle fonctionne en français et en anglais, roule entièrement sur
votre ordinateur ou votre serveur, et n'envoie jamais de données ni de statistiques d'utilisation à qui que ce soit.

> **État : début du développement (phase 1 sur 6).** Pas prête pour la production. Voir [CHANGELOG.md](CHANGELOG.md).

## Ce qui la distingue

- **Locale et privée.** Aucune télémétrie, aucune vérification de mises à jour, aucun appel à une
  boutique en ligne. Chaque appel réseau sortant est listé dans [UPSTREAM_PATCHES.md](UPSTREAM_PATCHES.md)
  et est désactivé par défaut.
- **Votre image de marque.** Le nom, le logo, les couleurs, le texte de connexion et les liens
  légaux viennent d'un seul fichier : [`orbitos/branding/branding.yml`](orbitos/branding/branding.yml).
  Voir le [guide de l'image de marque](orbitos/docs/fr/branding.md).
- **Bilingue.** Le français (Canada et France) et l'anglais sont traités à égalité. *(En cours : phase 2.)*
- **Licence ouverte.** Licence Apache 2.0, pour toujours. Voir [Licence](#licence).

## Essayer (développeurs)

Il vous faut Docker. Tout le reste est construit dans Docker.

1. Construisez l'image (environ 20 minutes la première fois) :
   ```bash
   docker build -f orbitos/docker/Dockerfile -t orbitos/orbitos:dev .
   ```
2. Créez `orbitos/docker/.env` avec une seule ligne : `ORBITOS_DB_PASSWORD=` suivi d'une longue valeur aléatoire.
3. Créez la base de données (la première fois seulement) :
   ```bash
   docker compose -f orbitos/docker/compose.yml --profile install run --rm orbitos-install
   ```
4. Démarrez :
   ```bash
   docker compose -f orbitos/docker/compose.yml up -d
   ```
5. Ouvrez http://localhost:8080.

La phase 1 utilise encore le compte administrateur par défaut hérité du projet d'origine.
Une installation en une commande, avec des mots de passe générés et un assistant de premier
démarrage, arrive à la phase 3.

## Licence

OrbitOS est distribuée sous la [licence Apache 2.0](LICENSE).

OrbitOS est un embranchement (fork) de l'édition communautaire de
[ThingsBoard](https://github.com/thingsboard/thingsboard) 4.3.1.6, la dernière version sous
licence Apache 2.0. Elle ne contient aucun code de ThingsBoard 4.4 ou plus récent, qui utilise une
autre licence. OrbitOS n'est ni affiliée à ThingsBoard, Inc. ni approuvée par elle.
Voir [NOTICE](NOTICE) et [UPSTREAM_PATCHES.md](UPSTREAM_PATCHES.md).

## Sécurité

Voir [SECURITY.md](SECURITY.md).
