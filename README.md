# baufismart-dokumente-api

Diese Schnittstelle ermöglicht es, Dokumente einem Vorgang/Antrag hinzuzufügen oder herunterzuladen.

Diese API wird nicht weiter entwickelt und in Zukunft mit folgender API abgelöst: [dokumente-api](https://github.com/europace/dokumente-api). Der Zeitpunkt ist noch nicht bekannt.

## Dokumentation

### OpenAPi (ehemals swagger)  Spezifikationen
Die API ist vollständig in OpenAPI definiert. EIne Konvertierung in das swagger Format ist mit diesen Dateien auch möglich.

* [openapi.json](https://github.com/europace/baufismart-dokumente-api/blob/master/openapi/openapi.json)
* [openapi.yaml](https://github.com/europace/baufismart-dokumente-api/blob/master/openapi/openapi.yaml)

Diese Spezifikationen können auch zur Generierung von Clients für diese API verwendet
werden. Dazu empfehlen wir das Tool [Swagger Codegen](https://swagger.io/tools/swagger-codegen/)

### Postman Collection

Durch das importieren der Datei  [postman collection](/postman/baufismart-dokumente-api.postman_collection.json) kann man die API Funktionalitäten leicht testen

## API Funktionalitäten

### Download

Dokumente können durch die Dokumenten Id, die beim Upload generiert, und beim Upload im Header zurückgeliefert wird, heruntergeladen werden.

Request Parameter | Beschreibung
-----|-------------
id | Id des Dokuments

### Upload / Import

Ein hochgeladenes Dokument besteht aus binär Daten (z.B. einem PDF oder Bild) und dessen Metadaten. Die Schnittstelle ist streaming fähig. Hochgeladene Dokumente erscheinen wie alle anderen Dokumente in der Dokumenten-Lasche innerhalb eines BaufiSmart Vorgang.

Folgende Request Parameter stehen zur Verfügung:

Request Parameter | Beschreibung
-----|-------------
anzeigename | Name zur Anzeige auf der Oberfläche.
vorgangsNummer | Das Dokument wird diesem Vorgang zugeordnet, wenn keine teilantragnummer gegeben.
teilAntragNummer | Das Dokument wird dem Kreditentscheidungsteilvorgang des Teilantrag zugeordnet. Entweder die 'vorgangsNummer' oder die 'teilAntragNummer' muss angegeben sein.
sichtbarFuerVertrieb | Wenn "true", dann wird das Dokument (auch) dem Beratungsteilvorgang zugeordnet. Wenn eine 'vorgangsNummer' angegeben wird, darf dieser Wert nicht false sein.
erstellungsdatum | Datum an welchem das Dokument erstellt wurde. Format: YYY-MM-DDThh:mm:ss.SSSZ

## Authentifizierung
Die Authentifizierung läuft über den OAuth2 Flow vom Typ ressource owner password credentials flow. https://tools.ietf.org/html/rfc6749#section-1.3.3

### Credentials
Um die Credentials zu erhalten, erfagen Sie beim Helpdesk der Plattform die Zugangsdaten zur Auslesen API, bzw. bitten Ihren Auftraggeber dies zu tun.
Folgende Schritte sind notwendig um ein Token zu generieren und dieses zu nutzen.

#### 1. Login - JWT Erzeugung
Absenden eines POST Requests auf den Login-Endpunkt https://api.europace.de/login mit 'username' und 'password'.
Der Username entspricht der PartnerId und das Password ist der API-Key. Alternativ kann ein Login auch über einen GET Aufruf mit HTTP Basic Auth auf den Login-Endpunkt erfolgen.

Header Parameter | Beschreibung
-----------------|-------------
X-ApiKey         | API des Benutzers / der Organisationen
X-PartnerId      | PartnerId des Benutzers

#### 2. JWT auslesen

Aus der JSON-Antwort das JWToken (access_token) entnehmen

#### 3. JWT beim Upload/Download mitliefern

Bei Upload Requests muss dieser JWToken als 'X-Authenitcation' Header mitgeschickt werden. Beim download als 'Authorization' Header.

Usecase | Header  | Header Value
-----------------|-------------|-------------
Download   |  Authorization     | {{JWT}}
Upload | X-Authenitcation     | Bearer {{JWT}

Wichtig!
Der Benutzer muss neben des JWT auch Zugriff auf den Vorgang bzw. TeilAntrag haben um Dokumente hinzufügen oder runterladen zu können.

# Beispiel

## Upload

Nachfolgendes Beispiel zeigt einen Auschnitt des HTTP Request, welcher die Datei Antrag.pdf in die EUROPACE 2 Plattform importiert und dem Vorgang 123456 zuordnet. Als Antwort wird im Location Header die URI auf das Dokument geliefert.

Besitzt man einen gültigen JWT kann man auch die API per Header Parameter 'X-Authenitcation' nutzen:

Header Parameter | Beschreibung
-----------------|-------------
X-Authenitcation | der JWT 


```
POST https://www.europace2.de/vorgang/dokumente
Content-Type: multipart/form-data
X-ApiKey: 34jklj34h56l
X-PartnerId: 324h6lj21

Content-Type: multipart/form-data; boundary=----------------------------b15d48e6d7db

------------------------------b15d48e6d7db
Content-Disposition: form-data; name="sichtbarFuerVertrieb"

true
------------------------------b15d48e6d7db
Content-Disposition: form-data; name="vorgangsNummer"

123456
------------------------------b15d48e6d7db
Content-Disposition: form-data; name="filename"

Antrag.pdf
------------------------------b15d48e6d7db
Content-Disposition: form-data; name="file"; filename="Antrag.pdf"
Content-Type: application/octet-stream

... content hidden for brevity ...


------------------------------b15d48e6d7db--

```


```
201 CREATED
Location: https://www.europace2.de/dokumentenverwaltung/v97w3945w045ct3576c4w09rczg4twc0r8563458utmwv49vw8e4p57bz45wiovu6e98457c
```


Ein Beispiel-Request kann mit curl erzeugt werden. Bitte alle grossgeschriebenen Platzhalter ersetzen.

```
curl -v -H "X-PartnerId: PARTNER_ID" -H "X-ApiKey: API_KEY"   -F "vorgangsNummer=V_NR" -F "filename=DATEI_NAME"  -F "file=@DATEI_NAME" -F "sichtbarFuerVertrieb=true" https://www.europace2.de/vorgang/dokumente
```

