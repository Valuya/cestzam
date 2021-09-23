package be.valuya.cestzam.ws.resource;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocument;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentKey;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentProvider;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentsDownloadRequest;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentsProvidersSearch;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentsResource;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentsSearch;
import be.valuya.cestzam.api.util.ResultPage;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;
import be.valuya.cestzam.client.myminfin.rest.MyminfinDocumentsRestClientService;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentKey;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentProvidersResponse;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentStream;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentsListResponse;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.ws.converter.MymininDocumentConverter;
import be.valuya.cestzam.ws.converter.CestzamConverterService;
import be.valuya.cestzam.ws.util.ConfigParam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class MyminfinDocumentController implements MyminfinDocumentsResource {

    @Inject
    private MyminfinDocumentsRestClientService myminfinDocumentsRestClientService;
    @Inject
    private CestzamConverterService cestzamConverterService;
    @Inject
    private MymininDocumentConverter mymininDocumentConverter;
    @Context
    private HttpServletResponse servletResponse;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @BeanParam
    private ConfigParam configParam;

    @Override
    public ResultPage<MyminfinDocumentProvider> searchDocumentProviders(MyminfinDocumentsProvidersSearch providersSearch) {
        AuthenticatedMyminfinContext myminfinContext = providersSearch.getMyminfinContext();
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);


        try {
            DocumentProvidersResponse providers = myminfinDocumentsRestClientService.getProviders(authenticatedMyminfinContext);
            List<MyminfinDocumentProvider> myminfinDocumentProviders = providers.getGroupsByProvider().entrySet()
                    .stream()
                    .map(e -> mymininDocumentConverter.toMyminfinDocumentProvider(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            ResultPage<MyminfinDocumentProvider> resultPage = new ResultPage<>(myminfinDocumentProviders.size(), myminfinDocumentProviders);
            return resultPage;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public ResultPage<MyminfinDocument> searchDocuments(MyminfinDocumentsSearch documentsSearch) {
        AuthenticatedMyminfinContext myminfinContext = documentsSearch.getMyminfinContext();
        String provider = documentsSearch.getProvider();
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        try {
            DocumentsListResponse documentsListResponse = myminfinDocumentsRestClientService.listProviderDocuments(authenticatedMyminfinContext, provider);
            List<MyminfinDocument> myminfinDocuments = documentsListResponse.getDocumentDescriptionList()
                    .stream()
                    .map(e -> mymininDocumentConverter.toMyminfinDocument(e))
                    .collect(Collectors.toList());
            ResultPage<MyminfinDocument> resultPage = new ResultPage<>(myminfinDocuments.size(), myminfinDocuments);
            return resultPage;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public InputStream downloadDocument(MyminfinDocumentsDownloadRequest downloadRequest) {
        AuthenticatedMyminfinContext myminfinContext = downloadRequest.getMyminfinContext();
        MyminfinDocumentKey myminfinDocumentKey = downloadRequest.getDocumentKey();
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        DocumentKey documentKey = mymininDocumentConverter.toDocumentKey(myminfinDocumentKey);
        try {
            DocumentStream documentStream = myminfinDocumentsRestClientService.downloadDocument(authenticatedMyminfinContext, documentKey);
            InputStream inputStream = documentStream.getInputStream();
            String fileName = documentStream.getFileName();
            String mimeType = documentStream.getMimeType();

            servletResponse.addHeader("content-type", mimeType);
            servletResponse.addHeader("content-disposition", "attachment; filename=" + fileName);
            return inputStream;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public void close() throws IOException {

    }
}
