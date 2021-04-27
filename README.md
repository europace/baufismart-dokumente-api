# Dokumente-API
Die API ermöglicht das Hinzufügen von Dokumente zu einem Vorgang bzw. Antrag und das Herunterladen eines bekannten Dokumentes.

![Vertrieb](https://img.shields.io/badge/-Vertrieb-lightblue)
![Produktanbieter](https://img.shields.io/badge/-Produktanbieter-lightblue)
![Baufinanzierung](https://img.shields.io/badge/-Baufinanzierung-lightblue)

[![Authentication](https://img.shields.io/badge/Auth-OAuth2-green)](https://github.com/europace/authorization-api)
[![GitHub release](https://img.shields.io/github/v/release/europace/baufismart-ereignisse-api)](https://github.com/europace/baufismart-dokumente-api/releases)

## Dokumentation
[![YAML](https://img.shields.io/badge/OAS-YAML-lightgrey)](https://raw.githubusercontent.com/europace/baufismartdokumente-api/master/dokumente-openapi.yaml)

## Anwendungsfälle der API
- vom Vertrieb generierte Vertriebsdokumente dem Vorgang hinzufügen
- vom Produktanbieter generierte Anträge/Verträge oder benötigte Formulare dem Vertrieb über den Antrag hinzufügen
- ein bekanntes Dokument herunterladen


> **_Achtung:_**  Antragsteller-Dokumente wie Unterlagen und Nachweise sollten über die [Unterlagen-API](https://docs.api.europace.de/baufinanzierung/unterlagen/unterlagen-api/) verarbeitet werden.


# Schnellstart
Damit du unsere APIs und deinen Anwendungsfall schnellstmöglich testen kannst, haben wir eine [Postman-Collection](https://docs.api.europace.de/baufinanzierung/schnellstart/) für dich zusammengestellt.

### Authentifizierung
Bitte benutze [![Authentication](https://img.shields.io/badge/Auth-OAuth2-green)](https://docs.api.europace.de/baufinanzierung/authentifizierung/), um Zugang zur API bekommen. Um die API verwenden zu können, benötigt der OAuth2-Client folgende Scopes:

| Scope                                  | API Usecase                                   |
|----------------------------------------|-----------------------------------------------|
| `dokumente:dokument:schreiben`         | Dokument hinzufügen                           |


## Funktionsweise
Ein hochgeladenes Dokument besteht aus binär Daten (z.B. einem PDF oder Bild) und dessen Metadaten. Die Schnittstelle ist streaming fähig. Hochgeladene Dokumente erscheinen wie alle anderen Dokumente in der Dokumenten-Lasche innerhalb eines BaufiSmart Vorgang.

## Beispiel: Dokument einem Vorgang hinzufügen

Der Vertrieb möchte ein selbsterzeugtes Angebotsdokument dem Vorgang hinzufügen, damit es über Finn dem Antragsteller zur Verfügung gestellt werden kann.

Request:
``` html
POST /vorgang/dokumente HTTP/1.1
Host: www.europace2.de
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW
Authorization: Bearer eyJraWQiOiJS...
Content-Length: 771

----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="bestesAngebot.pdf"
Content-Type: application/pdf

(data)
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="anzeigename"

bestesAngebot
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="vorgangsNummer"

S719MH
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="teilAntragNummer"


----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="sichtbarFuerVertrieb"

true
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="erstellungsdatum"

2021-03-05T09:45:00.000-01:00
----WebKitFormBoundary7MA4YWxkTrZu0gW
```

Response: 
``` html
201 created
Header: 
    Location: https://www.europace2.de/dokumentenverwaltung/download/?id=21a21f2d8932f5bef262672e8437388f12b0543d8a6a5fbbb1d99999672a3a4829f24a7c2c04461f806d9ad6b05730e2271407b28d6d1740960c24d4fb7f2a05
```

Die Datei kann mit dem Link aus der Header-Variable Location wieder abgerufen werden. Die Id des Dokumentes ist in dem Link enthalten.

## Beispiel: Dokument einem Antrag hinzufügen

Ein Produktanbieter möchte den vollständig ausgefüllten Darlehensantrag bereitstellen und einem Antrag hinzufügen. Dabei ist wichtig, dass die Sichtbarkeit für den Vertrieb (sichtbarFuerVertrieb=true) gewährt wird, damit das Dokument über den Vertrieb an den Antragsteller weitergeleitet werden kann. 

Request:
``` html
POST /vorgang/dokumente HTTP/1.1
Host: www.europace2.de
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW
Authorization: Bearer eyJraWQiOiJS...
Content-Length: 771

----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="Darlehensantrag meineBank.pdf"
Content-Type: application/pdf

(data)
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="anzeigename"

Darlehensantrag meineBank
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="vorgangsNummer"


----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="teilAntragNummer"

S719MH/1/1
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="sichtbarFuerVertrieb"

true
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="erstellungsdatum"

2021-03-05T09:45:00.000-01:00
----WebKitFormBoundary7MA4YWxkTrZu0gW
```

Response: 
``` html
201 created
Header: 
    Location: https://www.europace2.de/dokumentenverwaltung/download/?id=21a21f2d8932f5bef262672e8437388f12b0543d8b6a2fbbb1e99999672a3a4829f24a7c2c04461f806d9ad6b05730e2271407b28d6d1740960c24d4fb7f2a05
```

## Beispiel: Dokument herunterladen

Sofern die Id des Dokumentes nicht bekannt ist, kann sie über die Vorgaenge- oder Antraege-API ermittelt werden.

Request:
```html
GET /dokumentenverwaltung/dokument/?id=21a21f2d8932f5bef262672e8437388f12b0543d8b6a2fbbb1e99999672a3a4829f24a7c2c04461f806d9ad6b05730e2271407b28d6d1740960c24d4fb7f2a05 HTTP/1.1
Host: www.europace2.de
Accept: */*
Authorization: Bearer eyJraWQiOiJS...
``` 

Response:
```html
Header:
    content-disposition: inline; filename="Darlehensantrag meineBank.pdf"
    content-type: application/pdf;charset=UTF-8
Body:
    file-binary
``` 

## Support
Bei Fragen oder Problemen kannst du dich an devsupport@europace2.de wenden.

## Nutzungsbedingungen
Die APIs werden unter folgenden [Nutzungsbedingungen](https://docs.api.europace.de/nutzungsbedingungen/) zur Verfügung gestellt.
