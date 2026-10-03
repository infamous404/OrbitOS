#### Modèles de notification de limite d'utilisation de l'API

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `feature` - fonction de l'API visée par la limite; l'une des valeurs : 'Device API', 'Telemetry persistence',
  'Rule Engine execution', 'JavaScript functions execution', 'Email messages', 'SMS messages', 'Alarms';
* `status` - l'une des valeurs : 'enabled' (activée), 'warning' (avertissement), 'disabled' (désactivée);
* `unitLabel` - nom de l'unité limitée; l'une des valeurs : 'message', 'data point', 'Rule Engine execution',
  'JavaScript execution', 'email message', 'SMS message', 'alarm';
* `limit` - la limite d'unités utilisées;
* `currentValue` - nombre actuel d'unités utilisées;
* `tenantId` - identifiant de l'organisation;
* `tenantName` - nom de l'organisation;
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

Prenons une organisation dont les appareils ont envoyé 8 000 messages sur un maximum de 10 000,
avec un seuil d'avertissement de 0,8 (80 %) dans le profil d'organisation. Le modèle suivant :

```text
${feature} feature - ${status:upperCase} (usage: ${currentValue} out of ${limit} ${unitLabel}s)
{:copy-code}
```

devient :

```text
Device API feature - WARNING (usage: 8000 out of 10000 messages)
```

<br>
<br>
