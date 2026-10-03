# Démarrage rapide

*[English version](../en/quick-start.md)*

De zéro à un tableau de bord en direct. Il vous faut un ordinateur avec **Docker** (Docker Desktop
sous Windows ou macOS) et environ 4 Go de mémoire libre pour la première construction.

## 1. Démarrer OrbitOS

Dans le dossier d'OrbitOS :

```bash
docker compose up -d
```

La première fois, OrbitOS est construit à partir du code source (de 15 à 30 minutes). Les
démarrages suivants prennent une minute. Avec une image prête à l'emploi, voir « Utiliser une
image publiée » dans le [guide d'installation](install.md).

## 2. Le configurer

1. Ouvrez **http://localhost:8080**. La page de configuration s'affiche.
2. Choisissez votre langue.
3. Obtenez le code de configuration : `docker compose logs orbitos`, cherchez « Configuration initiale ».
4. Entrez le nom de votre organisation, votre adresse courriel et un mot de passe (12 caractères ou plus).
5. Laissez « Créer un appareil et un tableau de bord d'exemple » coché et cliquez sur **Terminer la configuration**.
6. **Enregistrez le mot de passe de l'administrateur de la plateforme** affiché à la fin (il n'est affiché qu'une fois).

## 3. Envoyer des données

Cliquez sur **Envoyer une valeur de test** à la dernière étape, ou envoyez des données comme un vrai appareil :

```bash
python orbitos/scripts/mqtt_device.py --token <jeton de l'appareil affiché sur la page de configuration>
```

## 4. Les voir

Cliquez sur **Se connecter et ouvrir le tableau de bord**. Les valeurs s'affichent dès leur arrivée.

## Ensuite

- Ajoutez vos propres appareils : [Connecter des appareils](connect-devices.md).
- Invitez des personnes, créez des tableaux de bord, réglez des alarmes : [Guide de l'administrateur](admin-guide.md).
- Activez l'authentification à deux facteurs : [Confidentialité et sécurité](privacy-security.md).
- Sauvegardez régulièrement : [Installer, sauvegarder, mettre à jour](install.md).
