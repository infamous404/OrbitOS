#### Modèles de notification de cycle de vie du moteur de règles

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `componentType` - l'une des valeurs : 'Rule chain' (chaîne de règles), 'Rule node' (nœud de règle);
* `componentId` - l'identifiant du composant (UUID);
* `componentName` - le nom de la chaîne ou du nœud de règle;
* `ruleChainId` - l'identifiant de la chaîne de règles (UUID);
* `ruleChainName` - le nom de la chaîne de règles;
* `eventType` - l'une des valeurs : 'started' (démarré), 'updated' (mis à jour), 'stopped' (arrêté);
* `action` - l'une des valeurs : 'start' (démarrer), 'update' (mettre à jour), 'stop' (arrêter);
* `error` - le texte de l'erreur;
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

Prenons une notification au sujet d'un nœud de règle Kafka mal configuré. Le modèle suivant :

```text
Rule node '${componentName}' - ${action} failure:<br/>${error}
{:copy-code}
```

devient :

```text
Rule node 'Export to Kafka' - start failure:<br/>Connection refused!
```

<br>
<br>
