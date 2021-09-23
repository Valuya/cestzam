# Cestzam

A Belgian public service proxy that gives your back your cestzam cookies.

## Getting started
- Openapi spec deployed at https://cestzam.valuya.be/openapi
- High-level api client provided by the `seame-api-client` artifact
  which is not published on maven central yet
  
  
### API client Usage

Thus using the `cestzam-api-client` artifact.

- Instantiate a client for your service:
```java
MyminfinApiClient myminfinClient = CestzamApiClientBuilder.create()
                .apiUri(URI.create("https://seame.valuya.be"))
                .build()
                .getMyminfin();
```
- Authenticate using the token credentials as an enterprise:
```java
Map<String, String> tokens = Map.of("1", "ABCDEF", "2", "GHIJKL");
myminfinClient.authenticateWithToken(tokenLogin, tokenPassword, tokens, Capacity.ENTERPRISE);
MyminfinUser myminfinUser = myminfinClient.getAuthenticatedUser();
// myminfinUser.visitorType == MyminfinUserType.PRO
``` 
- Activate a mandate granted by a citizen:
```java
List<MyMinfinMandate> availableMandates = myminfinClient.getAvailableMandates(MyminfinMandateType.CITIZEN);
MyminfinMandate mandateToActivate = //...
myminfinClient.setActiveMandate(minfinMandate);
MyminfinUser myminfinUser = myminfinClient.getAuthenticatedUser();
// myminfinUser.visitorType == MyminfinUserType.PRO
// myminfinUser.customerType == MyminfinUserType.customerTypeCITIZEN
// myminfinUser.customerSelected == true
```
- List documents
```java
ResultPage<MyminfinDocumentProvider> providers = myminfinClient.listDocumentsProviders();
MyminfinDocumentProvider provider = //...
MyminfinDocumentFilter documentFilter = new MyminfinDocumentFilter();
documentFilter.setProvider(provider.getName());
documentFilter.setFromDate(LocalDate.of(2020,01,01));
ResultPage<MyminfinDocument> documentResultPage = myminfinClient.searchDocuments(documentFilter);
```
- Download document
```
MyminfinDocument document = //...
MyminfinDocumentStream documentStream = myminfinClient.downloadDocument(document);
Path documentPath = Paths.get("/tmp", documentStream.getFileName());
OutputStream outputStream = Files.newOutputStream(documentPath, StandardOpenOption.CREATE_NEW);
documentStream.getInputStream().transferTo(outputStream);
```


### WS api usage

The ws api uses POST methods taking an opaque `CestzamContext` in the request body.
Requests may return an updated `CestzamContext` in the response body. Clients must keep track
of this state, and always send the most recent one returned in their request body.

Once your are authenticated for your service provider, most request wont alter the state anymore;
but this is not true for requests activating a mandate for instance.
