#### Modèles de notification de nouvelle version de la plateforme

<div class="divider"></div>
<br/>

Les champs objet et message de la notification acceptent des paramètres de modèle.
La liste des paramètres disponibles dépend du type de modèle.
Voici les types et paramètres disponibles :

Paramètres de modèle disponibles :

* `latestVersion` - la dernière version de la plateforme;
* `latestVersionReleaseNotesUrl` - lien vers les notes de version de la dernière version;
* `upgradeInstructionsUrl` - lien vers les instructions de mise à jour;
* `currentVersion` - la version actuelle de la plateforme
* `currentVersionReleaseNotesUrl` - lien vers les notes de version de la version actuelle;
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

Prenons une nouvelle version 3.5.0 publiée alors que la version installée est la 3.4.4. Le modèle suivant :

```text
New version ${latestVersion} is available. Current version is ${currentVersion}
{:copy-code}
```

devient :

```text
New version 3.5.0 is available. Current version is 3.4.4
```

<br>
<br>
