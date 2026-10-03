#### Modèles de notification d'échec de traitement d'une tâche

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `taskType` - le type de tâche, p. ex. 'telemetry deletion';
* `taskDescription` - la description de la tâche, p. ex. 'telemetry deletion for device c4d93dc0-63a1-11ee-aa6d-f7cbc0a71325';
* `error` - la trace de l'erreur
* `tenantId` - l'identifiant de l'organisation;
* `entityType` - le type d'entité visée par la tâche;
* `entityId` - l'identifiant de l'entité visée par la tâche;
* `attempt` - le nombre de tentatives de traitement de la tâche
* `recipientEmail` - adresse courriel du destinataire;
* `recipientFirstName` - prénom du destinataire;
* `recipientLastName` - nom de famille du destinataire;

Les noms de paramètres doivent être entourés de `${...}`. Par exemple : `${entityType}`.
Vous pouvez aussi modifier la valeur du paramètre avec l'un de ces suffixes :

* `upperCase`, par exemple : `${entityType:upperCase}`
* `lowerCase`, par exemple : `${entityType:lowerCase}`
* `capitalize`, par exemple : `${entityType:capitalize}`

<div class="divider"></div>

##### Exemples

Prenons une suppression de télémesures qui a échoué pour un appareil.
Le modèle suivant :

```text
Failed to process ${taskType} for ${entityType:lowerCase} ${entityId}
{:copy-code}
```

devient :

```text
Failed to process telemetry deletion for device c4d93dc0-63a1-11ee-aa6d-f7cbc0a71325
```

<br>
<br>
