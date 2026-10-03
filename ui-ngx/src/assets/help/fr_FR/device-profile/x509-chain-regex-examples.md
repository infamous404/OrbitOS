#### Exemples d'expressions régulières

L'expression régulière sert à extraire le nom de l'appareil du nom commun (CN) du certificat X.509.
La syntaxe est celle de Java [Pattern](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/regex/Pattern.html).
Vous pouvez tester vos expressions sur [regex101](https://regex101.com/) en choisissant la variante Java 8.

* **Expression :**<code>(.*)\.company.com</code> - correspond à tous les caractères avant « .company.com ».
  <br>**CN sample:**<code>DeviceA.company.com</code>
  <br>**Result:**<code>DeviceA</code>

* **Expression :** <code>(.*)@company.com</code> - correspond à tous les caractères avant « @company.com ».
  <br>**CN sample:**<code>DeviceA@company.com</code>
  <br>**Result:**<code>DeviceA</code>

* **Expression :** <code>prefix(.*)suffix@company.com</code> - correspond aux caractères entre « prefix » et « suffix@company.com ».
  <br>**CN sample:**<code>prefixDeviceAsuffix@company.com</code>
  <br>**Pattern matches:** <code>DeviceA</code>

* **Expression :** <code>\\D+\\.(.*)\\.\\d+@company.com</code> - correspond aux caractères entre un préfixe sans chiffres suivi d'un point, et une suite de chiffres terminée par « @company.com ».
  <br>**CN sample:**<code>region.DeviceA.220423@company.com</code>
  <br>**Pattern matches:** <code>DeviceA</code>
