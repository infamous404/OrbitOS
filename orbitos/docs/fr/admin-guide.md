# Guide de l'administrateur

*[English version](../en/admin-guide.md)*

L'utilisation d'OrbitOS au quotidien. Pour le serveur lui-même (installation, sauvegardes, mises
à jour), voir le [guide d'installation](install.md).

## Qui fait quoi

| Rôle | Peut | Personne type |
|---|---|---|
| Administrateur de la plateforme | Créer des organisations, régler la plateforme (serveur de courriel, sécurité, deux facteurs) | TI |
| Administrateur d'organisation | Tout dans une organisation : appareils, tableaux de bord, règles, utilisateurs, clients | Responsable des opérations |
| Utilisateur client | Voir les appareils et tableaux de bord attribués à son client | Un client, un gestionnaire d'immeuble |

Une **organisation** (appelée « locataire » dans certains écrans) est entièrement séparée des
autres. Un **client** est un groupe dans une organisation : il permet de donner à un client
l'accès à ses seuls appareils.

## Utilisateurs

- **Administrateurs d'organisation** : administrateur de la plateforme > *Locataires* > l'organisation > *Gérer les administrateurs*.
- **Utilisateurs clients** : *Clients* > le client > *Gérer les utilisateurs*.
- Les nouveaux utilisateurs reçoivent un courriel d'activation si un serveur de courriel est
  configuré (plus bas); sinon, copiez le lien d'activation affiché après la création et envoyez-le vous-même.
- Chaque ligne offre **Exporter les renseignements personnels** et **Effacer ce compte** pour les
  demandes relatives aux renseignements personnels (voir [Confidentialité et sécurité](privacy-security.md)).

## Appareils

1. *Entités > Appareils > +*. Donnez un nom et, au besoin, un **profil d'appareil** (un type,
   comme « thermostat », qui regroupe réglages et règles d'alarme).
2. Ouvrez l'appareil et *Copiez le jeton d'accès* : l'appareil s'en sert pour se connecter
   (voir [Connecter des appareils](connect-devices.md)).
3. *Vérifier la connectivité*, sur la page de l'appareil, affiche des commandes prêtes à copier.
4. Attribuez un appareil à un client pour que ce client le voie.

L'onglet **Dernières télémesures** montre la dernière valeur de chaque mesure; **Attributs**
montre les propriétés de l'appareil (version du micrologiciel, emplacement...).

<!-- #dashboards -->
## Tableaux de bord

1. *Tableaux de bord > +*, puis *Modifier* (crayon).
2. *Ajouter un widget* : graphiques, cartes de valeur, jauges, cartes géographiques, tableaux,
   boutons qui envoient des commandes.
3. Choisissez les données : le widget demande un *alias d'entité* (quels appareils) et les
   *clés* (quelles mesures, par exemple `temperature`).
4. Enregistrez. Rendez un tableau de bord visible à un client avec *Attribuer au client*, ou
   public (lien en lecture seule) avec *Rendre public*.

Les widgets de carte n'affichent pas de fond de carte tant que le serveur est hors ligne (par
défaut); voir `ORBITOS_ONLINE_MAPS` dans le [guide d'installation](install.md).

<!-- #alarms -->
## Alarmes

Les alarmes se règlent par profil d'appareil : *Profils > Profils d'appareil >* le profil
*> Règles d'alarme*. Exemple : « Température élevée » quand `temperature` dépasse 30 pendant
5 minutes, effacée quand elle redescend sous 28. Les alarmes actives apparaissent dans *Alarmes*,
dans les tableaux de bord et comme notifications.

<!-- #rules -->
## Règles

Chaque message d'un appareil passe par la **chaîne de règles racine** : elle enregistre les
valeurs, les propriétés et traite les commandes. Ouvrez *Chaînes de règles* pour ajouter des étapes, par exemple :

- filtrer ou transformer des valeurs (nœud de script);
- envoyer un courriel ou une notification;
- appeler un autre service (nœud REST API call; voir les [webhooks](api.md));
- créer des alarmes à partir de conditions complexes.

Modifiez les chaînes de règles avec prudence : une chaîne racine brisée empêche l'enregistrement
des données. Exportez-la d'abord (*Chaînes de règles* > exporter) pour pouvoir la restaurer.

<!-- #notifications -->
## Courriel et notifications

- **Serveur de courriel** (administrateur de la plateforme) : *Paramètres > Serveur de courriel*.
  Nécessaire pour les courriels d'activation et de réinitialisation du mot de passe. Les
  courriels partent dans la langue de chaque personne.
- **Notifications** : *Centre de notifications*. Des règles par défaut avertissent les
  administrateurs d'organisation des nouvelles alarmes, des changements d'activité des
  appareils et des échecs. Chaque personne choisit comment les recevoir (Web, courriel) dans son profil.

## Vos réglages

Menu en haut à droite > *Profil* : nom, langue, mot de passe, authentification à deux facteurs.
