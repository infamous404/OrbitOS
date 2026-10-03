# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
"""Builds the French help pages for administrators (notification templates, date formats,
certificate hints) from the English pages, line by line. Code blocks and HTML lines are copied
as they are. Run after an upstream update; it lists any English line it cannot translate.

    python orbitos/i18n/help_fr.py          (writes ui-ngx/src/assets/help/fr_FR/...)
    python orbitos/i18n/help_fr.py --check  (fails if a page is missing or out of date)

French used here is understood in Canada and France (courriel, téléverser...)."""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
EN = ROOT / "ui-ngx/src/assets/help/en_US"
FR = ROOT / "ui-ngx/src/assets/help/fr_FR"
PAGES = sorted([p.relative_to(EN) for p in (EN / "notification").glob("*.md")]) + [
    Path("date/date-format.md"), Path("device-profile/x509-chain-hint.md"),
    Path("device-profile/x509-chain-regex-examples.md")]

TITLES = {
    "Alarm": "d'alarme", "Alarm assignment": "d'assignation d'alarme", "Alarm comment": "de commentaire d'alarme",
    "API usage limit": "de limite d'utilisation de l'API", "Device activity": "d'activité d'appareil",
    "Edge communication failure": "d'échec de communication Edge", "Edge connection": "de connexion Edge",
    "Entity count limit": "de limite du nombre d'entités",
    "Entity count limit increase request": "de demande d'augmentation de la limite d'entités",
    "Entity action": "d'action sur une entité", "General": "générale",
    "New platform version": "de nouvelle version de la plateforme", "Exceeded rate limits": "de dépassement des limites de débit",
    "Resources shortage": "de manque de ressources", "Rule engine lifecycle": "de cycle de vie du moteur de règles",
    "Rule engine": "du moteur de règles", "Task processing failure": "d'échec de traitement d'une tâche",
}

PATTERNS = [
    (re.compile(r"^#### (.+) notification templatization$"),
     lambda m: "#### Modèles de notification %s" % TITLES[m.group(1)] if m.group(1) in TITLES else None),
    (re.compile(r"^\* `(upperCase|lowerCase|capitalize)`, for example - (.+)$"),
     lambda m: "* `%s`, par exemple : %s" % (m.group(1), m.group(2))),
    (re.compile(r"^Parameter names must be wrapped using `\$\{\.\.\.\}`\. For example: (`\$\{\w+\}`)\.$"),
     lambda m: "Les noms de paramètres doivent être entourés de `${...}`. Par exemple : %s." % m.group(1)),
]

LINES = {
    "Notification subject and message fields support templatization.":
        "Les champs objet et message de la notification acceptent des paramètres de modèle.",
    "The list of available templatization parameters depends on the template type.":
        "La liste des paramètres disponibles dépend du type de modèle.",
    "See the available types and parameters below:": "Voici les types et paramètres disponibles :",
    "Available template parameters:": "Paramètres de modèle disponibles :",
    "You may also modify the value of the parameter with one of the suffixes:":
        "Vous pouvez aussi modifier la valeur du paramètre avec l'un de ces suffixes :",
    "##### Examples": "##### Exemples",
    "The following template:": "Le modèle suivant :",
    "will be transformed to:": "devient :",
    "* `alarmType` - alarm type;": "* `alarmType` - type d'alarme;",
    "* `action` - one of: 'created', 'severity changed', 'acknowledged', 'cleared', 'deleted';":
        "* `action` - l'une des valeurs : 'created' (créée), 'severity changed' (gravité modifiée), 'acknowledged' (reconnue), 'cleared' (effacée), 'deleted' (supprimée);",
    "* `alarmId` - the alarm id as uuid string;": "* `alarmId` - l'identifiant de l'alarme (UUID);",
    "* `alarmSeverity` - alarm severity (lower case);": "* `alarmSeverity` - gravité de l'alarme (en minuscules);",
    "* `alarmStatus` - the alarm status;": "* `alarmStatus` - l'état de l'alarme;",
    "* `alarmOriginatorEntityType` - the entity type of the alarm originator, e.g. 'Device';":
        "* `alarmOriginatorEntityType` - le type d'entité à l'origine de l'alarme, p. ex. 'Device';",
    "* `alarmOriginatorName` - the name of the alarm originator, e.g. 'Sensor T1';":
        "* `alarmOriginatorName` - le nom de l'entité à l'origine de l'alarme, p. ex. 'Sensor T1';",
    "* `alarmOriginatorLabel` - the label of the alarm originator, e.g. 'Sensor T1';":
        "* `alarmOriginatorLabel` - le libellé de l'entité à l'origine de l'alarme, p. ex. 'Sensor T1';",
    "* `alarmOriginatorId` - the alarm originator entity id as uuid string;":
        "* `alarmOriginatorId` - l'identifiant de l'entité à l'origine de l'alarme (UUID);",
    "* `recipientTitle` - title of the recipient (first and last name if specified, email otherwise);":
        "* `recipientTitle` - nom du destinataire (prénom et nom s'ils sont indiqués, sinon l'adresse courriel);",
    "* `recipientEmail` - email of the recipient;": "* `recipientEmail` - adresse courriel du destinataire;",
    "* `recipientFirstName` - first name of the recipient;": "* `recipientFirstName` - prénom du destinataire;",
    "* `recipientLastName` - last name of the recipient;": "* `recipientLastName` - nom de famille du destinataire;",
    "* `details.<key>` - any key field from the alarm's details. Fox example, if details are `{\"data\": \"Temperature is 25\"}`, use `${details.data}` to access \"Temperature is 25\";":
        "* `details.<clé>` - tout champ des détails de l'alarme. Par exemple, si les détails sont `{\"data\": \"Temperature is 25\"}`, utilisez `${details.data}` pour obtenir \"Temperature is 25\";",
    "Let's assume the notification about new alarm with type 'High Temperature' for device 'Sensor A'.":
        "Prenons une notification de nouvelle alarme de type 'High Temperature' pour l'appareil 'Sensor A'.",
    "* `assigneeTitle` - title of the assignee;": "* `assigneeTitle` - nom de la personne assignée;",
    "* `assigneeEmail` - email of the assignee;": "* `assigneeEmail` - adresse courriel de la personne assignée;",
    "* `assigneeFirstName` - first name of the assignee;": "* `assigneeFirstName` - prénom de la personne assignée;",
    "* `assigneeLastName` - last name of the assignee;": "* `assigneeLastName` - nom de famille de la personne assignée;",
    "* `assigneeId` - the id of the assignee as uuid string;": "* `assigneeId` - l'identifiant de la personne assignée (UUID);",
    "* `userTitle` - title of the user who made the action;": "* `userTitle` - nom de l'utilisateur qui a fait l'action;",
    "* `userEmail` - email of the user who made the action;": "* `userEmail` - adresse courriel de l'utilisateur qui a fait l'action;",
    "* `userFirstName` - first name of the user who made the action;": "* `userFirstName` - prénom de l'utilisateur qui a fait l'action;",
    "* `userLastName` - last name of the user who made the action;": "* `userLastName` - nom de famille de l'utilisateur qui a fait l'action;",
    "* `action` - one of: 'assigned', 'unassigned';": "* `action` - l'une des valeurs : 'assigned' (assignée), 'unassigned' (désassignée);",
    "Let's assume the notification about alarm with type 'High Temperature' for device 'Sensor A' was assigned":
        "Prenons une notification indiquant que l'alarme de type 'High Temperature' de l'appareil 'Sensor A' a été assignée",
    "to user 'John Doe'. The following template:": "à l'utilisateur 'John Doe'. Le modèle suivant :",
    "* `comment` - text of the comment;": "* `comment` - texte du commentaire;",
    "* `action` - one of: 'added', 'updated';": "* `action` - l'une des valeurs : 'added' (ajouté), 'updated' (modifié);",
    "* `feature` - API feature for which the limit is applied; one of: 'Device API', 'Telemetry persistence',":
        "* `feature` - fonction de l'API visée par la limite; l'une des valeurs : 'Device API', 'Telemetry persistence',",
    "'Rule Engine execution', 'JavaScript functions execution', 'Email messages', 'SMS messages', 'Alarms';":
        "'Rule Engine execution', 'JavaScript functions execution', 'Email messages', 'SMS messages', 'Alarms';",
    "* `status` - one of: 'enabled', 'warning', 'disabled';": "* `status` - l'une des valeurs : 'enabled' (activée), 'warning' (avertissement), 'disabled' (désactivée);",
    "* `unitLabel` - name of the limited unit; one of: 'message', 'data point', 'Rule Engine execution',":
        "* `unitLabel` - nom de l'unité limitée; l'une des valeurs : 'message', 'data point', 'Rule Engine execution',",
    "'JavaScript execution', 'email message', 'SMS message', 'alarm';": "'JavaScript execution', 'email message', 'SMS message', 'alarm';",
    "* `limit` - the limit on used feature units;": "* `limit` - la limite d'unités utilisées;",
    "* `currentValue` - current number of used units;": "* `currentValue` - nombre actuel d'unités utilisées;",
    "* `tenantId` - id of the tenant;": "* `tenantId` - identifiant de l'organisation;",
    "* `tenantName` - name of the tenant;": "* `tenantName` - nom de l'organisation;",
    "Let's assume tenant's devices pushed 8K messages with the max allowed number of 10K":
        "Prenons une organisation dont les appareils ont envoyé 8 000 messages sur un maximum de 10 000,",
    "and warn threshold in tenant profile set to 0.8 (80%). The following template:":
        "avec un seuil d'avertissement de 0,8 (80 %) dans le profil d'organisation. Le modèle suivant :",
    "* `deviceId` - the device id as uuid string;": "* `deviceId` - l'identifiant de l'appareil (UUID);",
    "* `deviceName` - the device name;": "* `deviceName` - le nom de l'appareil;",
    "* `deviceLabel` - the device label;": "* `deviceLabel` - le libellé de l'appareil;",
    "* `deviceType` - the device type;": "* `deviceType` - le type d'appareil;",
    "* `eventType` - one of: 'inactive', 'active';": "* `eventType` - l'une des valeurs : 'inactive' (inactif), 'active' (actif);",
    "Let's assume the notification about inactive thermometer device 'Sensor T1'.":
        "Prenons une notification indiquant que le thermomètre 'Sensor T1' est inactif.",
    "* `edgeId` - the edge id as uuid string;": "* `edgeId` - l'identifiant d'Edge (UUID);",
    "* `edgeName` - the name of the edge;": "* `edgeName` - le nom d'Edge;",
    "* `failureMsg` - the string representation of the failure, occurred on the Edge;": "* `failureMsg` - la description de l'échec survenu sur Edge;",
    "Let's assume the notification about the failing of processing connection to Edge.":
        "Prenons une notification d'échec de traitement de la connexion à Edge.",
    "* `eventType` - the string representation of the connectivity status: connected or disconnected;":
        "* `eventType` - l'état de la connexion : connected (connecté) ou disconnected (déconnecté);",
    "Let's assume the notification about the connecting Edge into the {{brandName}}.":
        "Prenons une notification de connexion d'Edge à {{brandName}}.",
    "* `entityType` - one of: 'Device', 'Asset', 'User', etc.;": "* `entityType` - l'une des valeurs : 'Device', 'Asset', 'User', etc.;",
    "* `currentCount` - the current count of entities;": "* `currentCount` - le nombre actuel d'entités;",
    "* `limit` - the limit on number of entities;": "* `limit` - la limite du nombre d'entités;",
    "* `percents` - the percent from the notification rule configuration;": "* `percents` - le pourcentage défini dans la règle de notification;",
    "Let's assume the tenant created 400 devices with the max allowed number is 500 and rule threshold 0.8 (80%).":
        "Prenons une organisation qui a créé 400 appareils sur un maximum de 500, avec un seuil de règle de 0,8 (80 %).",
    "* `entityType` - one of: 'Device', 'Asset', 'Customer', 'User', 'Dashboard', 'Rule chain', 'Edge';":
        "* `entityType` - l'une des valeurs : 'Device', 'Asset', 'Customer', 'User', 'Dashboard', 'Rule chain', 'Edge';",
    "* `userEmail` - email of the user who sends the request;": "* `userEmail` - adresse courriel de l'utilisateur qui envoie la demande;",
    "* `increaseLimitActionLabel` - label of the button used to open Limits Management page, for ex: 'Set new limit';":
        "* `increaseLimitActionLabel` - libellé du bouton qui ouvre la page de gestion des limites, p. ex. 'Set new limit';",
    "* `increaseLimitLink` - link to the Limits Management page;": "* `increaseLimitLink` - lien vers la page de gestion des limites;",
    "* `baseUrl` - used to construct the full URL for the Limits Management page in email notifications;":
        "* `baseUrl` - sert à construire l'adresse complète de la page de gestion des limites dans les courriels;",
    "Let's assume the notification about the increasing limit of the maximum allowed devices for the tenant.":
        "Prenons une demande d'augmentation du nombre maximal d'appareils d'une organisation.",
    "* `entityType` - the entity type, e.g. 'Device';": "* `entityType` - le type d'entité, p. ex. 'Device';",
    "* `entityId` - the entity id as uuid string;": "* `entityId` - l'identifiant de l'entité (UUID);",
    "* `entityName` - the name of the entity;": "* `entityName` - le nom de l'entité;",
    "* `actionType` - one of: 'added', 'updated', 'deleted';": "* `actionType` - l'une des valeurs : 'added' (ajoutée), 'updated' (modifiée), 'deleted' (supprimée);",
    "* `userId` - id of the user who made the action;": "* `userId` - identifiant de l'utilisateur qui a fait l'action;",
    "Let's assume the notification about device 'T1' was added by user 'john.doe@gmail.com'.":
        "Prenons une notification indiquant que l'appareil 'T1' a été ajouté par l'utilisateur 'john.doe@gmail.com'.",
    "Let's assume the notification recipient user `John Doe`. The following template:":
        "Prenons le destinataire `John Doe`. Le modèle suivant :",
    "* `latestVersion` - the latest platform version available;": "* `latestVersion` - la dernière version de la plateforme;",
    "* `latestVersionReleaseNotesUrl` - release notes link for latest version;": "* `latestVersionReleaseNotesUrl` - lien vers les notes de version de la dernière version;",
    "* `upgradeInstructionsUrl` - upgrade instructions link for latest version;": "* `upgradeInstructionsUrl` - lien vers les instructions de mise à jour;",
    "* `currentVersion` - the current platform version": "* `currentVersion` - la version actuelle de la plateforme",
    "* `currentVersionReleaseNotesUrl` - release notes link for current version;": "* `currentVersionReleaseNotesUrl` - lien vers les notes de version de la version actuelle;",
    "Let's assume that new 3.5.0 version is released but currently deployed version is 3.4.4. The following template:":
        "Prenons une nouvelle version 3.5.0 publiée alors que la version installée est la 3.4.4. Le modèle suivant :",
    "* `api` - rate-limited API label; one of: 'REST API requests', 'REST API requests per customer', 'transport messages',":
        "* `api` - nom de l'API limitée; l'une des valeurs : 'REST API requests', 'REST API requests per customer', 'transport messages',",
    "'transport messages per device', 'Cassandra queries', 'WS updates per session', 'notification requests', 'notification requests per rule',":
        "'transport messages per device', 'Cassandra queries', 'WS updates per session', 'notification requests', 'notification requests per rule',",
    "'entity version creation', 'entity version load', 'Edge events', 'Edge events per edge', 'Edge uplink messages', 'Edge uplink messages per edge';":
        "'entity version creation', 'entity version load', 'Edge events', 'Edge events per edge', 'Edge uplink messages', 'Edge uplink messages per edge';",
    "* `limitLevelEntityType` - entity type of the limit level entity, e.g. 'Tenant', 'Device', 'Notification rule', 'Customer', etc.;":
        "* `limitLevelEntityType` - type de l'entité visée par la limite, p. ex. 'Tenant', 'Device', 'Notification rule', 'Customer', etc.;",
    "* `limitLevelEntityId` - id of the limit level entity;": "* `limitLevelEntityId` - identifiant de l'entité visée par la limite;",
    "* `limitLevelEntityName` - name of the limit level entity;": "* `limitLevelEntityName` - nom de l'entité visée par la limite;",
    "Let's assume customer 'Customer A' exceeded rate limit for per-customer REST API requests. The following template:":
        "Prenons le client 'Customer A' qui a dépassé la limite de requêtes REST par client. Le modèle suivant :",
    "* `resource` - the resource name (e.g., \"CPU\", \"RAM\", \"STORAGE\");": "* `resource` - le nom de la ressource (p. ex. \"CPU\", \"RAM\", \"STORAGE\");",
    "* `usage` - the current usage value of the resource;": "* `usage` - l'utilisation actuelle de la ressource;",
    "* `serviceId` - the service id (convenient in cluster setup);": "* `serviceId` - l'identifiant du service (utile en grappe);",
    "* `serviceType` - the service type (convenient in cluster setup);": "* `serviceType` - le type de service (utile en grappe);",
    "Let's assume there is a resource usage shortage and the system is low on free resources (CPU, RAM, or Storage).":
        "Prenons un système qui manque de ressources libres (processeur, mémoire ou stockage).",
    "* `componentType` - one of: 'Rule chain', 'Rule node';": "* `componentType` - l'une des valeurs : 'Rule chain' (chaîne de règles), 'Rule node' (nœud de règle);",
    "* `componentId` - the component id as uuid string;": "* `componentId` - l'identifiant du composant (UUID);",
    "* `componentName` - the rule chain or rule node name;": "* `componentName` - le nom de la chaîne ou du nœud de règle;",
    "* `ruleChainId` - the rule chain id as uuid string;": "* `ruleChainId` - l'identifiant de la chaîne de règles (UUID);",
    "* `ruleChainName` - the rule chain name;": "* `ruleChainName` - le nom de la chaîne de règles;",
    "* `eventType` - one of: 'started', 'updated', 'stopped';": "* `eventType` - l'une des valeurs : 'started' (démarré), 'updated' (mis à jour), 'stopped' (arrêté);",
    "* `action` - one of: 'start', 'update', 'stop';": "* `action` - l'une des valeurs : 'start' (démarrer), 'update' (mettre à jour), 'stop' (arrêter);",
    "* `error` - the error text;": "* `error` - le texte de l'erreur;",
    "Let's assume the notification about misconfigured Kafka rule node. The following template:":
        "Prenons une notification au sujet d'un nœud de règle Kafka mal configuré. Le modèle suivant :",
    "* values from the incoming message metadata referenced using the metadata key name;":
        "* les valeurs des métadonnées du message entrant, désignées par le nom de la clé de métadonnées;",
    "* values from the incoming message data referenced using the data key name;":
        "* les valeurs des données du message entrant, désignées par le nom de la clé de données;",
    "* `originatorType` - type of the originator, e.g. 'Device';": "* `originatorType` - type de l'émetteur, p. ex. 'Device';",
    "* `originatorId` - id of the originator": "* `originatorId` - identifiant de l'émetteur",
    "* `customerId` - id of the customer if any": "* `customerId` - identifiant du client, s'il y en a un",
    "* `msgType` - type of the message": "* `msgType` - type du message",
    "Let's assume the incoming message to Rule node has the following data:":
        "Prenons un message entrant dans le nœud de règle avec les données suivantes :",
    "* `taskType` - the task type, e.g. 'telemetry deletion';": "* `taskType` - le type de tâche, p. ex. 'telemetry deletion';",
    "* `taskDescription` - the task description, e.g. 'telemetry deletion for device c4d93dc0-63a1-11ee-aa6d-f7cbc0a71325';":
        "* `taskDescription` - la description de la tâche, p. ex. 'telemetry deletion for device c4d93dc0-63a1-11ee-aa6d-f7cbc0a71325';",
    "* `error` - the error stacktrace": "* `error` - la trace de l'erreur",
    "* `tenantId` - the tenant id;": "* `tenantId` - l'identifiant de l'organisation;",
    "* `entityType` - the type of the entity to which the task is related;": "* `entityType` - le type d'entité visée par la tâche;",
    "* `entityId` - the id of the entity to which the task is related;": "* `entityId` - l'identifiant de l'entité visée par la tâche;",
    "* `attempt` - the number of attempts processing the task": "* `attempt` - le nombre de tentatives de traitement de la tâche",
    "Let's assume that telemetry deletion failed for some device.": "Prenons une suppression de télémesures qui a échoué pour un appareil.",
    # certificates
    "##### X509 Certificate Chain info": "##### Chaîne de certificats X.509",
    "X.509 certificates strategy is used to provision devices by client certificates in two-way TLS communication.":
        "La stratégie par certificats X.509 sert à provisionner des appareils avec leur certificat client, en TLS bidirectionnel.",
    "* check for pre-provisioned devices": "* vérifier les appareils déjà provisionnés",
    "* update X.509 device credentials": "* mettre à jour les identifiants X.509 des appareils",
    "* create new devices": "* créer de nouveaux appareils",
    "If a device already exists with outdated X.509 credentials, this strategy automatically updates it with the device certificate's credentials from the chain.":
        "Si un appareil existe déjà avec des identifiants X.509 périmés, cette stratégie les remplace automatiquement par ceux du certificat de la chaîne.",
    "#### Examples of RegEx usage": "#### Exemples d'expressions régulières",
    "The regular expression is required to extract device name from the X509 certificate's common name.":
        "L'expression régulière sert à extraire le nom de l'appareil du nom commun (CN) du certificat X.509.",
    "The regular expression syntax is based on Java [Pattern](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html).":
        "La syntaxe est celle de Java [Pattern](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html).",
    "You may also use this [resource](https://regex101.com/) to test your expressions but make sure you select Java 8 flavor.":
        "Vous pouvez tester vos expressions sur [regex101](https://regex101.com/) en choisissant la variante Java 8.",
    "* **Pattern:**<code>(.*)\\.company.com</code>- matches any characters before the \".company.com\".":
        "* **Expression :**<code>(.*)\\.company.com</code> - correspond à tous les caractères avant « .company.com ».",
    "* **Pattern:** <code>(.*)@company.com</code>- matches any characters before the \"@company.com\".":
        "* **Expression :** <code>(.*)@company.com</code> - correspond à tous les caractères avant « @company.com ».",
    "* **Pattern:** <code>prefix(.*)suffix@company.com</code>- matches characters between \"prefix\" and \"suffix@company.com\".":
        "* **Expression :** <code>prefix(.*)suffix@company.com</code> - correspond aux caractères entre « prefix » et « suffix@company.com ».",
    "* **Pattern:** <code>\\\\D+\\\\.(.*)\\\\.\\\\d+@company.com</code>- matches characters between not digits prefix followed by period and sequence of digits with \"@company.com\" ending.":
        "* **Expression :** <code>\\\\D+\\\\.(.*)\\\\.\\\\d+@company.com</code> - correspond aux caractères entre un préfixe sans chiffres suivi d'un point, et une suite de chiffres terminée par « @company.com ».",
    # date formats
    "#### Pre-defined format options": "#### Formats prédéfinis",
    "#### Custom format options": "#### Formats personnalisés",
    "You can construct a format string using symbols to specify the components":
        "Vous pouvez composer un format avec des symboles qui désignent les parties",
    "of a date-time value, as described in the following table.": "d'une date et d'une heure, comme décrit dans le tableau suivant.",
    "Format details depend on the locale.": "Le résultat dépend de la langue et de la région.",
    "Fields marked with (*) are only available in the extra data set for the given locale.":
        "Les champs marqués (*) ne sont disponibles que dans les données étendues de la langue choisie.",
}

# Words inside the date-format tables (column text only; symbols and examples stay as they are).
DATE_WORDS = [
    ("Option     | Equivalent to                   | Examples (given in `en-US` locale)", "Option     | Équivaut à                      | Exemples (en anglais des É.-U.)"),
    ("Field type          | Format      | Description", "Type de champ       | Format      | Description"),
    ("| Example Value ", "| Exemple       "),
    ("Numeric: minimum digits", "Numérique : chiffres minimum"),
    ("Numeric: 2 digits + zero padded", "Numérique : 2 chiffres, avec zéros"),
    ("Numeric: 3 digits + zero padded (= milliseconds)", "Numérique : 3 chiffres, avec zéros (millisecondes)"),
    ("Numeric: 3 digits + zero padded", "Numérique : 3 chiffres, avec zéros"),
    ("Numeric: 4 digits or more + zero padded", "Numérique : 4 chiffres ou plus, avec zéros"),
    ("Numeric: 1 digit", "Numérique : 1 chiffre"),
    ("Wide (fallback to `a` when missing)", "Long (sinon `a`)"),
    ("| Abbreviated ", "| Abrégé      "), ("| Wide ", "| Long "), ("| Narrow ", "| Étroit "), ("| Short ", "| Court "),
    ("Short specific non location format (fallback to O)", "Format court sans lieu (sinon O)"),
    ("Long specific non location format (fallback to OOOO)", "Format long sans lieu (sinon OOOO)"),
    ("ISO8601 basic format", "Format ISO 8601 de base"),
    ("ISO8601 extended format + Z indicator for offset 0 (= XXXXX)", "Format ISO 8601 étendu + Z pour un décalage nul"),
    ("Long localized GMT format", "Format GMT long localisé"),
    ("Short localized GMT format", "Format GMT court localisé"),
    ("| Era ", "| Ère "), ("| Year ", "| Année "), ("| Week-numbering year", "| Année de semaine  "),
    ("| Month standalone ", "| Mois autonome    "), ("| Month ", "| Mois  "), ("| Week of year ", "| Semaine de l'an"),
    ("| Week of month ", "| Semaine du mois"), ("| Day of month ", "| Jour du mois "),
    ("| Week day standalone", "| Jour autonome      "), ("| Week day ", "| Jour      "),
    ("| Period standalone*", "| Période autonome*"), ("| Period*", "| Période*"), ("| Period ", "| Période"),
    ("| Hour 1-12 ", "| Heure 1-12"), ("| Hour 0-23 ", "| Heure 0-23"), ("| Minute ", "| Minute "),
    ("| Second ", "| Seconde"), ("| Fractional seconds", "| Fractions de s.  "), ("| Zone ", "| Fuseau"),
]


def translate_line(line):
    text = line.strip()
    indent = line[:len(line) - len(line.lstrip())]
    if text in LINES:
        return indent + LINES[text]
    for pattern, build in PATTERNS:
        m = pattern.match(text)
        if m:
            result = build(m)
            if result:
                return indent + result
    if text.startswith("|"):
        out = line
        for en, fr in DATE_WORDS:
            out = out.replace(en, fr)
        return out
    return None


def build(page):
    lines, missing, in_code = [], [], False
    for line in (EN / page).read_text(encoding="utf-8").splitlines():
        text = line.strip()
        if text.startswith("```"):
            in_code = not in_code
            lines.append(line)
            continue
        if in_code or not text or text.startswith("<") or text == "{:copy-code}" or not any(c.isalpha() for c in text):
            lines.append(line)
            continue
        translated = translate_line(line)
        if translated is None:
            missing.append(text)
            lines.append(line)
        else:
            lines.append(translated)
    return "\n".join(lines) + "\n", missing


def main():
    check = "--check" in sys.argv
    problems = []
    for page in PAGES:
        text, missing = build(page)
        problems += ["%s: untranslated: %s" % (page.as_posix(), m) for m in missing]
        target = FR / page
        if check:
            if not target.exists() or target.read_text(encoding="utf-8") != text:
                problems.append("%s: missing or out of date (run python orbitos/i18n/help_fr.py)" % page.as_posix())
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(text, encoding="utf-8", newline="\n")
    for p in problems:
        print(p)
    print("French help pages: %d, problems: %d" % (len(PAGES), len(problems)))
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
