#### Modèles de notification d'échec de communication Edge

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `edgeId` - l'identifiant d'Edge (UUID);
* `edgeName` - le nom d'Edge;
* `failureMsg` - la description de l'échec survenu sur Edge;
* `recipientEmail` - adresse courriel du destinataire;
* `recipientFirstName` - prénom du destinataire;
* `recipientLastName` - nom de famille du destinataire;

Les noms de paramètres doivent être entourés de `${...}`. Par exemple : `${edgeName}`.
Vous pouvez aussi modifier la valeur du paramètre avec l'un de ces suffixes :

* `upperCase`, par exemple : `${edgeName:upperCase}`
* `lowerCase`, par exemple : `${edgeName:lowerCase}`
* `capitalize`, par exemple : `${edgeName:capitalize}`

<div class="divider"></div>

##### Exemples

Prenons une notification d'échec de traitement de la connexion à Edge.
Le modèle suivant :

```text
Edge '${edgeName}' communication failure occurred
{:copy-code}
```

devient :

```text
Edge 'DatacenterEdge' communication failure occurred
```

<br/>

Le modèle suivant :

```text
Failure message: '${failureMsg}'
{:copy-code}
```

devient :

```text
Failure message: 'Failed to process edge connection!'
```

<br>
<br>
