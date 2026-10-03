#### Modèles de notification du moteur de règles

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* les valeurs des métadonnées du message entrant, désignées par le nom de la clé de métadonnées;
* les valeurs des données du message entrant, désignées par le nom de la clé de données;
* `originatorType` - type de l'émetteur, p. ex. 'Device';
* `originatorId` - identifiant de l'émetteur
* `customerId` - identifiant du client, s'il y en a un
* `msgType` - type du message
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

Prenons un message entrant dans le nœud de règle avec les données suivantes :

```json
{
  "building_1": {
    "temperature": 24
  }
}
```

Le modèle suivant :

```text
Building 1: temperature is ${building_1.temperature} 
{:copy-code}
```

devient :

```text
Building 1: temperature is 24
```

<br>
<br>
