# Dokumente-API
The API allows adding documents to a case (Vorgang) or application (Antrag) and downloading a known document.

---- 
![advisor](https://img.shields.io/badge/-advisor-lightblue)
![loanProvider](https://img.shields.io/badge/-loanProvider-lightblue)
![mortgageLoan](https://img.shields.io/badge/-mortgageLoan-lightblue)

[![Authentication](https://img.shields.io/badge/Auth-OAuth2-green)](https://docs.api.europace.de/common/authentifizierung/)
[![GitHub release](https://img.shields.io/github/v/release/europace/baufismart-dokumente-api)](https://github.com/europace/baufismart-dokumente-api/releases)

## Documentation
[![YAML](https://img.shields.io/badge/OAS-HTML_Doc-lightblue)](https://europace.github.io/baufismart-dokumente-api/)
[![YAML](https://img.shields.io/badge/OAS-YAML-lightgrey)](https://raw.githubusercontent.com/europace/baufismartdokumente-api/master/dokumente-openapi.yaml)

## Use cases
- Add sales documents generated by sales to Vorgang
- add applications- or contract-documents and required forms generated by the loan provider to the application adressed to sales
- download a known document


> **_Caution:_** Customer documents for approval purpose should be processed via the [Unterlagen-API](https://docs.api.europace.de/baufinanzierung/unterlagen/unterlagen-api/).


## Quick Start
To help you test our APIs and your use case as quickly as possible, we've put together a [Postman Collection](https://docs.api.europace.de/baufinanzierung/quickstart/) for you.

### Authentication
Please use [![Authentication](https://img.shields.io/badge/Auth-OAuth2-green)](https://docs.api.europace.de/common/authentifizierung/authorization-api/) to get access to the API. The OAuth2 client requires the following scopes:

| Scope                                  | API Usecase                                   |
|----------------------------------------|-----------------------------------------------|
| `dokumente:dokument:schreiben`         | add documents                                 |
| `dokumente:dokument:lesen`             | read documents                                |

## How it works
An uploaded document consists of binary data (e.g. a PDF or image) and its metadata. The interface is streaming capable. Uploaded documents appear like all other documents in the Dokumente-Reiter of the BaufiSmart case.

## Example: Add a document to a case

Sales wants to add a self-generated quote document to the process so that it can be provided to the customer through Finn.

Request:
``` html
POST /vorgang/dokumente HTTP/1.1
Host: www.europace2.de
Content-Type: multipart/form-data; boundary=WebKitFormBoundary7MA4YWxkTrZu0gW
Authorization: Bearer eyJraWQiOiJS...
Content-Length: 771

--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="bestesAngebot.pdf"
Content-Type: application/pdf

(data)
--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="anzeigename"

bestesAngebot
--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="vorgangsNummer"

S719MH
--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="teilAntragNummer"


--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="sichtbarFuerVertrieb"

true
--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="erstellungsdatum"

2021-03-05T09:45:00.000-01:00
--WebKitFormBoundary7MA4YWxkTrZu0gW--
```

The `(data)` is binary without encoding.

Response: 
``` html
201 created
Header: 
    Location: https://www.europace2.de/dokumentenverwaltung/download/?id=21a21f2d8932f5bef262672e8437388f12b0543d8a6a5fbbb1d99999672a3a4829f24a7c2c04461f806d9ad6b05730e2271407b28d6d1740960c24d4fb7f2a05
```

The file can be retrieved with the link from the Location header variable. The document-Id is included in the link.

## Example: Add document to an application

A loan product provider wants to provide the fully completed loan application and add it to an application. It is important that the visibility for sales (sichtbarFuerVertrieb=true) is granted so that the document can be forwarded to the customer via sales. 

Request:
``` html
POST /vorgang/dokumente HTTP/1.1
Host: www.europace2.de
Content-Type: multipart/form-data; boundary=WebKitFormBoundary7MA4YWxkTrZu0gW
Authorization: Bearer eyJraWQiOiJS...
Content-Length: 771

--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="file"; filename="Darlehensantrag meineBank.pdf"
Content-Type: application/pdf

(data)
--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="anzeigename"

Darlehensantrag meineBank
--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="vorgangsNummer"


--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="teilAntragNummer"

S719MH/1/1
--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="sichtbarFuerVertrieb"

true
--WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="erstellungsdatum"

2021-03-05T09:45:00.000-01:00
--WebKitFormBoundary7MA4YWxkTrZu0gW--
```

The `(data)` is binary without encoding.

Response: 
``` html
201 created
Header: 
    Location: https://www.europace2.de/dokumentenverwaltung/download/?id=21a21f2d8932f5bef262672e8437388f12b0543d8b6a2fbbb1e99999672a3a4829f24a7c2c04461f806d9ad6b05730e2271407b28d6d1740960c24d4fb7f2a05
```

## Example: Download document

If the doucment-Id is unknown, it can be determined via the (Antraege-API)[/baufinanzierung/antraege/antraege-api].

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
If you have any questions or problems, please contact helpdesk@europace2.de.

## Terms of use
The APIs are provided under the following [Terms of Use](https://docs.api.europace.de/terms/).
