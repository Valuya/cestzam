package be.valuya.cestzam.client.myminfin.rest;

import be.valuya.cestzam.client.CestzamClientService;
import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.debug.CestzamDebugService;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentIdentifiers;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentKey;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentProvidersResponse;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentStream;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentsListResponse;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.client.response.CestzamResponseService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class MyminfinDocumentsRestClientService {

    private static final String REST_URL = "https://eservices.minfin.fgov.be/myminfin-rest/documents";

    @Inject
    private CestzamClientService cestzamClientService;
    @Inject
    private CestzamDebugService cestzamDebugService;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @Inject
    private CestzamResponseService cestzamResponseService;

    public DocumentProvidersResponse getProviders(CestzamAuthenticatedMyminfinContext myminfinContext) throws CestzamClientError {
        CestzamCookies cookies = myminfinContext.getCookies();
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("minminfin-rest", "documents", "providers");

        HttpResponse<String> userDataResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "providers");
        cestzamResponseService.assertSuccessStatusCode(userDataResponse);
        DocumentProvidersResponse parsedResponse = cestzamResponseService.parseJson(userDataResponse, DocumentProvidersResponse.class);

        return parsedResponse;
    }

    public DocumentsListResponse listProviderDocuments(CestzamAuthenticatedMyminfinContext myminfinContext,
                                                       String provider) throws CestzamClientError {
        CestzamCookies cookies = myminfinContext.getCookies();
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("minminfin-rest", "documents", "all");

        HttpResponse<String> userDataResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "all?provider=" + provider);
        cestzamResponseService.assertSuccessStatusCode(userDataResponse);
        DocumentsListResponse parsedResponse = cestzamResponseService.parseJson(userDataResponse, DocumentsListResponse.class);

        return parsedResponse;
    }


    public DocumentStream downloadDocument(CestzamAuthenticatedMyminfinContext myminfinContext,
                                           DocumentKey documentKey) throws CestzamClientError {
        CestzamCookies cookies = myminfinContext.getCookies();
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String documentId = documentKey.getIdentifiers()
                .getOrDefault(DocumentIdentifiers.FILENET_UUID,
                        documentKey.getIdentifiers().getOrDefault(DocumentIdentifiers.ID, "unknown_document"));
        String debugTag = cestzamDebugService.createFlowDebugTag("minminfin-rest", "documents", "download", documentId);

        String payload = cestzamRequestService.formatJson(documentKey);
        HttpRequest request = cestzamRequestService.createNewPostJsonJsonRequest(payload, REST_URL, "download")
                .build();
        HttpResponse<InputStream> documentResponse = cestzamDebugService.trace(debugTag, request,
                () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).join());
        cestzamDebugService.debugResponse(debugTag, documentResponse, client.cookieHandler());
        cestzamResponseService.assertSuccessStatusCode(documentResponse);

        String fileName = documentResponse.headers().firstValue("content-disposition")
                .flatMap(value -> cestzamResponseService.parseStringGroup1(value, ".*filename=([^;]+).*"))
                .orElse(documentId + ".pdf");
        String mimeType = documentResponse.headers().firstValue("content-type")
                .orElse("application/octet-stream");
        InputStream body = documentResponse.body();

        DocumentStream documentStream = new DocumentStream(fileName, mimeType, body);
        return documentStream;
    }
}
