#### Modèles de notification d'action sur une entité

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `entityType` - le type d'entité, p. ex. 'Device';
* `entityId` - l'identifiant de l'entité (UUID);
* `entityName` - le nom de l'entité;
* `actionType` - l'une des valeurs : 'added' (ajoutée), 'updated' (modifiée), 'deleted' (supprimée);
* `userId` - identifiant de l'utilisateur qui a fait l'action;
* `userTitle` - nom de l'utilisateur qui a fait l'action;
* `userEmail` - adresse courriel de l'utilisateur qui a fait l'action;
* `userFirstName` - prénom de l'utilisateur qui a fait l'action;
* `userLastName` - nom de famille de l'utilisateur qui a fait l'action;
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

Prenons une notification indiquant que l'appareil 'T1' a été ajouté par l'utilisateur 'john.doe@gmail.com'.
Le modèle suivant :

```text
${entityType:capitalize} was ${actionType}!
{:copy-code}
```

devient :

```text
Device was added!
```

<br/>

Le modèle suivant :

```text
${entityType} '${entityName}' was ${actionType} by user ${userEmail}
{:copy-code}
```

devient :

```text
Device 'T1' was added by user john.doe@gmail.com
```

<br>
<br>
