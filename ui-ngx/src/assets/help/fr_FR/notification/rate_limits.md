#### Modèles de notification de dépassement des limites de débit

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `api` - nom de l'API limitée; l'une des valeurs : 'REST API requests', 'REST API requests per customer', 'transport messages',
  'transport messages per device', 'Cassandra queries', 'WS updates per session', 'notification requests', 'notification requests per rule',
  'entity version creation', 'entity version load', 'Edge events', 'Edge events per edge', 'Edge uplink messages', 'Edge uplink messages per edge';
* `limitLevelEntityType` - type de l'entité visée par la limite, p. ex. 'Tenant', 'Device', 'Notification rule', 'Customer', etc.;
* `limitLevelEntityId` - identifiant de l'entité visée par la limite;
* `limitLevelEntityName` - nom de l'entité visée par la limite;
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

Prenons le client 'Customer A' qui a dépassé la limite de requêtes REST par client. Le modèle suivant :

```text
Rate limits for ${api} exceeded for ${limitLevelEntityType:lowerCase} '${limitLevelEntityName}'
{:copy-code}
```

devient :

```text
Rate limits for REST API requests per customer exceeded for customer 'Customer A'
```

<br>
<br>
