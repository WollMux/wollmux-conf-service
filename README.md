# WollMux Konfiugrationsservice

Der WollMux-Konfigurationsservice bietet eine Schnittstelle an, über die verschiedene Produkte wie der WollMux, die WollMuxBar und der Seriendruck auf ihre Konfiguration  zugreifen können. Der Konfigurationsservice soll auf lange Sicht die jetzige Standard-Konfiguration ablösen.

Jedes Produkt kommuniziert nur mit dem Gateway-Service. Jeder Konfigurations-Service meldet sich beim Starten am Gateway-Service an. Dabei gibt er an, für welches Referat er die Konfiguration ausliefert. Der Gateway-Service ermittelt an Hand der URL den Konfigurations-Service, der die Anfrage beantworten kann und leitet sie entsprechend weiter. Der Konfigurations-Service lädt die Standard-Konfiguration und die referatsspezifische Konfiguration und baut sie zu einer Konfiguration zusammen. Die fertige Konfiguration wird abschließend über den Gateway-Service an das Produkt geschickt.

## Vorteile
* TODO: Redundanter Gateway-Service möglich?
* Die WollMux-Konfiguration wird bereits auf dem Server zusammengebaut. Durch einen Cache wird auf Serverseite wird dadurch die Auslieferung der Konfiguration beschleunigt und der WollMux startet schneller.
* Auf dem Server kann gelogt werden welche Vorlagen aktive verwendet werden und dadurch können obsolete Vorlagen identifiziert werden. Zudem wird jede Anfrage nach einer Konfiguration gelogt.
* TODO: Support/Wartbarkeit?

## Was noch fehlt
* Derzeit ist es nicht möglich über HTTPS die Konfiguration auszuliefern. Dies ist aber in einem späteren Schritt geplant.

## Migration
* Es ist weiterhin möglich die Standard-Konfiguration auf das Netzlaufwerk zu legen. Wenn in der Datei wollmux.conf auf diese Konfiguration verwiesen wird, dann lädt der WollMux auch die Konfiguration vom Netzlaufwerk. Daran ändert sich nichts.
* Soll der neue Konfigurationsservice verwendet werden so muss die Datei wollmux.conf angepasst werden. Die %Include-Anweisungen müssen auf die neuen URLs umgestellt werden.
* Beim Starten des Konfigurations-Service muss angegeben werden unter welcher URL die Konfiguration zu finden ist. Bei der URL kann es sich sowohl um eine lokale Datei als auch um eine Onlinequelle handeln. Die entsprechende Property ist ENV_CONFSERVICE_PATH
* Es ist also noch möglich die Konfiguration samt Vorlagen selbst zu hosten. Dazu muss der Konfigurations-Service nur mit dem richtigen Gateway-Service verbunden werden. Dies lässt sich über die Property ENV_CONFSERVICE_IP festlegen.

## Beispiel
Die Konfiguration von IT@M soll über einen Konfigurationsservice bereitgestellt werden. Dazu wird ein Gateway-Service auf Port 9090 gestartet. Da der Konfigurations-Service auf der gleichen Maschine (itm.wollmuxservice.muenchen.de) gestartet wird ist es nicht notwendig die Variable ENV_CONFSERVICE_IP zu setzen.
* Starten eines Gateway-Service
      java -jar -DENV_CONFSERVICE_PORT=9090 conf-service-gateway-<version>-fat.jar &
* Starten des Konfigurations-Service
      java -jar -DENV_CONFSERVICE_UNIT=itm -DENV_CONFSERVICE_PATH=http://itm.wollmux.muenchen.de conf-service-<version>-fat.jar &
* Anpassungen in der Datei wollmux.conf
      %include "http://itm.wollmuxservice.muenchen.de:9090/api/v1/itm/conf/main.conf"
      %include "http://itm.wollmuxservice.muenchen.de:9090/api/v1/itm/conf/wollmuxbar_standard.conf"
      DEFAULT_CONTEXT "http://itm.wollmuxservice.muenchen.de:9090/api/v1/itm/"
* Wenn der WollMux nun die Datei wollmux.conf ausliest, dann fragt er beim Gateway-Service nach der api/v1/**itm**/conf/main.conf. Der Gateway-Service erkennt an diesem Pfad, dass die Konfiguration von IT@M gefragt ist und leitet die Anfrage deshalb an den Konfigurations-Service, der sich unter der Unit **itm** registriert hat, weiter. Dort werden dann alle %include-Anweisungen durch den Konfigurations-Service aus der Datei main.conf abgearbeitet und die vollständige Konfiguration wird über den Gateway-Service an den WollMux ausgeliefert.

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
