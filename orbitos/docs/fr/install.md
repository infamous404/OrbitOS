# Installer, sauvegarder, mettre à jour

*[English version](../en/install.md)*

## Ce qu'il vous faut

- Un ordinateur ou un serveur avec **Docker** (Docker Desktop sous Windows ou macOS, Docker Engine sous Linux).
- **4 Go de mémoire vive** pour construire l'image la première fois. Pour fonctionner, OrbitOS a besoin d'environ **2 Go**.
- Environ 5 Go d'espace disque.

## Installer (une seule commande)

```bash
git clone <votre copie du dépôt OrbitOS> orbitos
cd orbitos
docker compose up -d
```

La première fois, Docker construit OrbitOS à partir du code source. Comptez de 15 à 30 minutes
selon votre ordinateur. Les démarrages suivants prennent environ une minute.

Ensuite :

1. Ouvrez **http://localhost:8080**. La page de configuration s'affiche.
2. Choisissez votre langue.
3. Entrez le **code de configuration**. Par sécurité, il n'apparaît que dans le journal du serveur :
   ```bash
   docker compose logs orbitos
   ```
   Cherchez « Configuration initiale » et copiez le code (`XXXX-XXXX-XXXX`).
4. Créez votre compte : nom de l'organisation, votre adresse courriel et un mot de passe (12 caractères ou plus).
5. Laissez « Créer un appareil et un tableau de bord d'exemple » coché, puis cliquez sur **Terminer la configuration**.
6. Cliquez sur **Envoyer une valeur de test**, puis sur **Se connecter et ouvrir le tableau de bord**. La valeur s'affiche.

C'est tout. Pour vérifier que tout va bien :

```bash
docker compose ps
```

La ligne `orbitos` indique `healthy` quand le serveur et la base de données fonctionnent.

### Deux comptes

| Compte | Sert à | Identifiant |
|---|---|---|
| Administrateur de l'organisation | Appareils, tableaux de bord, règles, utilisateurs : le travail de tous les jours | votre adresse courriel |
| Administrateur de la plateforme | Organisations, paramètres de la plateforme (serveur de courriel, sécurité) | `sysadmin@local.invalid` (ou l'adresse choisie) |

Votre compte utilise le mot de passe choisi. L'administrateur de la plateforme reçoit son propre
mot de passe aléatoire, affiché **une seule fois** à la fin de la configuration (enregistrez-le
dans un gestionnaire de mots de passe); il doit être changé à sa première connexion. Voir les
[contrôles de confidentialité et de sécurité](privacy-security.md) pour l'authentification à deux
facteurs, le journal d'audit, la conservation et les demandes d'accès ou d'effacement.

## Paramètres

Tous les paramètres sont facultatifs. Copiez `.env.example` en `.env`, modifiez-le, puis relancez `docker compose up -d`.

| Paramètre | Par défaut | Signification |
|---|---|---|
| `ORBITOS_BIND` | `127.0.0.1` | Qui peut se connecter. `127.0.0.1` = cet ordinateur seulement. `0.0.0.0` = aussi votre réseau (nécessaire pour de vrais appareils). |
| `ORBITOS_HTTP_PORT`, `ORBITOS_MQTT_PORT`, `ORBITOS_COAP_PORT` | 8080, 1883, 5683 | Ports sur cet ordinateur. |
| `ORBITOS_DEFAULT_LANGUAGE` | `en_US` | Langue des courriels et des données par défaut, jusqu'à ce que la page de configuration la fixe. |
| `ORBITOS_BRAND_NAME` | (tiré de `branding.yml`) | Nom du produit affiché partout. Voir le [guide de l'image de marque](branding.md). |
| `ORBITOS_ONLINE_MAPS` | `false` | `true` permet aux widgets de carte de charger des fonds de carte en ligne gratuits. |
| `ORBITOS_ALLOWED_HOSTS` | (vide) | Autres hôtes Web dont l'interface peut charger des données. |
| `ORBITOS_JAVA_OPTS` | `-Xms256m -Xmx1g` | Mémoire du serveur. |

## Sécurité par défaut

- **Aucun mot de passe par défaut.** Le mot de passe de la base de données est aléatoire et
  généré au premier démarrage. Le compte administrateur est créé sur la page de configuration,
  protégée par un code à usage unique.
- **Rien d'exposé par défaut.** Les ports n'écoutent que sur cet ordinateur. La base de données
  n'a aucun port hors de Docker.
- **Aucun appel sortant.** Le serveur ne contacte jamais Internet de lui-même. L'interface Web ne
  peut charger des données que depuis votre serveur (une règle du navigateur, la « Content
  Security Policy »). Les widgets de carte n'affichent aucun fond de carte tant que
  `ORBITOS_ONLINE_MAPS=true` n'est pas défini.

## HTTPS

Pour servir OrbitOS en HTTPS sur les ports 443 et 80 :

```bash
docker compose --profile https up -d
```

- Sur un réseau local (par défaut) : un certificat d'une autorité locale, sans Internet. Les
  navigateurs affichent un avertissement tant que cette autorité n'est pas approuvée (voir la
  documentation « local HTTPS » de Caddy).
- Avec un nom de domaine public : dans `.env`, indiquez `ORBITOS_DOMAIN=iot.exemple.ca` et
  `ORBITOS_TLS=vous@exemple.ca`. Un certificat gratuit est demandé à Let's Encrypt (cela
  contacte Internet).

Le MQTT chiffré (TLS, port 8883) n'est pas encore configuré.

## Sauvegarder et restaurer

Sauvegarder (le serveur peut rester démarré) :

```bash
docker compose run --rm backup
```

Le fichier est écrit dans le dossier `backups`, par exemple `backups/orbitos-20261002-101500.dump`.
Copiez-le sur un autre disque ou un autre ordinateur.

Restaurer (remplace toutes les données par la sauvegarde) :

```bash
docker compose stop orbitos
docker compose run --rm restore orbitos-20261002-101500.dump
docker compose start orbitos
```

La sauvegarde contient la base de données : appareils, télémesures, tableaux de bord, utilisateurs, paramètres.

## Mettre à jour

1. Sauvegardez : `docker compose run --rm backup`
2. Récupérez la nouvelle version : `git pull` (ou décompressez la nouvelle version dans le même dossier).
3. Construisez-la et mettez à jour la base de données :
   ```bash
   docker compose build orbitos
   docker compose stop orbitos
   docker compose run --rm orbitos upgrade
   docker compose up -d
   ```

### Revenir en arrière

En cas de problème après une mise à jour :

1. Revenez à la version précédente : `git checkout <version précédente>`, puis `docker compose build orbitos`.
2. Restaurez la sauvegarde faite à l'étape 1 (voir « Sauvegarder et restaurer »).
3. `docker compose up -d`

## Désinstaller

```bash
docker compose down        # arrêter, garder les données
docker compose down -v     # arrêter et SUPPRIMER toutes les données (base de données, mot de passe généré)
```

## Dépannage

| Problème | Que faire |
|---|---|
| La page ne s'ouvre pas | `docker compose ps` : attendez que `orbitos` soit `healthy` (premier démarrage : quelques minutes). Ensuite, `docker compose logs orbitos`. |
| Code de configuration perdu | `docker compose logs orbitos`. Après 10 essais ratés, un nouveau code est affiché. |
| « An earlier installation did not finish » | Le premier démarrage a été interrompu. Supprimez les données et recommencez : `docker compose down -v`, puis `docker compose up -d`. |
| Les appareils du réseau ne se connectent pas | Indiquez `ORBITOS_BIND=0.0.0.0` dans `.env` et vérifiez votre pare-feu. |
