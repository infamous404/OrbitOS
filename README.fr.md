<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="orbitos/branding/extra/logo-stacked-dark.svg">
    <img src="orbitos/branding/extra/logo-stacked-light.svg" alt="OrbitOS" width="240">
  </picture>
</p>

# OrbitOS

*[English version](README.md)*

OrbitOS est une plateforme IdO (Internet des objets) libre, pensée pour le Canada et ailleurs :
branchez vos appareils, recueillez leurs données, consultez-les dans des tableaux de bord et
réagissez grâce à des règles. Elle fonctionne en français et en anglais, roule entièrement sur
votre ordinateur ou votre serveur, et n'envoie jamais de données ni de statistiques d'utilisation à qui que ce soit.

> **État : version 1.0.** Les six phases sont terminées; voir [le rapport final](orbitos/docs/status/FINAL_STATUS.md). Faites-la examiner par un avocat ou un responsable de la protection des renseignements personnels avant un usage en production. Voir [CHANGELOG.md](CHANGELOG.md).

## Ce qui la distingue

- **Locale et privée.** Aucune télémétrie, aucune vérification de mises à jour, aucun appel à une
  boutique en ligne. Chaque appel réseau sortant est listé dans [UPSTREAM_PATCHES.md](UPSTREAM_PATCHES.md)
  et est désactivé par défaut.
- **Votre image de marque.** Le nom, le logo, les couleurs, le texte de connexion et les liens
  légaux viennent d'un seul fichier : [`orbitos/branding/branding.yml`](orbitos/branding/branding.yml).
  Voir le [guide de l'image de marque](orbitos/docs/fr/branding.md).
- **Bilingue.** Le français (Canada et France) et l'anglais sont traités à égalité : écrans, courriels, dates.
- **Simple à faire rouler.** Une seule commande, environ 1,3 Go de mémoire, aucun mot de passe par défaut, fonctionne hors ligne.
- **Privée et sécurisée par défaut.** Authentification à deux facteurs, journal d'audit vérifiable, durées de conservation, exportation et effacement des renseignements personnels (Loi 25, LPRPDE).
- **Appareils.** MQTT (aussi chiffré), HTTP, CoAP; Modbus, OPC UA et BACnet par un module de passerelle facultatif.
- **Licence ouverte.** Licence Apache 2.0, pour toujours. Voir [Licence](#licence).

## Démarrage rapide

Il vous faut Docker. Dans ce dossier :

```bash
docker compose up -d
```

Le premier démarrage construit OrbitOS (de 15 à 30 minutes). Ouvrez ensuite **http://localhost:8080** :
la page de configuration crée votre compte, votre organisation et un appareil d'exemple avec un
tableau de bord. Le code de configuration demandé se trouve dans le journal du serveur
(`docker compose logs orbitos`).

Guides ([tous](orbitos/docs/fr/index.md), aussi dans l'application à `/docs`) : [démarrage rapide](orbitos/docs/fr/quick-start.md) · [installation, sauvegardes, mises à jour, HTTPS](orbitos/docs/fr/install.md) · [connecter des appareils](orbitos/docs/fr/connect-devices.md) · [API et webhooks](orbitos/docs/fr/api.md) · [confidentialité et sécurité](orbitos/docs/fr/privacy-security.md).

## Licence

OrbitOS est distribuée sous la [licence Apache 2.0](LICENSE).

OrbitOS est un embranchement (fork) de l'édition communautaire de
[ThingsBoard](https://github.com/thingsboard/thingsboard) 4.3.1.6, la dernière version sous
licence Apache 2.0. Elle ne contient aucun code de ThingsBoard 4.4 ou plus récent, qui utilise une
autre licence. OrbitOS n'est ni affiliée à ThingsBoard, Inc. ni approuvée par elle.
Voir [NOTICE](NOTICE) et [UPSTREAM_PATCHES.md](UPSTREAM_PATCHES.md).

## Soutien

OrbitOS est gratuit et fourni tel quel, sans garantie (licence Apache 2.0). L'aide vient de la communauté :
posez vos questions et signalez les bogues dans les [Issues GitHub](../../issues). Problèmes de sécurité : en privé, voir [SECURITY.md](SECURITY.md).

## Sécurité

Voir [SECURITY.md](SECURITY.md).
