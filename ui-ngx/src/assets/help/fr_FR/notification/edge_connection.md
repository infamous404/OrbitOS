#### Modèles de notification de connexion Edge

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `edgeId` - l'identifiant d'Edge (UUID);
* `edgeName` - le nom d'Edge;
* `eventType` - l'état de la connexion : connected (connecté) ou disconnected (déconnecté);
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

Prenons une notification de connexion d'Edge à {{brandName}}.
Le modèle suivant :

```text
Edge '${edgeName}' is now ${eventType}
{:copy-code}
```

devient :

```text
Edge 'DatacenterEdge' is now connected
```

<br/>

<br>
<br>
