#### Modèles de notification d'alarme

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `alarmType` - type d'alarme;
* `action` - l'une des valeurs : 'created' (créée), 'severity changed' (gravité modifiée), 'acknowledged' (reconnue), 'cleared' (effacée), 'deleted' (supprimée);
* `alarmId` - l'identifiant de l'alarme (UUID);
* `alarmSeverity` - gravité de l'alarme (en minuscules);
* `alarmStatus` - l'état de l'alarme;
* `alarmOriginatorEntityType` - le type d'entité à l'origine de l'alarme, p. ex. 'Device';
* `alarmOriginatorName` - le nom de l'entité à l'origine de l'alarme, p. ex. 'Sensor T1';
* `alarmOriginatorLabel` - le libellé de l'entité à l'origine de l'alarme, p. ex. 'Sensor T1';
* `alarmOriginatorId` - l'identifiant de l'entité à l'origine de l'alarme (UUID);
* `recipientTitle` - nom du destinataire (prénom et nom s'ils sont indiqués, sinon l'adresse courriel);
* `recipientEmail` - adresse courriel du destinataire;
* `recipientFirstName` - prénom du destinataire;
* `recipientLastName` - nom de famille du destinataire;
* `details.<clé>` - tout champ des détails de l'alarme. Par exemple, si les détails sont `{"data": "Temperature is 25"}`, utilisez `${details.data}` pour obtenir "Temperature is 25";

Les noms de paramètres doivent être entourés de `${...}`. Par exemple : `${action}`.
Vous pouvez aussi modifier la valeur du paramètre avec l'un de ces suffixes :

* `upperCase`, par exemple : `${recipientFirstName:upperCase}`
* `lowerCase`, par exemple : `${recipientFirstName:lowerCase}`
* `capitalize`, par exemple : `${recipientFirstName:capitalize}`

<div class="divider"></div>

##### Exemples

Prenons une notification de nouvelle alarme de type 'High Temperature' pour l'appareil 'Sensor A'.
Le modèle suivant :

```text
Alarm '${alarmType}' - ${action:upperCase}
{:copy-code}
```

devient :

```text
Alarm 'High Temperature' - CREATED
```

<br/>

Le modèle suivant :

```text
${alarmOriginatorEntityType:capitalize} '${alarmOriginatorName}'
{:copy-code}
```

devient :

```text
Device - Sensor A
```

<br>
<br>
