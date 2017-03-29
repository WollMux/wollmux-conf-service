# WollMux Konfiugrationsservice

## Einstellungen
Alle Einstellungen sind sowohl als Umgebungsvariable und als Java Property
möglich.
### Gateway-Service
* **ENV_CONFSERVICE_IP**: Legt den Cluster Host fest. Die IP dient als
Name wenn die Services über den Eventbus miteinander kommunizieren. Der
Default-Wert ist `localhost`.
* **ENV_CONFSERVICE_PORT** (optional): Legt den Port des Gateways fest. Der
Default-Wert ist `8080`.
* **ENV_CONFSERVICE_CHUNK** (optional): Legt die Größe der Http-Response
in Bytes fest. 0 bedeutet die ganze Response wird auf einmal versendet.
Ansonsten wird die Response in kleinere Teile zerlegt und nacheinander
verschickt. Der Default-Wert ist `1000`.

### Referats-Service
* **ENV_CONFSERVICE_UNIT**: Legt den Name des Mikro-Services fest dieser ist
entscheidend um zwischen verschiedenen Konfiguration zu unterscheiden.
* **ENV_CONFSERVICE_PATH**: Legt den Pfad zur WollMux Konfiguration fest.
Bei der URL kann es sich sowohl um einen lokalen Ordner als auch um einen
Ordner, der via HTTP erreichbar ist, handeln.
* **ENV_CONFSERVICE_IP**: Legt den Cluster Host fest. Die IP dient als
Name wenn die Services über den Eventbus miteinander kommunizieren. Der
Default-Wert ist `localhost`.
