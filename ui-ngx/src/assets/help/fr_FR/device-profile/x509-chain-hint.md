##### Chaîne de certificats X.509

La stratégie par certificats X.509 sert à provisionner des appareils avec leur certificat client, en TLS bidirectionnel.

<b>This strategy can:</b>
* vérifier les appareils déjà provisionnés
* mettre à jour les identifiants X.509 des appareils
* créer de nouveaux appareils

<b>The user uploads</b> X.509 certificate to the device profile and sets a regular expression to fetch the device name from *Common Name (CN)*.

<b>Client certificates must</b> be signed by X.509 certificate, pre-uploaded for this device profile to provision devices by the strategy. 

<b>The client must</b> establish a TLS connection using the entire chain of certificates (this chain must include device profile X.509 certificate on the last level).

Si un appareil existe déjà avec des identifiants X.509 périmés, cette stratégie les remplace automatiquement par ceux du certificat de la chaîne.

<b>Important:</b> Uploaded certificates should be neither root nor intermediate certificates that are provided by a well-known *Certificate Authority (CA)*.
