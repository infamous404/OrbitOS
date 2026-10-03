#### Modèles de notification de commentaire d'alarme

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `alarmType` - type d'alarme;
* `alarmId` - l'identifiant de l'alarme (UUID);
* `alarmSeverity` - gravité de l'alarme (en minuscules);
* `alarmStatus` - l'état de l'alarme;
* `alarmOriginatorEntityType` - le type d'entité à l'origine de l'alarme, p. ex. 'Device';
* `alarmOriginatorName` - le nom de l'entité à l'origine de l'alarme, p. ex. 'Sensor T1';
* `alarmOriginatorLabel` - le libellé de l'entité à l'origine de l'alarme, p. ex. 'Sensor T1';
* `alarmOriginatorId` - l'identifiant de l'entité à l'origine de l'alarme (UUID);
* `comment` - texte du commentaire;
* `action` - l'une des valeurs : 'added' (ajouté), 'updated' (modifié);
* `userTitle` - nom de l'utilisateur qui a fait l'action;
* `userEmail` - adresse courriel de l'utilisateur qui a fait l'action;
* `userFirstName` - prénom de l'utilisateur qui a fait l'action;
* `userLastName` - nom de famille de l'utilisateur qui a fait l'action;
* `recipientTitle` - nom du destinataire (prénom et nom s'ils sont indiqués, sinon l'adresse courriel);
* `recipientEmail` - adresse courriel du destinataire;
* `recipientFirstName` - prénom du destinataire;
* `recipientLastName` - nom de famille du destinataire;

Les noms de paramètres doivent être entourés de `${...}`. Par exemple : `${action}`.
Vous pouvez aussi modifier la valeur du paramètre avec l'un de ces suffixes :

* `upperCase`, par exemple : `${recipientFirstName:upperCase}`
* `lowerCase`, par exemple : `${recipientFirstName:lowerCase}`
* `capitalize`, par exemple : `${recipientFirstName:capitalize}`

<div class="divider"></div>

##### Exemples

Prenons une notification indiquant que l'alarme de type 'High Temperature' de l'appareil 'Sensor A' a été assignée
à l'utilisateur 'John Doe'. Le modèle suivant :

```text
Alarm '${alarmType}' - comment ${action}
{:copy-code}
```

devient :

```text
Alarm 'High Temperature' - comment added
```

<br/>

Le modèle suivant :

```text
Alarm '${alarmType}' (${alarmSeverity:capitalize}) was commented
{:copy-code}
```

devient :

```text
Alarm 'High Temperature' (Critical) was commented
```

<br>
<br>
