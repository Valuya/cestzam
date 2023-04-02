package be.valuya.cestzam.client.myminfin.rest;

import be.valuya.cestzam.client.CestzamClientService;
import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.debug.CestzamDebugService;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.rest.ubo.CanManage;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompaniesSearchRequest;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompaniesSearchResults;
import be.valuya.cestzam.client.myminfin.rest.ubo.Company;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompanyControl;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompositionTreeNode;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompositionTreeRequest;
import be.valuya.cestzam.client.myminfin.rest.ubo.ControlRequest;
import be.valuya.cestzam.client.myminfin.rest.ubo.GetCompanyRequest;
import be.valuya.cestzam.client.myminfin.rest.ubo.KeepAlive;
import be.valuya.cestzam.client.myminfin.rest.ubo.UserInfo;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.client.response.CestzamResponseService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class MyminfinUboRestClientService {

    private static final String REST_URL = "https://eservices.minfin.fgov.be/ubo/api";

    @Inject
    private CestzamClientService cestzamClientService;
    @Inject
    private CestzamDebugService cestzamDebugService;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @Inject
    private CestzamResponseService cestzamResponseService;


    public UserInfo getUserInfo(CestzamCookies cookies) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("ubo-api", "userInfo");

        HttpResponse<String> userDataResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "users/get-user-info");
        cestzamResponseService.assertSuccessStatusCode(userDataResponse);
        UserInfo userInfo = cestzamResponseService.parseJson(userDataResponse, UserInfo.class);

        return userInfo;
    }


    public String getApplicationVersion(CestzamCookies cookies) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("ubo-api", "appVersion");

        HttpResponse<String> versionResponse = cestzamRequestService.getTextPlain(debugTag, client, REST_URL, "application/version");
        cestzamResponseService.assertSuccessStatusCode(versionResponse);
        return versionResponse.body();
    }


    public KeepAlive getKeepAlive(CestzamCookies cookies) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("ubo-api", "keepalive");

        HttpResponse<String> keepAliveResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "application/keepalive");
        cestzamResponseService.assertSuccessStatusCode(keepAliveResponse);

        KeepAlive keepAlive = cestzamResponseService.parseJson(keepAliveResponse, KeepAlive.class);
        return keepAlive;
    }


    public CompaniesSearchResults getCompaniesSearchResults(CestzamCookies cookies, CompaniesSearchRequest searchRequest) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("ubo-api", "searchCompanies");

        Map<String, String> queryParams = new HashMap<>();
        Optional.ofNullable(searchRequest.getIdentificationNumber())
                .ifPresent(n -> queryParams.put("identificationNumber", n));
        Optional.ofNullable(searchRequest.getLegalForm())
                .ifPresent(n -> queryParams.put("legalForm", n));
        Optional.ofNullable(searchRequest.getName())
                .ifPresent(n -> queryParams.put("name", n));
        Optional.ofNullable(searchRequest.getLanguage())
                .ifPresent(n -> queryParams.put("language", n));
        Optional.ofNullable(searchRequest.getPage())
                .ifPresent(n -> queryParams.put("page", n.toString()));
        Optional.ofNullable(searchRequest.getLimit())
                .ifPresent(n -> queryParams.put("limit", n.toString()));
        String queryString = cestzamRequestService.encodeBodyFormData(queryParams);

        HttpResponse<String> searchResultResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "companies/search?" + queryString);
        cestzamResponseService.assertSuccessStatusCode(searchResultResponse);

        CompaniesSearchResults searchResults = cestzamResponseService.parseJson(searchResultResponse, CompaniesSearchResults.class);
        return searchResults;
    }

    public Company getCompany(CestzamCookies cookies, String companyId, GetCompanyRequest getCompanyRequest) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("ubo-api", "getCompany");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("admin", getCompanyRequest.isAdmin() ? "true" : "false");
        if (getCompanyRequest.getLanguage() != null) {
            queryParams.put("language", getCompanyRequest.getLanguage());
        }
        String queryString = cestzamRequestService.encodeBodyFormData(queryParams);

        HttpResponse<String> getCompanyResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "companies", companyId, "?" + queryString);
        cestzamResponseService.assertSuccessStatusCode(getCompanyResponse);

        Company company = cestzamResponseService.parseJson(getCompanyResponse, Company.class);
        return company;
    }

    public Boolean canManagetCompany(CestzamCookies cookies, String companyId) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("ubo-api", "canManageCompany");

        HttpResponse<String> canManageResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "users/can-manage", companyId);
        cestzamResponseService.assertSuccessStatusCode(canManageResponse);

        CanManage canManage = cestzamResponseService.parseJson(canManageResponse, CanManage.class);
        return canManage.getCanManage();
    }


    public CompositionTreeNode getCompanyCompositionTree(CestzamCookies cookies, String companyId, CompositionTreeRequest compositionTreeRequest) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("ubo-api", "commpanyCompositionTree");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("includeOthers", compositionTreeRequest.isIncludeOthers() ? "true" : "false");
        queryParams.put("withComposition", compositionTreeRequest.isWithComposition() ? "true" : "false");
        if (compositionTreeRequest.getLanguage() != null) {
            queryParams.put("language", compositionTreeRequest.getLanguage());
        }
        String queryString = cestzamRequestService.encodeBodyFormData(queryParams);

        HttpResponse<String> compoistionTreeResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "companies", companyId, "composition-tree", "?" + queryString);
        cestzamResponseService.assertSuccessStatusCode(compoistionTreeResponse);

        CompositionTreeNode compositionTreeNode = cestzamResponseService.parseJson(compoistionTreeResponse, CompositionTreeNode.class);
        return compositionTreeNode;
    }


    public List<CompanyControl> getCompanyControls(CestzamCookies cookies, String companyId, ControlRequest controlRequest) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("ubo-api", "companyControls");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("inactiveDocuments", controlRequest.isInactiveDocuments() ? "true" : "false");
        String queryString = cestzamRequestService.encodeBodyFormData(queryParams);

        HttpResponse<String> compoistionTreeResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "companies", companyId, "controls", "?" + queryString);
        cestzamResponseService.assertSuccessStatusCode(compoistionTreeResponse);


        Type collectionType = new ArrayList<CompanyControl>() {
        }.getClass().getGenericSuperclass();
        List<CompanyControl> companyControlList = cestzamResponseService.parseJson(compoistionTreeResponse, collectionType);
        return companyControlList;
    }


    public Company confirmCompany(CestzamCookies cookies, String companyId) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("ubo-api", "confirm");

        HttpResponse<String> compoistionTreeResponse = cestzamRequestService.postJsonGetJson(debugTag, client, Map.of(), null, REST_URL, "companies", companyId, "confirm");
        cestzamResponseService.assertSuccessStatusCode(compoistionTreeResponse);

        Company company = cestzamResponseService.parseJson(compoistionTreeResponse, Company.class);
        return company;
    }

    public InputStream getDocument(CestzamCookies cookies, Long documentId) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("ubo-api", "getDocument");

        //eservices.minfin.fgov.be/ubo/api/documents/19462292
        HttpResponse<InputStream> documentResponse = cestzamRequestService.getSlowStream(debugTag, client, REST_URL, "documents", documentId.toString());
        cestzamResponseService.assertSuccessStatusCode(documentResponse);

        return documentResponse.body();
    }


}
