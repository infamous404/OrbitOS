# API : REST, WebSocket, webhooks

*[English version](../en/api.md)*

Pour les applications qui lisent des données ou commandent des appareils. Pour *envoyer* les
données d'un appareil, voir [Connecter des appareils](connect-devices.md).

## Référence

Chaque appel REST est décrit, avec un bouton « Try it out », à **http://HOTE:8080/swagger-ui.html**
(servi par votre propre serveur, fonctionne hors ligne). Cliquez sur *Authorize* et collez `Bearer <jeton>`.

## Se connecter

```bash
curl -X POST http://HOTE:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"username": "vous@exemple.ca", "password": "votre mot de passe"}'
```

La réponse contient `token` (valide 2,5 heures) et `refreshToken`. Envoyez le jeton avec chaque appel :

```
X-Authorization: Bearer <jeton>
```

Utilisez un utilisateur dédié à chaque application, avec le plus petit rôle suffisant (un
utilisateur client s'il ne lit que les appareils de ce client). Si l'authentification à deux
facteurs est activée pour cet utilisateur, la première réponse est un jeton de pré-vérification :
envoyez le code à `POST /api/auth/2fa/verification/check?providerType=TOTP&verificationCode=123456`.

## Appels courants

| Quoi | Appel |
|---|---|
| Lister vos appareils | `GET /api/tenant/devices?pageSize=20&page=0` |
| Dernières valeurs | `GET /api/plugins/telemetry/DEVICE/{deviceId}/values/timeseries?keys=temperature,humidity` |
| Historique | même chose, plus `&startTs=<ms>&endTs=<ms>&limit=1000` |
| Propriétés de l'appareil | `GET /api/plugins/telemetry/DEVICE/{deviceId}/values/attributes` |
| Envoyer une commande et attendre la réponse | `POST /api/plugins/rpc/twoway/{deviceId}` avec `{"method": "getStatus", "params": {}}` |
| Envoyer une commande sans attendre | `POST /api/plugins/rpc/oneway/{deviceId}` |
| Alarmes | `GET /api/alarm/DEVICE/{deviceId}?pageSize=20&page=0` |

## Mises à jour en direct (WebSocket)

Connectez-vous à `ws://HOTE:8080/api/ws` (ou `wss://` avec HTTPS), puis envoyez :

```json
{"authCmd": {"cmdId": 0, "token": "<jeton>"},
 "cmds": [{"type": "TIMESERIES", "entityType": "DEVICE", "entityId": "<deviceId>",
           "scope": "LATEST_TELEMETRY", "cmdId": 10}]}
```

Vous recevez les valeurs actuelles, puis chaque nouvelle valeur dès son arrivée :
`{"subscriptionId": 10, "data": {"temperature": [[1767225600000, "21.5"]]}}`.

Dans un navigateur :

```js
const ws = new WebSocket('ws://HOTE:8080/api/ws');
ws.onopen = () => ws.send(JSON.stringify({authCmd: {cmdId: 0, token}, cmds: [
  {type: 'TIMESERIES', entityType: 'DEVICE', entityId: deviceId, scope: 'LATEST_TELEMETRY', cmdId: 10}]}));
ws.onmessage = event => console.log(JSON.parse(event.data).data);
```

## Webhooks

**Entrants** (un autre service envoie des données à OrbitOS) : donnez-lui l'adresse HTTP de
l'appareil `http://HOTE:8080/api/v1/<jeton de l'appareil>/telemetry`. Voir [Connecter des appareils](connect-devices.md).

**Sortants** (OrbitOS appelle votre service quand des données arrivent) :

1. *Chaînes de règles* > ouvrez la chaîne de règles racine.
2. Ajoutez un nœud **REST API call**. URL : votre service, par exemple `https://exemple.ca/hooks/iot`;
   méthode `POST`. Le corps est le message de l'appareil (JSON). Les en-têtes peuvent utiliser
   les détails du message, par exemple `X-Device: ${deviceName}`.
3. Reliez la sortie *Post telemetry* du nœud *Message Type Switch* à ce nœud, puis enregistrez.

Rien n'est envoyé nulle part tant que vous n'ajoutez pas un tel nœud : c'est une connexion sortante que vous choisissez.
État : documenté, **non testé** de bout en bout dans ce projet.
