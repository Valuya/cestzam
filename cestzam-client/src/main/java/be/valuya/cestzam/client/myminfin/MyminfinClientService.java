package be.valuya.cestzam.client.myminfin;

import be.valuya.cestzam.client.CestzamClientService;
import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.czam.CzamCitizenInfo;
import be.valuya.cestzam.client.czam.CestzamAuthenticatedSamlResponse;
import be.valuya.cestzam.client.czam.CestzamLoginContext;
import be.valuya.cestzam.client.debug.CestzamDebugService;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.rest.MyminfinRestClientService;
import be.valuya.cestzam.client.myminfin.rest.UserData;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.client.response.CestzamResponseService;
import org.jsoup.Jsoup;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class MyminfinClientService {

    private static final String MYMINFIN_HOST = "eservices.minfin.fgov.be";
    private static final String MYMINFIN_ORIGIN = "https://" + MYMINFIN_HOST;
    private static final String FEDIAM_MYMINFIN_ORIGIN = "https://fediam.minfin.fgov.be";

    @Inject
    private CestzamClientService cestzamClientService;
    @Inject
    private CestzamDebugService cestzamDebugService;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @Inject
    private CestzamResponseService cestzamResponseService;
    @Inject
    private MyminfinRestClientService restClientService;

    public CestzamLoginContext startLoginFlow() throws CestzamClientError {
        CestzamCookies cookies = new CestzamCookies();
        HttpClient client = cestzamClientService.createFollowRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("myminfin", "startLogin");

        // Click on language choice
        JsonObject languagePayload = Json.createObjectBuilder()
                .add("language", "fr")
                .build();
        // 2022-01: uri changed with /public path name & accept: json
        HttpResponse<String> languageResponse = cestzamRequestService.followRedirects(debugTag, client,
                cestzamRequestService.postJsonGetJson(debugTag, client, languagePayload.toString(), MYMINFIN_ORIGIN, "myminfin-rest/portal/public/language")
        );
        cestzamResponseService.assertSuccessStatusCode(languageResponse);

        // Load of main page
        // 2022-01: pages subpath appended. Racaptcha resources loaded (warning)
        HttpResponse<String> homepageResponse = cestzamRequestService.followRedirects(debugTag, client,
                cestzamRequestService.getHtml(debugTag, client, MYMINFIN_ORIGIN, "myminfin-web/pages")
        );
        cestzamResponseService.assertSuccessStatusCode(homepageResponse);

        // Click on login
        HttpResponse<String> czamSaml2Response = cestzamRequestService.followRedirects(debugTag, client,
                cestzamRequestService.getHtml(debugTag, client, MYMINFIN_ORIGIN, "myminfin-web/pages/private/login")
        );
        cestzamResponseService.assertSuccessStatusCode(czamSaml2Response);

        URI loginUri;
        String saml2RequestToken;
        String secondVisitUrl;
        if (czamSaml2Response.statusCode() == 200) {
            // 2022-01: We get another response with another form and a js script.
            // The script attempts to store hidden input values in session storage then set the location to
            // the value of another input field.

            saml2RequestToken = cestzamResponseService.searchAttributeInDom(czamSaml2Response.body(), "input#saml2Request", "value")
                    .orElseThrow((() -> new CestzamClientError("Expected saml2Request element in dom, none found")));
            secondVisitUrl = cestzamResponseService.searchAttributeInDom(czamSaml2Response.body(), "input#secondVisitUrl", "value")
                    .map(s -> Jsoup.parse(s).body().text()) // escaped html charactes hex-codes
                    .orElseThrow((() -> new CestzamClientError("Expected secondVisitUrl element in dom, none found")));

            String loginUrl = cestzamResponseService.searchAttributeInDom(czamSaml2Response.body(), "input#loginUrl", "value")
                    .map(s -> Jsoup.parse(s).body().text()) // escaped html charactes hex-codes
                    .orElseThrow((() -> new CestzamClientError("Expected loginUrl element in dom, none found")));

            loginUri = URI.create(loginUrl);
        } else {
            throw new CestzamClientError("Unexpected response status: " + czamSaml2Response.statusCode());
        }

        CestzamCookies updatedCookies = cestzamClientService.extractCookies(client);
        CestzamLoginContext cestzamLoginContext = new CestzamLoginContext(loginUri, saml2RequestToken, secondVisitUrl, updatedCookies);
        return cestzamLoginContext;
    }

    public CestzamAuthenticatedMyminfinContext completeLoginFlow(CestzamAuthenticatedSamlResponse authenticatedSamlResponse) throws CestzamClientError {
        return completeLoginFlow(Optional.empty(), authenticatedSamlResponse);
    }

    public CestzamAuthenticatedMyminfinContext completeLoginFlow(String requestedVatNumber, CestzamAuthenticatedSamlResponse authenticatedSamlResponse) throws CestzamClientError {
        return completeLoginFlow(Optional.of(requestedVatNumber), authenticatedSamlResponse);
    }

    private CestzamAuthenticatedMyminfinContext completeLoginFlow(Optional<String> requestedVatNumber, CestzamAuthenticatedSamlResponse authenticatedSamlResponse) throws CestzamClientError {
        CestzamCookies cookies = authenticatedSamlResponse.getCookies();
        String relayState = authenticatedSamlResponse.getRelayState();
        String samlResponse = authenticatedSamlResponse.getSamlResponse();


        HttpClient client = cestzamClientService.createFollowRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("myminfin", "completeLogin");

        Map<String, String> fediamFormParm = Map.of(
                "RelayState", relayState,
                "SAMLResponse", samlResponse
        );
        // Should be that sso/url in bmost case
        URI completeURI = authenticatedSamlResponse.getServiceRedirectUrl()
                .map(URI::create)
                .orElseGet(() -> URI.create(FEDIAM_MYMINFIN_ORIGIN + "/sso/Consumer/metaAlias/external/sp"));

        HttpResponse<String> loginResponse = cestzamRequestService.postFormUrlEncodedAcceptHtml(debugTag, client, fediamFormParm, completeURI.toASCIIString());
        cestzamResponseService.assertSuccessStatusCode(loginResponse);
        URI responseUri = loginResponse.uri();

        if (isCCCFLocation(responseUri)) {
            return handleCCFF(client, loginResponse, requestedVatNumber);
        }

        // There is a post to /static/dynaTraceMonitor with payload:
        // $a=1%7C1%7C_load_%7C_load_%7C-%7C1591814865225%7C1591814867503%7C33%7C-%7C-%7C-%7C-%7C-%7Chttps%3A%2F%2Fidp.iamfas.belgium.be%2Ffas%2FSSORedirect%2FmetaAlias%2Fidp%3FReqID%3Ds24bbe149010748eb25c89a3ce78032d86b0c5477d%26index%3Dnull%26acsURL%3Dhttps%3A%2F%2Ffediam.minfin.fgov.be%2Fsso%2FConsumer%2FmetaAlias%2Fexternal%2Fsp%26spEntityID%3Dfediam.minfin.fgov.be%26binding%3Durn%3Aoasis%3Anames%3Atc%3ASAML%3A2.0%3Abindings%3AHTTP-POST%2C2%7C2%7C_onload_%7C_load_%7C-%7C1591814867496%7C1591814867503%7C33$v=63$fId=14867278_965$rId=RID_-741331357$rpId=502870212$domR=1591814867496$w=1920$h=941$nt=a0b1591814865225e1782f1782g1782h1782i1782j0k1785l1809m1810n1837o2264p2266q2270r2271s2271t2278$url=https%3A%2F%2Feservices.minfin.fgov.be%2Fmyminfin-web%2Fpages%2Fprivate%2Flogin$title=$time=1591814867550

        CestzamCookies cestzamCookies = cestzamClientService.extractCookies(client);
        UserData userData = restClientService.getUserData(cestzamCookies);

        String nationalNumber = userData.getNationalNumber();
        String firstname = userData.getFirstname();
        String lastname = userData.getLastname();
        CzamCitizenInfo czamCitizenInfo = new CzamCitizenInfo(firstname, lastname, nationalNumber);

        return new CestzamAuthenticatedMyminfinContext(cestzamCookies, czamCitizenInfo);
    }

    private CestzamAuthenticatedMyminfinContext handleCCFF(HttpClient client, HttpResponse<String> loginResponse, Optional<String> requestedVatNumberOptional) throws CestzamClientError {
        String dom = loginResponse.body();
        List<String> vatNumbers = cestzamResponseService.listAttributesInDom(dom, "select[name=delegation] > option", "value")
                .stream()
                .map(optionValue -> optionValue.replaceAll("^KBO-", ""))
                .collect(Collectors.toList());
        String formAction = cestzamResponseService.searchAttributeInDom(dom, "form[name=enterpriseSelectionForm]", "action")
                .orElseThrow(() -> new CestzamClientError("No form action in the CCFF page"));
        if (vatNumbers.isEmpty()) {
            throw new CestzamClientError("Unexpected CCFF form content: no vat number select options");
        }
        String vatNumber = requestedVatNumberOptional
                .orElseGet(() -> vatNumbers.get(0));
        if (!vatNumbers.contains(vatNumber)) {
            throw new CestzamClientError("Requested vat number " + vatNumber + " not present in the select options");
        }

        String debugTag = cestzamDebugService.createFlowDebugTag("myminfin", "completeLogin", "CCFF", vatNumber);
        Map<String, String> formParam = Map.of("delegation", "KBO-" + vatNumber);
        HttpResponse<String> ccffResponse = cestzamRequestService.postFormUrlEncodedAcceptHtml(debugTag, client, formParam, MYMINFIN_ORIGIN, formAction);
        cestzamResponseService.assertSuccessStatusCode(ccffResponse);

        CestzamCookies cestzamCookies = cestzamClientService.extractCookies(client);
        return new CestzamAuthenticatedMyminfinContext(cestzamCookies, null);//FIXME
    }

    private boolean isCCCFLocation(URI responseUri) {
        String host = responseUri.getHost();
        String path = responseUri.getPath();
        boolean ccffPath = path.startsWith("/CCFF_Authentication");
        return host.equalsIgnoreCase(MYMINFIN_HOST) && ccffPath;
    }


}
