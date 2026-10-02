#### Horaire toujours actif

Pour un horaire toujours actif, l'argument peut être un objet JSON vide ou un objet JSON dans le format suivant :

```javascript
{
  "type": "ANY_TIME"
}
```

#### Horaire précis

La valeur de l'argument doit être un objet JSON dans le format suivant (le champ `type` est facultatif) :

```javascript
{
  "type": "SPECIFIC_TIME",
  "daysOfWeek": [
    2,
    4
  ],
  "endsOn": 0,
  "startsOn": 0,
  "timezone": "Europe/Kiev"
}
```

<ul>
<li>
<b>timezone :</b> le nom du fuseau horaire.
</li>
<li>
<b>daysOfWeek :</b> les jours de la semaine (lundi = 1, mardi = 2, ..., dimanche = 7) où l'horaire est actif.
</li>
<li>
<b>startsOn :</b> l'heure, en millisecondes depuis le début de la journée (00 h 00), où l'horaire devient actif les jours choisis.
</li>
<li>
<b>endsOn :</b> l'heure, en millisecondes depuis le début de la journée (00 h 00), où l'horaire cesse d'être actif les jours choisis.
Si cette valeur est absente ou vaut 0, l'horaire couvre toute la journée (24 heures en millisecondes).
</li>
</ul>

Si <b>startsOn</b> et <b>endsOn</b> valent 0, l'horaire est actif toute la journée.

#### Horaire personnalisé

La valeur de l'argument doit être un objet JSON dans le format suivant (le champ `type` est facultatif) :

```javascript
{
  "type": "CUSTOM",
  "timezone": "Europe/Kiev",
  "items": [
    {
      "dayOfWeek": 1,
      "enabled": true,
      "endsOn": 0,
      "startsOn": 0
    },
    {
      "dayOfWeek": 2,
      "enabled": true,
      "endsOn": 0,
      "startsOn": 0
    },
    {
      "dayOfWeek": 3,
      "enabled": true,
      "endsOn": 0,
      "startsOn": 0
    },
    {
      "dayOfWeek": 4,
      "enabled": true,
      "endsOn": 0,
      "startsOn": 0
    },
    {
      "dayOfWeek": 5,
      "enabled": true,
      "endsOn": 0,
      "startsOn": 0
    },
    {
      "dayOfWeek": 6,
      "enabled": true,
      "endsOn": 0,
      "startsOn": 0
    },
    {
      "dayOfWeek": 7,
      "enabled": true,
      "endsOn": 0,
      "startsOn": 0
    }
  ]
}
```

<ul>
<li>
<b>timezone :</b> le nom du fuseau horaire.
</li>
<li>
<b>items :</b> la liste des entrées, une par jour.
</li>
</ul>

Chaque entrée représente un jour de la semaine et contient :
<ul>
<li>
<b>dayOfWeek :</b> le numéro du jour (lundi = 1, mardi = 2, ..., dimanche = 7)
</li>
<li>
<b>enabled :</b> valeur <code>boolean</code> qui indique si ce jour est actif dans l'horaire.
</li>
<li>
<b>startsOn :</b> l'heure, en millisecondes depuis le début de la journée (00 h 00), où l'horaire devient actif ce jour-là.
</li>
<li>
<b>endsOn :</b> l'heure, en millisecondes depuis le début de la journée (00 h 00), où l'horaire cesse d'être actif ce jour-là.
Si cette valeur est absente ou vaut 0, l'horaire couvre toute la journée (24 heures en millisecondes).
</li>
</ul>

Si <b>startsOn</b> et <b>endsOn</b> valent 0, l'horaire est actif toute la journée.
