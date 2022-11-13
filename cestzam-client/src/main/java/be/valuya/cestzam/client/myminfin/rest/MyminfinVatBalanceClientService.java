package be.valuya.cestzam.client.myminfin.rest;

import be.valuya.cestzam.client.CestzamClientService;
import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.debug.CestzamDebugService;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;
import be.valuya.cestzam.client.myminfin.rest.vatbalance.CurrentVatBalance;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.client.response.CestzamResponseService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

@ApplicationScoped
public class MyminfinVatBalanceClientService {

    private static final String REST_URL = "https://eservices.minfin.fgov.be/myminfin-rest/vatbalance";

    @Inject
    private CestzamClientService cestzamClientService;
    @Inject
    private CestzamDebugService cestzamDebugService;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @Inject
    private CestzamResponseService cestzamResponseService;

    public CurrentVatBalance getCurrentVatBalance(CestzamAuthenticatedMyminfinContext myminfinContext) throws CestzamClientError {
        CestzamCookies cookies = myminfinContext.getCookies();
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("minminfin-rest", "vatbalance", "current");

        HttpResponse<String> userDataResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "current");
        cestzamResponseService.assertSuccessStatusCode(userDataResponse);
        CurrentVatBalance parsedResponse = cestzamResponseService.parseJson(userDataResponse, CurrentVatBalance.class);
        return parsedResponse;
    }

}
