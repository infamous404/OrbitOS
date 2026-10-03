#### Modèles de notification de limite du nombre d'entités

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `entityType` - l'une des valeurs : 'Device', 'Asset', 'User', etc.;
* `currentCount` - le nombre actuel d'entités;
* `limit` - la limite du nombre d'entités;
* `percents` - le pourcentage défini dans la règle de notification;
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

Prenons une organisation qui a créé 400 appareils sur un maximum de 500, avec un seuil de règle de 0,8 (80 %).
Le modèle suivant :

```text
${entityType:capitalize}s usage: ${currentCount}/${limit} (${percents}%)
{:copy-code}
```

devient :

```text
Devices usage: 400/500 (80%)
```

<br>
<br>
