#### Formats prédéfinis

| Option     | Équivaut à                      | Exemples (en anglais des É.-U.)            |
|------------|---------------------------------|-----------------------------------------------|
| short      | M/d/yy, h:mm a                  | 6/15/15, 9:03 AM                              |
| medium     | MMM d, y, h:mm:ss a             | Jun 15, 2015, 9:03:01 AM                      |
| long       | MMMM d, y, h:mm:ss a z          | June 15, 2015 at 9:03:01 AM GMT+1             |
| full       | EEEE, MMMM d, y, h:mm:ss a zzzz | Monday, June 15, 2015 at 9:03:01 AM GMT+01:00 |
| shortDate  | M/d/yy                          | 6/15/15                                       |
| mediumDate | MMM d, y                        | Jun 15, 2015                                  |
| longDate   | MMMM d, y                       | June 15, 2015                                 |
| fullDate   | EEEE, MMMM d, y                 | Monday, June 15, 2015                         |
| shortTime  | h:mm a                          | 9:03 AM                                       |
| mediumTime | h:mm:ss a                       | 9:03:01 AM                                    |
| longTime   | h:mm:ss a z                     | 9:03:01 AM GMT+1                              |
| fullTime   | h:mm:ss a zzzz                  | 9:03:01 AM GMT+01:00                          |

#### Formats personnalisés

Vous pouvez composer un format avec des symboles qui désignent les parties
d'une date et d'une heure, comme décrit dans le tableau suivant.
Le résultat dépend de la langue et de la région.
Les champs marqués (*) ne sont disponibles que dans les données étendues de la langue choisie.

| Type de champ       | Format      | Description                                                  | Exemple                                                    |
|---------------------|-------------|--------------------------------------------------------------|------------------------------------------------------------|
| Ère                 | G, GG & GGG | Abrégé                                                       | AD                                                         |
|                     | GGGG        | Long                                                         | Anno Domini                                                |
|                     | GGGGG       | Étroit                                                       | A                                                          |
| Année                | y           | Numérique : chiffres minimum                                      | 2, 20, 201, 2017, 20173                                    |
|                     | yy          | Numérique : 2 chiffres, avec zéros                              | 02, 20, 01, 17, 73                                         |
|                     | yyy         | Numérique : 3 chiffres, avec zéros                              | 002, 020, 201, 2017, 20173                                 |
|                     | yyyy        | Numérique : 4 chiffres ou plus, avec zéros                      | 0002, 0020, 0201, 2017, 20173                              |
| Année de semaine   | Y           | Numérique : chiffres minimum                                      | 2, 20, 201, 2017, 20173                                    |
|                     | YY          | Numérique : 2 chiffres, avec zéros                              | 02, 20, 01, 17, 73                                         |
|                     | YYY         | Numérique : 3 chiffres, avec zéros                              | 002, 020, 201, 2017, 20173                                 |
|                     | YYYY        | Numérique : 4 chiffres ou plus, avec zéros                      | 0002, 0020, 0201, 2017, 20173                              |
| Mois                | M           | Numérique : 1 chiffre                                             | 9, 12                                                      |
|                     | MM          | Numérique : 2 chiffres, avec zéros                              | 09, 12                                                     |
|                     | MMM         | Abrégé                                                       | Sep                                                        |
|                     | MMMM        | Long                                                         | September                                                  |
|                     | MMMMM       | Étroit                                                       | S                                                          |
| Mois autonome       | L           | Numérique : 1 chiffre                                             | 9, 12                                                      |
|                     | LL          | Numérique : 2 chiffres, avec zéros                              | 09, 12                                                     |
|                     | LLL         | Abrégé                                                       | Sep                                                        |
|                     | LLLL        | Long                                                         | September                                                  |
|                     | LLLLL       | Étroit                                                       | S                                                          |
| Semaine de l'an       | w           | Numérique : chiffres minimum                                      | 1... 53                                                    |
|                     | ww          | Numérique : 2 chiffres, avec zéros                              | 01... 53                                                   |
| Semaine du mois      | W           | Numérique : 1 chiffre                                             | 1... 5                                                     |
| Jour du mois        | d           | Numérique : chiffres minimum                                      | 1                                                          |
|                     | dd          | Numérique : 2 chiffres, avec zéros                              | 01                                                         |
| Jour                 | E, EE & EEE | Abrégé                                                       | Tue                                                        |
|                     | EEEE        | Long                                                         | Tuesday                                                    |
|                     | EEEEE       | Étroit                                                       | T                                                          |
|                     | EEEEEE      | Court                                                        | Tu                                                         |
| Jour autonome       | c, cc       | Numérique : 1 chiffre                                             | 2                                                          |
|                     | ccc         | Abrégé                                                       | Tue                                                        |
|                     | cccc        | Long                                                         | Tuesday                                                    |
|                     | ccccc       | Étroit                                                       | T                                                          |
|                     | cccccc      | Court                                                        | Tu                                                         |
| Période             | a, aa & aaa | Abrégé                                                       | am/pm or AM/PM                                             |
|                     | aaaa        | Long (sinon `a`)                          | ante meridiem/post meridiem                                |
|                     | aaaaa       | Étroit                                                       | a/p                                                        |
| Période*             | B, BB & BBB | Abrégé                                                       | mid.                                                       |
|                     | BBBB        | Long                                                         | am, pm, midnight, noon, morning, afternoon, evening, night |
|                     | BBBBB       | Étroit                                                       | md                                                         |
| Période autonome*  | b, bb & bbb | Abrégé                                                       | mid.                                                       |
|                     | bbbb        | Long                                                         | am, pm, midnight, noon, morning, afternoon, evening, night |
|                     | bbbbb       | Étroit                                                       | md                                                         |
| Heure 1-12          | h           | Numérique : chiffres minimum                                      | 1, 12                                                      |
|                     | hh          | Numérique : 2 chiffres, avec zéros                              | 01, 12                                                     |
| Heure 0-23          | H           | Numérique : chiffres minimum                                      | 0, 23                                                      |
|                     | HH          | Numérique : 2 chiffres, avec zéros                              | 00, 23                                                     |
| Minute              | m           | Numérique : chiffres minimum                                      | 8, 59                                                      |
|                     | mm          | Numérique : 2 chiffres, avec zéros                              | 08, 59                                                     |
| Seconde             | s           | Numérique : chiffres minimum                                      | 0... 59                                                    |
|                     | ss          | Numérique : 2 chiffres, avec zéros                              | 00... 59                                                   |
| Fractions de s.    | S           | Numérique : 1 chiffre                                             | 0... 9                                                     |
|                     | SS          | Numérique : 2 chiffres, avec zéros                              | 00... 99                                                   |
|                     | SSS         | Numérique : 3 chiffres, avec zéros (millisecondes)             | 000... 999                                                 |
| Fuseau               | z, zz & zzz | Court specific non location format (fallback to O)           | GMT-8                                                      |
|                     | zzzz        | Format long sans lieu (sinon OOOO)         | GMT-08:00                                                  |
|                     | Z, ZZ & ZZZ | Format ISO 8601 de base                                         | -0800                                                      |
|                     | ZZZZ        | Format GMT long localisé                                    | GMT-8:00                                                   |
|                     | ZZZZZ       | Format ISO 8601 étendu + Z pour un décalage nul | -08:00                                                     |
|                     | O, OO & OOO | Court localized GMT format                                   | GMT-8                                                      |
|                     | OOOO        | Format GMT long localisé                                    | GMT-08:00                                                  |
