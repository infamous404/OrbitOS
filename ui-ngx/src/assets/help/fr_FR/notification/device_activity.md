#### Modèles de notification d'activité d'appareil

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `deviceId` - l'identifiant de l'appareil (UUID);
* `deviceName` - le nom de l'appareil;
* `deviceLabel` - le libellé de l'appareil;
* `deviceType` - le type d'appareil;
* `eventType` - l'une des valeurs : 'inactive' (inactif), 'active' (actif);
* `recipientTitle` - nom du destinataire (prénom et nom s'ils sont indiqués, sinon l'adresse courriel);
* `recipientEmail` - adresse courriel du destinataire;
* `recipientFirstName` - prénom du destinataire;
* `recipientLastName` - nom de famille du destinataire;

Les noms de paramètres doivent être entourés de `${...}`. Par exemple : `${recipientFirstName}`.
Vous pouvez aussi modifier la valeur du paramètre avec l'un de ces suffixes :

* `upperCase`, par exemple : `${recipientFirstName:upperCase}`
* `lowerCase`, par exemple : `${recipientFirstName:lowerCase}`
* `capitalize`, par exemple : `${recipientFirstName:capitalize}`

<div class="divider"></div>

##### Exemples

Prenons une notification indiquant que le thermomètre 'Sensor T1' est inactif.
Le modèle suivant :

```text
Device '${deviceName}' inactive
{:copy-code}
```

devient :

```text
Device 'Sensor T1' inactive
```

<br/>

Le modèle suivant :

```text
${deviceType:capitalize} '${deviceName}' became ${eventType}
{:copy-code}
```

devient :

```text
Thermometer 'Sensor T1' became inactive
```

<br>
<br>
