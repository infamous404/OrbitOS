#### Modèles de notification de demande d'augmentation de la limite d'entités

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `entityType` - l'une des valeurs : 'Device', 'Asset', 'Customer', 'User', 'Dashboard', 'Rule chain', 'Edge';
* `userEmail` - adresse courriel de l'utilisateur qui envoie la demande;
* `increaseLimitActionLabel` - libellé du bouton qui ouvre la page de gestion des limites, p. ex. 'Set new limit';
* `increaseLimitLink` - lien vers la page de gestion des limites;
* `baseUrl` - sert à construire l'adresse complète de la page de gestion des limites dans les courriels;

Les noms de paramètres doivent être entourés de `${...}`. Par exemple : `${userEmail}`.
Vous pouvez aussi modifier la valeur du paramètre avec l'un de ces suffixes :

* `upperCase`, par exemple : `${userEmail:upperCase}`
* `lowerCase`, par exemple : `${userEmail:lowerCase}`
* `capitalize`, par exemple : `${userEmail:capitalize}`

<div class="divider"></div>

##### Exemples

Prenons une demande d'augmentation du nombre maximal d'appareils d'une organisation.
Le modèle suivant :

```text
${userEmail} has reached the maximum number of ${entityType:lowerCase}s allowed and is requesting an increase to the ${entityType:lowerCase} limit.
{:copy-code}
```

devient :

```text
johndoe@company.com has reached the maximum number of devices allowed and is requesting an increase to the device limit.
```

<br/>

<br>
<br>
