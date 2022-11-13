package be.valuya.cestzam.client.myminfin.rest;

import be.valuya.cestzam.client.CestzamClientService;
import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.debug.CestzamDebugService;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.client.response.CestzamResponseService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

@ApplicationScoped
public class MyminfinRestClientService {

    private static final String REST_URL = "https://eservices.minfin.fgov.be/myminfin-rest/myminfin/public";

    @Inject
    private CestzamClientService cestzamClientService;
    @Inject
    private CestzamDebugService cestzamDebugService;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @Inject
    private CestzamResponseService cestzamResponseService;

    public UserData getUserData(CestzamCookies cookies) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("minminfin-rest", "userData");

        HttpResponse<String> userDataResponse = cestzamRequestService.getJson(debugTag, client, REST_URL, "userData?currentHost=eservices.minfin.fgov.be");
        cestzamResponseService.assertSuccessStatusCode(userDataResponse);
        UserData userData = cestzamResponseService.parseJson(userDataResponse, UserData.class);

        return userData;
    }

}
