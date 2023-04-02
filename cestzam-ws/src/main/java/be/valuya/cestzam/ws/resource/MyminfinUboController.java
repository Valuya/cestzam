package be.valuya.cestzam.ws.resource;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import be.valuya.cestzam.api.service.myminfin.ubo.MyminfinUboResource;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompany;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanyCompositionNode;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanySearch;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanySearchResult;
import be.valuya.cestzam.api.service.myminfin.ubo.UboRequestContext;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;
import be.valuya.cestzam.client.myminfin.rest.MyminfinUboRestClientService;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompaniesSearchRequest;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompaniesSearchResults;
import be.valuya.cestzam.client.myminfin.rest.ubo.Company;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompositionTreeNode;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompositionTreeRequest;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.ws.converter.CestzamConverterService;
import be.valuya.cestzam.ws.converter.UboCompanyCompositionConverter;
import be.valuya.cestzam.ws.converter.UboCompanyConverter;
import be.valuya.cestzam.ws.converter.UboCompanySearchConverter;
import be.valuya.cestzam.ws.util.ConfigParam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@ApplicationScoped
public class MyminfinUboController implements MyminfinUboResource {
    @Inject
    private MyminfinUboRestClientService myminfinUboRestClientService;
    @Inject
    private UboCompanySearchConverter uboCompanySearchConverter;
    @Inject
    private UboCompanyCompositionConverter uboCompanyCompositionConverter;
    @Inject
    private UboCompanyConverter uboCompanyConverter;
    @Inject
    private CestzamConverterService cestzamConverterService;
    @Context
    private HttpServletResponse servletResponse;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @BeanParam
    private ConfigParam configParam;


    @Override
    public UboCompanySearchResult searchCompanies(UboCompanySearch companySearch) {
        AuthenticatedMyminfinContext myminfinContext = companySearch.getMyminfinContext();
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        CompaniesSearchRequest searchRequest = uboCompanySearchConverter.convertCompanySearch(companySearch);
        Optional.ofNullable(configParam.getLanguage())
                .ifPresent(searchRequest::setLanguage);

        try {
            CompaniesSearchResults companiesSearchResults = myminfinUboRestClientService.getCompaniesSearchResults(authenticatedMyminfinContext.getCookies(), searchRequest);
            return uboCompanySearchConverter.convertCompanySearchResult(companiesSearchResults);
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public UboCompanyCompositionNode getCompanyComposition(String companyId, UboRequestContext requestContext) {
        AuthenticatedMyminfinContext myminfinContext = requestContext.getMyminfinContext();
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        CompositionTreeRequest searchRequest = new CompositionTreeRequest();
        Optional.ofNullable(configParam.getLanguage())
                .ifPresent(searchRequest::setLanguage);

        try {
            CompositionTreeNode companyCompositionTree = myminfinUboRestClientService.getCompanyCompositionTree(authenticatedMyminfinContext.getCookies(), companyId, searchRequest);
            return uboCompanyCompositionConverter.convertCompositionNode(companyCompositionTree);
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public UboCompany confirmCompanyComposition(String companyId, UboRequestContext requestContext) {
        AuthenticatedMyminfinContext myminfinContext = requestContext.getMyminfinContext();
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        try {
            Company company = myminfinUboRestClientService.confirmCompany(authenticatedMyminfinContext.getCookies(), companyId);
            return uboCompanyConverter.convertUboCompany(company);
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public InputStream getDocumentContent(Long documentId, UboRequestContext requestContext) {
        AuthenticatedMyminfinContext myminfinContext = requestContext.getMyminfinContext();
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        try {
            InputStream inputStream = myminfinUboRestClientService.getDocument(authenticatedMyminfinContext.getCookies(), documentId);
            return inputStream;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public void close() throws IOException {

    }
}
