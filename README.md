# Cestzam

A Belgian public service proxy using CZAM tokens for authentication and providing an api to fetch myminfin documents.

[![Maven Central](https://img.shields.io/maven-central/v/be.valuya.cestzam/cestzam-ws)](https://search.maven.org/search?q=g:be.valuya.cestzam%20a:cestzam-ws)
[![Docker](https://img.shields.io/docker/v/valuya/cestzam-ws/master-latest?label=docker)](https://hub.docker.com/r/valuya/cestzam-ws/tags)

## Getting started

A docker-compose file is provided to get you started quickly using docker & docker-compose.

- Clone this repository
- Start the cestzam-ws service

Once this service is started, a proxy serving the api is running on localhost port 18080.

- Adjust your credential in the docker-compose-file.yaml by setting the environment variables for the cestzam-sync
  service
- Start the cestzam-sync service

This service synchronizes the myminfin document with a filesystem copy in target/fs. See below for more information.

Instead of building the images from this repository, you can as well use the published images on docker hub:

- valuya/cestzam-ws:dev-latest
- valuya/cestzam-myminfin-fs-sync:dev-latest

Image are tagged with the release version, as well as ${branch}-latest for the latest build of the master and dev
branches. See https://hub.docker.com/search?q=valuya&type=image.

## Myminfin synchronizer

[![Maven Central](https://img.shields.io/maven-central/v/be.valuya.cestzam/cestzam-myminfin-filesystem-sync)](https://search.maven.org/search?q=g:be.valuya.cestzam%20a:cestzam-myminfin-filesystem-sync)
[![Docker](https://img.shields.io/docker/v/valuya/cestzam-myminfin-fs-sync/master-latest?label=docker)](https://hub.docker.com/r/valuya/cestzam-myminfin-fs-sync/tags)

The cestzam-myminfin-filesystem-sync module provides an executable jar that consumes the cestzam api to synchronize
myminfin documents with a filesystem copy. It assumes an enterprise account is used for authentication, and activates
citizen mandates to fetch the documents.

A directory is created for each citizen mandates. In each of those, a directory is created for each document provider
group. In each of those, a directory is created for each document year. In those, the document pdf is downloaded, and a
.cestzam file is created alongside. The .cestzam file contains an unique identifier to handle documents that would share
the same date, title, year, and provider. Documents already existing on the filesystem are not downloaded on subsequent
run.

The synchronizer iterates over all mandates, activates them one by one, then iterate over each document provider to list
all documents, check if a local copy already exists, and download it if it does not.

In order to run this service, you must provide the following env variables:

- CESTZAM_TARGET_PATH: The local filesystem path in which to place the document. This must be an existing writable
  directory.
- CESTZAM_API_URI: The uri for the cestzam api service.
- CESTZAM_AUTH_LOGIN: The login to use for CZAM authentication using tokens.
- CESTZAM_AUTH_PASSWORD: The password to use for CZAM authentication using tokens.
- CESTZAM_AUTH_TOKENS_JSON: The CZAM authentication tokens, as a json dictionary. Eg: `{"1": "AAAAAA", "2": "BBBBBB"}`.
  There should be 24 tokens, you can generate them and get them sent to you as an email pdf attachment from the czam
  authentication page.

In addition, the following optional env variables allow you to filter which documents to process:

- CESTZAM_MANDATOR_SINGLE_SSIN: If specified, a single mandate will be processed. The mandator ssin must be equal to the
  value of this variable. The default behaviour is to iterate over all mandates available.
- CESTZAM_MANDATOR_NAME_PATTERN: If specified, mandators names will be checked against this regular expression to check
  whether they should be processed or not. Default behaviour is to iterate over all mandates available.
- CESTZAM_DOCUMENT_PROVIDER_NAME_PATTERN: If specified, document provider labels will be checked against this regular
  expression to check whether they should be processed. Default behaviour is to iterate over all document providers
  available for each mandate.
- CESTZAM_DOCUMENT_FROM_DATE: A date in the iso 8601 format, eg `2021-01-01`. If specified, document older than this
  date will be ignored. Default behaviour is to iterate over all documents for each document provider.

## API

The cestzam-ws service provides an api. The specification can be obtained from a running instance at the /openapi
endpoint.

### API client Usage

You can use the cestzam-api-client artifcat, which is a wrapper around microprofile-restclient client instances for the
different resources. Usage should be straightforward, exemples are provided below:

- Instantiate a client for your service:

```java
MyminfinApiClient myminfinClient=CestzamApiClientBuilder.create()
        .apiUri(URI.create("https://seame.valuya.be"))
        .build()
        .getMyminfin();
```

- Authenticate using the token credentials as an enterprise:

```java
Map<String, String> tokens=Map.of("1","ABCDEF","2","GHIJKL");
        myminfinClient.authenticateWithToken(tokenLogin,tokenPassword,tokens,Capacity.ENTERPRISE);
        MyminfinUser myminfinUser=myminfinClient.getAuthenticatedUser();
// myminfinUser.visitorType == MyminfinUserType.PRO
``` 

- Activate a mandate granted by a citizen:

```java
List<MyMinfinMandate> availableMandates=myminfinClient.getAvailableMandates(MyminfinMandateType.CITIZEN);
        MyminfinMandate mandateToActivate= //...
        myminfinClient.setActiveMandate(minfinMandate);
        MyminfinUser myminfinUser=myminfinClient.getAuthenticatedUser();
// myminfinUser.visitorType == MyminfinUserType.PRO
// myminfinUser.customerType == MyminfinUserType.customerTypeCITIZEN
// myminfinUser.customerSelected == true
```

- List documents

```java
ResultPage<MyminfinDocumentProvider> providers=myminfinClient.listDocumentsProviders();
        MyminfinDocumentProvider provider= //...
        MyminfinDocumentFilter documentFilter=new MyminfinDocumentFilter();
        documentFilter.setProvider(provider.getName());
        documentFilter.setFromDate(LocalDate.of(2020,01,01));
        ResultPage<MyminfinDocument> documentResultPage=myminfinClient.searchDocuments(documentFilter);
```

- Download document

```
MyminfinDocument document = //...
MyminfinDocumentStream documentStream = myminfinClient.downloadDocument(document);
Path documentPath = Paths.get("/tmp", documentStream.getFileName());
OutputStream outputStream = Files.newOutputStream(documentPath, StandardOpenOption.CREATE_NEW);
documentStream.getInputStream().transferTo(outputStream);
```

### API usage

The ws api uses POST methods taking an opaque `CestzamContext` in the request body. Requests may return an
updated `CestzamContext` in the response body. Clients must keep track of this state, and always send the most recent
one returned in their request body.

Once your are authenticated for your service provider, most request wont alter the state anymore; but this is not true
for requests activating a mandate for instance.
