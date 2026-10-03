#### Modèles de notification de manque de ressources

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `resource` - le nom de la ressource (p. ex. "CPU", "RAM", "STORAGE");
* `usage` - l'utilisation actuelle de la ressource;
* `serviceId` - l'identifiant du service (utile en grappe);
* `serviceType` - le type de service (utile en grappe);
* `recipientEmail` - adresse courriel du destinataire;
* `recipientFirstName` - prénom du destinataire;
* `recipientLastName` - nom de famille du destinataire;

Les noms de paramètres doivent être entourés de `${...}`. Par exemple : `${resource}`.
Vous pouvez aussi modifier la valeur du paramètre avec l'un de ces suffixes :

* `upperCase`, par exemple : `${resource:upperCase}`
* `lowerCase`, par exemple : `${resource:lowerCase}`
* `capitalize`, par exemple : `${resource:capitalize}`

<div class="divider"></div>

##### Exemples

Prenons un système qui manque de ressources libres (processeur, mémoire ou stockage).
Le modèle suivant :

```text
Warning: ${resource} is critically high at ${usage}%
{:copy-code}
```

devient :

```text
Warning: CPU is critically high at 83%
```

<br>
<br>
