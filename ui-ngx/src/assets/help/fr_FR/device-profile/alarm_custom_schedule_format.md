#### Format d'horaire personnalisé

Un attribut à valeur dynamique pour un horaire personnalisé doit contenir du JSON dans le format suivant :

```javascript
{
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
<b>timezone :</b> le fuseau horaire utilisé.
</li>
<li>
<b>items :</b> la liste des jours où l'horaire est actif.
</li>
</ul>

Chaque élément de la liste contient les champs suivants :
<ul>
<li>
<b>dayOfWeek :</b> le jour, sous forme de nombre (lundi = 1, mardi = 2, etc.).
</li>
<li>
<b>enabled :</b> valeur <code>boolean</code> qui indique si ce jour est actif dans l'horaire.
</li>
<li>
<b>startsOn :</b> l'heure, en millisecondes, à partir de laquelle l'horaire est actif ce jour-là.
</li>
<li>
<b>endsOn :</b> l'heure, en millisecondes, jusqu'à laquelle l'horaire est actif ce jour-là.
</li>
</ul>
Si <b>startsOn</b> et <b>endsOn</b> valent 0, l'horaire est actif toute la journée.
