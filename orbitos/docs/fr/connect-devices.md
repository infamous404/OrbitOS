# Connecter des appareils

*[English version](../en/connect-devices.md)*

Chaque appareil a un **jeton d'accès** (page de l'appareil > *Copier le jeton d'accès*). L'appareil
l'envoie avec chaque message : c'est ainsi qu'OrbitOS sait quel appareil parle. Gardez-le secret.

Remplacez `HOTE` par l'adresse de votre serveur et `JETON` par le jeton de l'appareil. Depuis un
autre ordinateur, indiquez d'abord `ORBITOS_BIND=0.0.0.0` dans `.env` (voir le [guide d'installation](install.md)).

## MQTT (recommandé pour la plupart des appareils)

| Paramètre | Valeur |
|---|---|
| Serveur | `HOTE`, port `1883` |
| Nom d'utilisateur | le jeton de l'appareil (sans mot de passe) |
| Envoyer des valeurs | sujet `v1/devices/me/telemetry`, JSON comme `{"temperature": 21.5}` |
| Envoyer des propriétés | sujet `v1/devices/me/attributes`, JSON comme `{"firmware": "1.2"}` |
| Recevoir des commandes | s'abonner à `v1/devices/me/rpc/request/+` |
| Répondre à une commande | sujet `v1/devices/me/rpc/response/<id>` (le `<id>` du sujet de la requête) |

Valeurs avec leur propre heure : `{"ts": 1767225600000, "values": {"temperature": 21.5}}` (millisecondes depuis 1970, UTC).

Pour essayer sans écrire de code, avec le simulateur fourni (Python 3, rien à installer) :

```bash
python orbitos/scripts/mqtt_device.py --host HOTE --token JETON
```

Il envoie une température et une humidité toutes les 5 secondes et répond aux commandes. Lisez-le
comme exemple pour votre propre code : environ 200 lignes. Autres moyens :

```bash
mosquitto_pub -h HOTE -p 1883 -u JETON -t v1/devices/me/telemetry -m '{"temperature": 21.5}'
```

Sur microcontrôleur (ESP32, Arduino), toute bibliothèque MQTT convient (par exemple PubSubClient) :
connectez-vous avec le jeton comme nom d'utilisateur et publiez sur le sujet ci-dessus.

## HTTP

```bash
curl -X POST http://HOTE:8080/api/v1/JETON/telemetry -H "Content-Type: application/json" -d '{"temperature": 21.5}'
```

Propriétés : `/api/v1/JETON/attributes`. Idéal pour les appareils qui se réveillent, envoient et se
rendorment, et pour les services qui poussent des données (un « webhook entrant » : donnez-leur cette adresse).

## CoAP (petits appareils à pile)

`coap://HOTE:5683/api/v1/JETON/telemetry`, même JSON. Exemple avec libcoap :
`coap-client -m post coap://HOTE/api/v1/JETON/telemetry -e '{"temperature": 21.5}'`

## Chiffrement

Le MQTT et le CoAP ci-dessus ne sont pas chiffrés. Utilisez-les sur un réseau de confiance ou par
un RPV. Le MQTT sur TLS (port 8883) n'est pas encore configuré. Le HTTP peut passer par l'option
HTTPS du guide d'installation.

## LoRaWAN (capteurs à pile longue portée)

Les capteurs LoRaWAN parlent par radio à une passerelle LoRaWAN (antenne). Un serveur de réseau
LoRaWAN transforme ces messages radio en données. **ChirpStack** (libre, licence MIT) est le choix
habituel; il fonctionne à côté d'OrbitOS et transmet les mesures décodées à l'API HTTP des appareils d'OrbitOS.

1. Installez ChirpStack avec ses propres fichiers Docker Compose (voir chirpstack.io) et
   enregistrez-y votre passerelle et vos capteurs. Ajoutez un *codec* (décodeur) au profil
   d'appareil pour que les octets du capteur deviennent des valeurs nommées (température, pile...).
2. Dans OrbitOS, créez un appareil par capteur et copiez son jeton.
3. Dans ChirpStack, sur chaque capteur, ajoutez la *variable* de jeton d'accès attendue par
   l'intégration de ChirpStack pour cette famille de plateformes (ChirpStack la nomme d'après le
   projet d'origine; voir `NOTICE`), avec le jeton OrbitOS comme valeur.
4. Dans ChirpStack, *Application > Integrations*, ajoutez cette intégration avec l'adresse du
   serveur `http://HOTE:8080`.

Les valeurs décodées apparaissent alors sur l'appareil comme toute autre télémesure.
État : documenté, **non testé** avec du vrai matériel LoRaWAN dans ce projet.

## Modbus, OPC UA, BACnet et autres protocoles industriels

Les machines, compteurs d'énergie et systèmes de bâtiment parlent souvent Modbus (ou OPC UA,
BACnet...). La **IoT Gateway** libre (Apache-2.0, du projet d'origine, voir `NOTICE`) fonctionne sur
un petit ordinateur près des machines, les lit et envoie les valeurs à OrbitOS par MQTT.

1. Dans OrbitOS, créez un appareil pour la passerelle et cochez **Est une passerelle**. Copiez son jeton.
2. Installez la IoT Gateway (paquet Python ou image Docker; voir sa documentation) et indiquez,
   dans sa configuration principale : hôte `HOTE`, port `1883`, le jeton de la passerelle.
3. Activez le connecteur Modbus et décrivez chaque machine : adresse, période de lecture, et quels
   registres deviennent quelles valeurs. Chaque machine apparaît dans OrbitOS comme un appareil, créé automatiquement.

État : documenté, **non testé** avec du vrai matériel Modbus dans ce projet.
