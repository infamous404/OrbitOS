# Contrôles de confidentialité et de sécurité

*[English version](../en/privacy-security.md)*

Ce que fait OrbitOS pour protéger les comptes et les renseignements personnels, et comment les
administrateurs s'en servent. Ce guide n'est pas un avis juridique. Faites valider votre usage par
un avocat spécialisé ou votre responsable de la protection des renseignements personnels (Loi 25
au Québec, LPRPDE au Canada, RGPD, CCPA).

## Comptes et connexion

| Contrôle | Par défaut | Où le modifier |
|---|---|---|
| Aucun mot de passe par défaut | L'administrateur de la plateforme reçoit un mot de passe aléatoire de 20 caractères, affiché **une seule fois** à la fin de la configuration | — |
| Changement obligatoire de ce mot de passe | À remplacer à la première connexion | — |
| Longueur des mots de passe | 12 caractères minimum | Administrateur de la plateforme : Paramètres de sécurité |
| Verrouillage | Après 10 échecs de connexion | Paramètres de sécurité |
| Authentification à deux facteurs | Offerte à tous (application d'authentification, codes de secours); non obligatoire | Paramètres de sécurité > Authentification à deux facteurs (« Imposer ») |

### Activer l'authentification à deux facteurs pour votre compte

1. Ouvrez votre profil (en haut à droite) > **Sécurité**.
2. Activez **Application d'authentification** et lisez le code QR avec une application comme Aegis,
   Google Authenticator ou Microsoft Authenticator. Les codes sont calculés sur votre téléphone;
   le serveur n'a pas besoin d'Internet.
3. Entrez le code à 6 chiffres pour confirmer. Créez aussi des **codes de secours** et rangez-les en lieu sûr.

Pour l'imposer à tous, l'administrateur de la plateforme active « Imposer l'authentification à
deux facteurs » dans les paramètres de sécurité.

## Journal d'audit (inviolabilité vérifiable)

Chaque entrée du journal d'audit (connexions, modifications d'appareils, de tableaux de bord,
d'utilisateurs, de règles...) est liée à la précédente par une empreinte SHA-256. Si quelqu'un
modifie, supprime ou insère une entrée directement dans la base de données, la chaîne ne
correspond plus et la vérification indique où.

Pour vérifier :

```bash
docker compose run --rm orbitos verify-audit
```

Le résultat est `"status" : "intact"` (code de sortie 0) ou `"broken"` avec les entrées en cause
(code de sortie 3). L'administrateur de la plateforme peut aussi appeler `GET /api/orbitos/audit/verify`.

Limites :
- La chaîne détecte une altération; elle ne l'empêche pas. Une personne ayant un accès complet à
  la base de données pourrait reconstruire toute la chaîne. Gardez une copie de la valeur
  `latestHash` ailleurs (par exemple dans un courriel hebdomadaire à vous-même) : si une
  vérification ultérieure montre un historique différent avant ce point, le journal a été reconstruit.
- Les entrées supprimées par la durée de conservation (ci-dessous) sont comptées comme `pruned`, pas comme une altération.
- Les noms d'utilisateur et de cible ne font pas partie de l'empreinte : un effacement peut donc les remplacer par un pseudonyme.

## Conservation (garder les données seulement le temps nécessaire)

| Données | Conservées | Pour modifier |
|---|---|---|
| Télémesures (valeurs des appareils) | 365 jours | Profil d'organisation (« Default storage TTL days »), ou `SQL_TTL_TS_TS_KEY_VALUE_TTL` (secondes) |
| Alarmes | 365 jours | Profil d'organisation (« Alarms TTL days ») |
| Commandes à distance (RPC) | 30 jours | Profil d'organisation (« RPC TTL days ») |
| Journal d'audit | 365 jours | `SQL_TTL_AUDIT_LOGS_SECS` dans `.env` (secondes) |
| Événements du moteur de règles | 30 jours (débogage : 7 jours) | `SQL_TTL_EVENTS_EVENTS_TTL` |
| Notifications | 30 jours | `SQL_TTL_NOTIFICATIONS_SECS` |

Les anciennes données sont supprimées par des tâches de fond (environ une fois par jour). Les
sauvegardes gardent ce qu'elles contenaient : supprimez les anciens fichiers de sauvegarde selon
les mêmes durées.

## Demandes relatives aux renseignements personnels (accès, effacement)

Pour une personne qui a un compte : dans **Utilisateurs** (utilisateurs d'un client, ou liste des
administrateurs d'organisation pour l'administrateur de la plateforme), chaque ligne offre deux actions.

- **Exporter les renseignements personnels** : télécharge un fichier JSON avec le compte, l'état
  de connexion (jamais le mot de passe), les préférences, les actions faites par la personne et
  sur son compte (journal d'audit), les notifications, et la fiche du client si le compte en dépend.
- **Effacer ce compte** : supprime le compte, ses données de connexion, ses réglages
  d'authentification à deux facteurs, ses préférences et ses notifications. Les entrées du journal
  d'audit sont conservées pour la sécurité pendant la durée de conservation, mais le nom de la
  personne est remplacé par un pseudonyme comme `erased-user-3f9a1c0b2d`.

Règles : un administrateur d'organisation ne traite que les comptes de sa propre organisation;
personne ne peut effacer son propre compte ni celui d'un administrateur de la plateforme avec cet outil.

Ce qui n'est **pas** couvert et demande un examen manuel par votre responsable de la protection
des renseignements personnels :
- Le texte saisi par la personne à l'intérieur des détails d'audit (par exemple dans une
  description d'appareil qu'elle a modifiée) reste jusqu'à la fin de la durée de conservation du journal.
- Les données d'appareils ne sont pas attribuées à des personnes par la plateforme. Si un appareil
  mesure une personne (objet porté, capteur de pièce), traitez ses données avec l'appareil
  (supprimez l'appareil ou ses télémesures).
- Les copies dans les sauvegardes.
