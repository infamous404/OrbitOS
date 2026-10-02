#### Format d'horaire précis

Un attribut à valeur dynamique pour un horaire précis doit contenir du JSON dans le format suivant :

```javascript
{
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
<b>timezone :</b> le fuseau horaire utilisé.
</li>
<li>
<b>daysOfWeek :</b> les jours, sous forme de nombres (lundi = 1, mardi = 2, etc.), où l'horaire est actif.
</li>
<li>
<b>startsOn :</b> l'heure, en millisecondes, à partir de laquelle l'horaire est actif les jours choisis.
</li>
<li>
<b>endsOn :</b> l'heure, en millisecondes, jusqu'à laquelle l'horaire est actif les jours choisis.
</li>
</ul>
Si <b>startsOn</b> et <b>endsOn</b> valent 0, l'horaire est actif toute la journée.
