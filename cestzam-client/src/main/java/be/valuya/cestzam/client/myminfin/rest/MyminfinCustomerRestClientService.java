package be.valuya.cestzam.client.myminfin.rest;

import be.valuya.cestzam.client.CestzamClientService;
import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.czam.CzamCitizenInfo;
import be.valuya.cestzam.client.debug.CestzamDebugService;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;
import be.valuya.cestzam.client.myminfin.rest.mandate.ApplicationMandate;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.client.response.CestzamResponseService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class MyminfinCustomerRestClientService {

    private static final String REST_URL = "https://eservices.minfin.fgov.be/myminfin-rest/customer";

    @Inject
    private CestzamClientService cestzamClientService;
    @Inject
    private CestzamDebugService cestzamDebugService;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @Inject
    private CestzamResponseService cestzamResponseService;

    public List<ApplicationMandate> getCitizenMandates(CestzamAuthenticatedMyminfinContext myminfinContext) throws CestzamClientError {
        CestzamCookies cookies = myminfinContext.getCookies();
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("minminfin-rest", "customer", "mandatesCitizen");

        HttpResponse<String> userDataResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "mandatesCitizen");
        cestzamResponseService.assertSuccessStatusCode(userDataResponse);

        Type collectionType = new ArrayList<ApplicationMandate>() {
        }.getClass().getGenericSuperclass();
        List<ApplicationMandate> parsedResponse = cestzamResponseService.parseJson(userDataResponse, collectionType);

        return parsedResponse;
    }

    public List<ApplicationMandate> getEnterpriseMandates(CestzamAuthenticatedMyminfinContext myminfinContext) throws CestzamClientError {
        CestzamCookies cookies = myminfinContext.getCookies();
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("minminfin-rest", "customer", "mandatesPro");

        HttpResponse<String> userDataResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "mandatesPro");
        cestzamResponseService.assertSuccessStatusCode(userDataResponse);

        Type collectionType = new ArrayList<ApplicationMandate>() {
        }.getClass().getGenericSuperclass();
        List<ApplicationMandate> parsedResponse = cestzamResponseService.parseJson(userDataResponse, collectionType);

        return parsedResponse;
    }


    public CestzamAuthenticatedMyminfinContext activateMandate(CestzamAuthenticatedMyminfinContext myminfinContext, ApplicationMandate applicationMandate) throws CestzamClientError {
        CestzamCookies cookies = myminfinContext.getCookies();
        CzamCitizenInfo czamCitizenInfo = myminfinContext.getCzamCitizenInfo();
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("minminfin-rest", "customer", "selectedmandator");

        String payload = cestzamRequestService.formatJson(applicationMandate);
        HttpResponse<String> userDataResponse = cestzamRequestService.postJsonGetJson(debugTag, client, payload, REST_URL, "selectedmandator");
        cestzamResponseService.assertSuccessStatusCode(userDataResponse);

        CestzamCookies cestzamCookies = cestzamClientService.extractCookies(client);
        return new CestzamAuthenticatedMyminfinContext(cestzamCookies, czamCitizenInfo);
    }

    public CestzamAuthenticatedMyminfinContext deactivateMandate(CestzamAuthenticatedMyminfinContext myminfinContext) throws CestzamClientError {
        CestzamCookies cookies = myminfinContext.getCookies();
        CzamCitizenInfo czamCitizenInfo = myminfinContext.getCzamCitizenInfo();
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("minminfin-rest", "customer", "selectedmandator");

        String payload = cestzamRequestService.formatJson(new HashMap<>());
        HttpResponse<String> userDataResponse = cestzamRequestService.postJsonGetJson(debugTag, client, payload, REST_URL, "selectedcustomer");
        cestzamResponseService.assertSuccessStatusCode(userDataResponse);

        CestzamCookies cestzamCookies = cestzamClientService.extractCookies(client);
        return new CestzamAuthenticatedMyminfinContext(cestzamCookies, czamCitizenInfo);
    }


}
