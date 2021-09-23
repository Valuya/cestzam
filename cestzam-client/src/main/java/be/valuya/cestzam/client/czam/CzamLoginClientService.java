package be.valuya.cestzam.client.czam;

import be.valuya.cestzam.client.CestzamClientService;
import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.debug.CestzamDebugService;
import be.valuya.cestzam.client.error.CzamSessionTimeoutError;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.client.response.CestzamResponseService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class CzamLoginClientService {
    public final static String CZAM_ORIGIN = "https://idp.iamfas.belgium.be";

    @Inject
    private CestzamClientService cestzamClientService;
    @Inject
    private CestzamDebugService cestzamDebugService;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @Inject
    private CestzamResponseService cestzamResponseService;

    public CestzamAuthenticatedSamlResponse doTokenLogin(CestzamLoginContext loginContext,
                                                         CzamCapacity czamCapacity,
                                                         String login, String password,
                                                         Map<Integer, String> tokenCodes) throws CestzamClientError {
        CestzamTokenVerificationResponse cestzamTokenVerificationResponse = startTokenLoginFlow(loginContext, login, password);
        CestzamCookies updatedCookies = cestzamTokenVerificationResponse.getCookies();
        int codeNumber = cestzamTokenVerificationResponse.getCodeNumber();
        String czamRequestId = cestzamTokenVerificationResponse.getCzamRequestId();

        String verificationCode = findCode(codeNumber, tokenCodes);
        return completeTokenLoginFlow(updatedCookies, czamCapacity, czamRequestId, verificationCode);
    }


    public CestzamTokenVerificationResponse startTokenLoginFlow(CestzamLoginContext loginContext, String login, String password) throws CestzamClientError {
        HttpClient client = cestzamClientService.createFollowRedirectClient(loginContext.getCestzamCookies());
        String debugTag = cestzamDebugService.createFlowDebugTag("czam", "startTokenLogin", login);
        CookieManager cookieManager = (CookieManager) client.cookieHandler()
                .orElseThrow(IllegalStateException::new);

        // A request seems to be made with this non-http-only  cookie, set from js:
        // 2021-05-18 Removed. The cookie is now set correctly in the previous response header
//        createIAALangCookie(cookieManager);

        // Click on 'login with security code via token'
        HttpResponse<String> tokenLoginFormResponse = cestzamRequestService.getHtml(debugTag, client, CZAM_ORIGIN, "fasui/login/citizentokenservice");
        checkNoError(tokenLoginFormResponse);

        // Submit of login+password form
        String tokenLoginForm = tokenLoginFormResponse.body();
        String requestId = cestzamResponseService.searchAttributeInDom(tokenLoginForm, "input[name=requestId]", "value")
                .orElseThrow(() -> new CestzamClientError("Expected a input[name=requestId] in the dom, but none found"));
        String submissionToken = this.createSubmissionId();
        Map<String, String> formParam = Map.of(
                "IDToken1", login,
                "IDToken2", password,
                "requestId", requestId,
                "submissionToken", submissionToken);
        HttpResponse<String> loginResponse2 = cestzamRequestService.postFormUrlEncodedAcceptHtml(debugTag, client, formParam, CZAM_ORIGIN, "fasui/login/citizentokenservice");
        checkNoError(loginResponse2);

        HttpResponse<String> loginLabelsResponse = cestzamRequestService.getJson(debugTag, client, CZAM_ORIGIN, "fasui/labels?language=fr");
        checkNoError(loginLabelsResponse);
        String jsonBody = loginLabelsResponse.body();
        JsonObject labelsJson = Json.createReader(new StringReader(jsonBody))
                .readObject();
        JsonObject tokenLabelsJson = labelsJson.getJsonObject("token");
        String tokenLabelString = tokenLabelsJson.getString("authMean_token_step2");
        int tokenInt = parseTokenInt(tokenLabelString);

        CestzamCookies updatedCookies = cestzamClientService.extractCookies(client);
        return new CestzamTokenVerificationResponse(updatedCookies, requestId, tokenLabelString, tokenInt);
    }


    public CestzamAuthenticatedSamlResponse completeTokenLoginFlow(CestzamCookies cookies, CzamCapacity czamCapacity, String requestId, String validationCode) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("czam", "completeTokenLogin");

        // POST validation code
        HttpResponse<String> loginResponse;
        {
            // IDToken1=FAQICE&requestId=s24bbe149010748eb25c89a3ce78032d86b0c5477d&submissionToken=268ftqix7ezd2gmbu6m80
            String submissionToken = this.createSubmissionId();
            Map<String, String> formParam = Map.of(
                    "IDToken1", validationCode,
                    "requestId", requestId,
                    "submissionToken", submissionToken);
            loginResponse = cestzamRequestService.followRedirects(debugTag, client,
                    cestzamRequestService.postFormUrlEncodedAcceptHtml(debugTag, client, formParam, CZAM_ORIGIN, "fasui/login/citizentokenservice")
            );
            checkNoError(loginResponse);
        }

        return completeFlowBackToCzam(czamCapacity, client, debugTag, loginResponse);
    }

    private CestzamAuthenticatedSamlResponse completeFlowBackToCzam(CzamCapacity czamCapacity, HttpClient client, String debugTag, HttpResponse<String> loginResponse) throws CestzamClientError {
        URI responseUri = loginResponse.uri();
        boolean setCapacityPage = responseUri.getPath().equalsIgnoreCase("/fasui/setCapacity");
        boolean setCapacityForExternalPartnerPage = responseUri.getPath().equalsIgnoreCase("/fasui/setCapacityForExternalPartner");

        if (setCapacityPage) {
            // citizen or entreprise login
            String submissionToken = this.createSubmissionId();
            Map<String, String> formParam = Map.of(
                    "capacity", czamCapacity.getName(),
                    "submissionToken", submissionToken);
            loginResponse = cestzamRequestService.postFormUrlEncodedAcceptHtml(debugTag, client, formParam, CZAM_ORIGIN, "fasui/setCapacity");
            checkNoError(loginResponse);
        } else if (setCapacityForExternalPartnerPage) {
            /**
             *  <form class="form-horizontal" method="post" action="setCapacityForExternalPartner">
             *
             *             <div class="col-md-8">
             *     <label class="radio-inline">
             *         <input type="radio" name="capacity" id="citizen" value="citizen" />
             *         <text lang="default" translate="auth_capacity_citizen">in your own name</text>
             *     </label>
             * </div>
             *
             *
             *             <div class="col-md-8">
             *     <label class="radio-inline">
             *         <input type="radio" name="capacity" id="enterprise" value="enterprise" />
             *         <text lang="default" translate="auth_capacity_enterprise">in the name of a company</text>
             *     </label>
             * </div>
             *
             *
             *         <button type="submit" lang="default" class="btn btn-default btn-lg purple" translate="auth_button_continue">Next</button>
             *     </form>
             */
            Map<String, String> formParam = Map.of(
                    "capacity", czamCapacity.getName()
            );
            loginResponse = cestzamRequestService.postFormUrlEncodedAcceptHtml(debugTag, client, formParam, CZAM_ORIGIN, "fasui/setCapacityForExternalPartner");
            checkNoError(loginResponse);
        }

        HttpResponse<String> samlResponse = cestzamRequestService.followRedirectsOnOrigin(debugTag, client, loginResponse);
        checkNoError(samlResponse);
        String samlResponseBody = samlResponse.body();
        Optional<String> serviceRedirectLocation = samlResponse.headers().firstValue("location");


        String samlResponseToken = cestzamResponseService.searchAttributeInDom(samlResponseBody, "input[name=SAMLResponse]", "value")
                .orElseThrow((() -> new CestzamClientError("Expected SAMLResponse element in dom, none found")));

        String relayStateToken = cestzamResponseService.searchAttributeInDom(samlResponseBody, "input[name=RelayState]", "value")
                .orElseThrow((() -> new CestzamClientError("Expected RelayState element in dom, none found")));

        CestzamCookies updatedCookies = cestzamClientService.extractCookies(client);
        return new CestzamAuthenticatedSamlResponse(updatedCookies, samlResponseToken, relayStateToken, serviceRedirectLocation);
    }


    private void checkNoError(HttpResponse<String> response) throws CestzamClientError {
        if (response.statusCode() >= 400) {
            throw new CestzamClientError("Error response: " + response);
        }
        String responseBody = response.body();
        boolean hasTimeoutMessage = cestzamResponseService.searchAttributeInDom(responseBody, "[translate=fas_Session has timed out]", "translate")
                .isPresent();
        if (hasTimeoutMessage) {
            throw new CzamSessionTimeoutError();
        }
        if (responseBody.contains("Authentication Failed")) {
            throw new CestzamClientError("Authentication failed ");
        }
    }

    private int parseTokenInt(String tokenNumber) {
        String digitString = tokenNumber.replaceAll("[^0-9]", "");
        return Integer.parseInt(digitString);
    }

    private String createSubmissionId() {
        /**
         * In czam page code:
         * $('<input>').attr({
         *                 type: 'hidden',
         *                 name: 'submissionToken',
         *                 value: (Math.random()*1e32).toString(36)
         *             }).appendTo(form);
         *             produces 6fh1ighon1c0000000000
         */

        String intString = BigDecimal.valueOf(Math.random() * Math.pow(10, 32))
                .toBigInteger()
                .toString(36);
        while (intString.length() < 21) {
            intString = intString + "0";
        }
        return intString;
    }


    private String findCode(int codeNumber, Map<Integer, String> tokenCodes) throws CestzamClientError {
        return Optional.ofNullable(tokenCodes.get(codeNumber))
                .orElseThrow(() -> new CestzamClientError("Could not find token code number " + codeNumber));
    }


    @Deprecated // This workaround does not seem to be used anymore (2021-05-18)
    private void createIAALangCookie(CookieManager cookieManager) {
        // A request seems to be made with this non-http-only  cookie, set from js:
        /*
         * this can't be in the *.js file because
         * it is specific for each page
         * and we need to access the currentLanguage response attribute using Thymeleaf
         *
         *  var lang = new Lang();
        var ROOT_CONTEXT = "/fasui/";
        lang.init({
                defaultLang: 'default',
                currentLang: 'FR',
                cookie: {
            name: 'IAA-lang',
                    expiry: 3650,
                    domain: '.iamfas.belgium.be'
        },
        allowCookieOverride: false
        });
         */
        HttpCookie cookie = new HttpCookie("IAA-lang", "FR");
        cookie.setDomain(".iamfas.belgium.be");
        cookie.setPath("/");
        cookie.setMaxAge(3650);
        CookieStore cookieStore = cookieManager.getCookieStore();
        cookieStore.add(null, cookie);
    }
}
