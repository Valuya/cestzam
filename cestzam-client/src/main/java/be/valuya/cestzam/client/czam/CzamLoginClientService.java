package be.valuya.cestzam.client.czam;

import be.valuya.cestzam.client.CestzamClientService;
import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.debug.CestzamDebugService;
import be.valuya.cestzam.client.error.CzamSessionTimeoutError;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.client.response.CestzamResponseService;
import org.jsoup.Jsoup;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.StringReader;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@ApplicationScoped
public class CzamLoginClientService {

    public final static String CZAM_ORIGIN = "https://idp.iamfas.belgium.be";
    private final static String ITSME_ORIGIN = "https://merchant.itsme.be";

    // Supported major api versions for /fasui/api. Used in pattern, so dots will match all chars
    private final List<String> CZAM_API_VERSION_SUPPORTED_VERSIONS_PREFIXES = List.of(CZAM_API_V19, CZAM_API_V22);
    public static final String CZAM_API_V22 = "22";
    public static final String CZAM_API_V19 = "19";

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
        String codeLabel = cestzamTokenVerificationResponse.getCodeLabel();
        String czamRequestId = cestzamTokenVerificationResponse.getCzamRequestId();
        String czamApiVersion = cestzamTokenVerificationResponse.getCzamApiVersion();
        boolean czamApiVersionSupported = cestzamTokenVerificationResponse.isCzamApiVersionSupported();
        String authId = cestzamTokenVerificationResponse.getAuthId();

        String verificationCode = findCode(codeNumber, tokenCodes);

        return completeTokenLoginFlow(updatedCookies, czamApiVersion, czamApiVersionSupported,
                czamCapacity, czamRequestId, codeLabel, authId, verificationCode,
                loginContext.getSaml2RequestToken(), loginContext.getSecondVisitUrl());
    }


    public CestzamTokenVerificationResponse startTokenLoginFlow(CestzamLoginContext loginContext,
                                                                String login, String password) throws CestzamClientError {
        HttpClient client = cestzamClientService.createFollowRedirectClient(loginContext.getCestzamCookies());
        String debugTag = cestzamDebugService.createFlowDebugTag("czam", "startTokenLogin", login);
        CookieManager cookieManager = (CookieManager) client.cookieHandler()
                .orElseThrow(IllegalStateException::new);

        String requestId = initCzamSinglePageApp(loginContext, debugTag, client, cookieManager);
        String apiVersion = fetchCzamApiVersion(client, debugTag);
        boolean apiVersionSupported = isSupportedApiVersion(apiVersion);

        // Post username-password
        String loginpasswordPayload = Json.createObjectBuilder(Map.of(
                "username", login,
                "password", password,
                "token", "",
                "tokenRequested", ""
        )).build().toString();
        // 2023-01: api v 22.2.0-RC69: no more request id in path
        HttpResponse<String> usernamePasswordResponse = cestzamRequestService.postJsonGetJson(debugTag, client, loginpasswordPayload, CZAM_ORIGIN, "/fasui/api/login/citizentokenservice/username-password");
        checkNoError(usernamePasswordResponse);

        String tokenRequestedValue;
        String authIdToken;
        JsonReader usernamePasswordResponseJson = Json.createReader(new StringReader(usernamePasswordResponse.body()));
        JsonObject usernamePasswordObject = usernamePasswordResponseJson.readObject();
        try {
            JsonString tokenRequested = (JsonString) usernamePasswordObject.get("tokenRequested");
            tokenRequestedValue = tokenRequested.getString();
        } catch (Exception e) {
            throw new CestzamClientError("Expected a requested token, but got error " + e.getMessage());
        }
        try {
            JsonString authIdValue = (JsonString) usernamePasswordObject.get("authId");
            authIdToken = authIdValue.getString();
        } catch (Exception e) {
            throw new CestzamClientError("Expected a authId token, but got error " + e.getMessage());
        }
        int tokenInt = parseTokenInt(tokenRequestedValue);
        CestzamCookies updatedCookies = cestzamClientService.extractCookies(client);
        return new CestzamTokenVerificationResponse(updatedCookies, apiVersion, apiVersionSupported,
                requestId, tokenRequestedValue, tokenInt, authIdToken,
                loginContext.getSaml2RequestToken(), loginContext.getSecondVisitUrl());
    }

    public CestzamAuthenticatedSamlResponse completeTokenLoginFlow(CestzamCookies cookies,
                                                                   String czamApiVersion, boolean czamApiVersionSupported, CzamCapacity czamCapacity, String requestId,
                                                                   String requestedToken, String authId, String requestedTokenValue,
                                                                   String saml2RequestToken, String secondVisitUrl) throws CestzamClientError {
        HttpClient client = cestzamClientService.createNoRedirectClient(cookies);
        String debugTag = cestzamDebugService.createFlowDebugTag("czam", "completeTokenLogin");
        String tokenIdValue;
        String gotoUrl;
        List<String> postAuthenticationSteps;

        {
            String requestedTokenValuePayload = Json.createObjectBuilder(Map.of(
                    "token", requestedTokenValue,
                    "tokenRequested", requestedToken
            )).build().toString();

            // 2023-01, api v 22.2.0-RC69: no more requestId in path
            HttpResponse<String> usernamePasswordResponse = cestzamRequestService.postJsonGetJson(debugTag, client,
                    Map.of("auth-id", authId),
                    requestedTokenValuePayload, CZAM_ORIGIN, "/fasui/api/login/citizentokenservice/token");
            checkNoError(usernamePasswordResponse);
            //  {"authId":null,"tokenId":"GCm10xCIEXlz_ukpfH1MAslhZt0.*AAJTSQACMDIAAlNLABxNN201ZzAxMU91bnBYbFV3T2dWSXJBSVVTc3c9AAR0eXBlAANDVFMAAlMxAAIwMQ..*","postAuthenticationSteps":[],"gotoUrl":null}
            JsonReader usernameResponseReader = Json.createReader(new StringReader(usernamePasswordResponse.body()));
            JsonObject usernameResponseObject = usernameResponseReader.readObject();

            try {
                JsonString tokenIdJsonString = (JsonString) usernameResponseObject.get("tokenId");
                tokenIdValue = tokenIdJsonString.getString();
            } catch (Exception e) {
                throw new CestzamClientError("Expected  tokenId in json response");
            }
            try {
                JsonValue gotoUrlJsonValue = usernameResponseObject.get("gotoUrl");
                gotoUrl = gotoUrlJsonValue.getValueType() == JsonValue.ValueType.NULL ? null : ((JsonString) gotoUrlJsonValue).getString();
            } catch (Exception e) {
                throw new CestzamClientError("Expected  gotoUrl in json response");
            }
            try {
                JsonArray postAuthenticationStepsValue = (JsonArray) usernameResponseObject.get("postAuthenticationSteps");
                postAuthenticationSteps = postAuthenticationStepsValue.stream()
                        .map(v -> (JsonString) v)
                        .map(JsonString::getString)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new CestzamClientError("Expected  gotoUrl in json response");
            }
        }

        return completeCzamPostAuthenticationFlow(client, czamApiVersion, czamApiVersionSupported,
                requestId, tokenIdValue, "citizentokenservice",
                gotoUrl, postAuthenticationSteps,
                czamCapacity, saml2RequestToken, secondVisitUrl);
    }

    private CestzamAuthenticatedSamlResponse completeCzamPostAuthenticationFlow(HttpClient client, String apiVersion, boolean apiVersionSupported,
                                                                                String requestId, String tokenId,
                                                                                String service,
                                                                                String gotoUrl, List<String> postSteps, CzamCapacity czamCapacity,
                                                                                String saml2RequestToken, String secondVisitUrl) throws CestzamClientError {
        String debugTag = cestzamDebugService.createFlowDebugTag("czam", "postAuthentication");

        if (postSteps.contains("capacity")) {
            setCzamCapacity(client, debugTag, requestId, tokenId, service, czamCapacity);
        }

        {
            // 2023-01: 22.2.0-RC69: no more requestId in path (replaced with 'complete' here).
            URI postAuthUri = cestzamRequestService.createUri(CZAM_ORIGIN, "/fasui/api/post-authentication/complete");
            HttpResponse<String> postAuthResponse = cestzamRequestService.getJson(debugTag, client,
                    Map.of("token-id", tokenId), postAuthUri
            );
            checkNoError(postAuthResponse);
            // {"gotoUrl":"https://idp.iamfas.belgium.be:443/fas/saml2/continue/metaAlias/idp?secondVisitUrl=/fas/SSOPOST/metaAlias/idp?ReqID=w5bpl8gsoh7e0onkc54hurcj056"}
            JsonReader postAuthResponseReader = Json.createReader(new StringReader(postAuthResponse.body()));
            JsonObject postAuthResponseObject = postAuthResponseReader.readObject();
            if (gotoUrl == null) {
                try {
                    JsonValue gotoUrlJsonValue = postAuthResponseObject.get("gotoUrl");
                    gotoUrl = ((JsonString) gotoUrlJsonValue).getString();
                } catch (Exception e) {
                    throw new CestzamClientError("Expected  gotoUrl in json response");
                }
            }
        }

        if (gotoUrl == null) {
            throw new CestzamClientError("Expected  gotoUrl in json response");
        }
        {
            URI gotoUri = URI.create(gotoUrl);
            HttpResponse<String> gotoResponse = cestzamRequestService.getHtml(debugTag, client, gotoUri);
            checkNoError(gotoResponse);

            // Response is a form and an injected script that submits the samle2Request stored in sessionStorage to
            // the secondVisitUrl
            URI secondVisitUri = cestzamRequestService.createUri(CZAM_ORIGIN, secondVisitUrl);
            Map<String, String> formData = Map.of(
                    "saml2Request", saml2RequestToken
            );
            HttpResponse<String> saml2Response = cestzamRequestService.postFormUrlEncodedAcceptHtml(debugTag, client, formData, secondVisitUri);
            checkNoError(saml2Response);

            // Response is a form with the new saml token + relay state
            String saml2ResponseBody = saml2Response.body();
            String samlResponseToken = cestzamResponseService.searchAttributeInDom(saml2ResponseBody, "input[name=SAMLResponse]", "value")
                    .orElseThrow((() -> new CestzamClientError("Expected SAMLResponse element in dom, none found")));
            String relayStateToken = cestzamResponseService.searchAttributeInDom(saml2ResponseBody, "input[name=RelayState]", "value")
                    .orElseThrow((() -> new CestzamClientError("Expected RelayState element in dom, none found")));
            String formActionUrlString = cestzamResponseService.searchAttributeInDom(saml2ResponseBody, "form", "action")
                    .map(s -> Jsoup.parse(s).body().text()) // html-escaped characters
                    .orElseThrow((() -> new CestzamClientError("Expected form element in dom, none found")));

            CestzamCookies updatedCookies = cestzamClientService.extractCookies(client);
            return new CestzamAuthenticatedSamlResponse(updatedCookies, apiVersion, apiVersionSupported, samlResponseToken, relayStateToken, Optional.of(formActionUrlString));
        }
    }

    private CestzamAuthenticatedSamlResponse completeCzamExternalPartnerFlow(HttpClient client, CzamCapacity czamCapacity,
                                                                             CestzamLoginContext loginContext,
                                                                             HttpResponse<String> loginResponse) throws CestzamClientError {
        String debugTag = cestzamDebugService.createFlowDebugTag("czam", "postAuthenticationExternal");
        String apiVersoin = fetchCzamApiVersion(client, debugTag);
        boolean apiVersionSupported = isSupportedApiVersion(apiVersoin);

        String secondVisitUrl = loginContext.getSecondVisitUrl();
        String saml2RequestToken = loginContext.getSaml2RequestToken();
        // Response is a form and an injected script that submits the samle2Request stored in sessionStorage to
        // the secondVisitUrl
        URI secondVisitUri = cestzamRequestService.createUri(CZAM_ORIGIN, secondVisitUrl);
        Map<String, String> formData = Map.of(
                "saml2Request", saml2RequestToken
        );
        HttpResponse<String> saml2Response = cestzamRequestService.postFormUrlEncodedAcceptHtml(debugTag, client, formData, secondVisitUri);
        HttpResponse<String> redirectedSaml2Response = cestzamRequestService.followRedirectsOnOrigin(debugTag, client, saml2Response);
        checkNoError(redirectedSaml2Response);


        URI responseUri = redirectedSaml2Response.uri();
        boolean setCapacityForExternalPartnerPage = responseUri.getPath().equalsIgnoreCase("/fasui/setCapacityForExternalPartner");
        HttpResponse<String> samlFormResponse;

        if (setCapacityForExternalPartnerPage) {
            String requestId = cestzamResponseService.parseURIParamsGroup1(responseUri, ".*requestId=([^&]*).*")
                    .orElseThrow(() -> new CestzamClientError("Expected requestId query param"));
            String tokenId = cestzamResponseService.parseURIParamsGroup1(responseUri, ".*tokenId=([^&]*).*")
                    .orElseThrow(() -> new CestzamClientError("Expected tokenId query param"));
            String authnService = cestzamResponseService.parseURIParamsGroup1(responseUri, ".*authnService=([^&]*).*")
                    .orElseThrow(() -> new CestzamClientError("Expected authnService query param"));
            String gotoValue = cestzamResponseService.parseURIParamsGroup1(responseUri, ".*goto=([^&]*).*")
                    .orElseThrow(() -> new CestzamClientError("Expected goto query param"));

            setCzamCapacity(client, debugTag, requestId, tokenId, authnService, czamCapacity);

            samlFormResponse = cestzamRequestService.getHtml(debugTag, client, URI.create(gotoValue));
        } else {
            throw new CestzamClientError("Expected to be forwarded to setCapacityForExternalPartner");
        }
        checkNoError(samlFormResponse);
        String samlResponseBody = samlFormResponse.body();

        String samlResponseToken = cestzamResponseService.searchAttributeInDom(samlResponseBody, "input[name=SAMLResponse]", "value")
                .orElseThrow((() -> new CestzamClientError("Expected SAMLResponse element in dom, none found")));
        String relayStateToken = cestzamResponseService.searchAttributeInDom(samlResponseBody, "input[name=RelayState]", "value")
                .orElseThrow((() -> new CestzamClientError("Expected RelayState element in dom, none found")));
        String formActionUrlString = cestzamResponseService.searchAttributeInDom(samlResponseBody, "form", "action")
                .map(s -> Jsoup.parse(s).body().text()) // html-escaped characters
                .orElseThrow((() -> new CestzamClientError("Expected form element in dom, none found")));

        CestzamCookies updatedCookies = cestzamClientService.extractCookies(client);
        return new CestzamAuthenticatedSamlResponse(updatedCookies, samlResponseToken, apiVersionSupported, relayStateToken, relayStateToken, Optional.of(formActionUrlString));
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

    private String findCode(int codeNumber, Map<Integer, String> tokenCodes) throws CestzamClientError {
        return Optional.ofNullable(tokenCodes.get(codeNumber))
                .orElseThrow(() -> new CestzamClientError("Could not find token code number " + codeNumber));
    }

    private String getApiVersion(HttpResponse<String> apiVersionResponse) {
        String apiVersionJson = apiVersionResponse.body();
        JsonReader jsonReader = Json.createReader(new StringReader(apiVersionJson));
        String apiVersion = ((JsonString) jsonReader.readValue()).getString();
        return apiVersion;
    }

    private boolean isSupportedApiVersion(String fasuiApiVersionString) {
        return CZAM_API_VERSION_SUPPORTED_VERSIONS_PREFIXES.stream()
                .anyMatch(versionPrefix -> {
                    Matcher matcher = java.util.regex.Pattern.compile("^" + versionPrefix + "\\..*$").matcher(fasuiApiVersionString);
                    return matcher.matches();
                });
    }

    private String fetchCzamApiVersion(HttpClient client, String debugTag) throws CestzamClientError {
        // Fetch the api version
        HttpResponse<String> apiVersionResponse = cestzamRequestService.getJson(debugTag, client, CZAM_ORIGIN, "/fasui/api/version");
        checkNoError(apiVersionResponse);
        String apiVersion = getApiVersion(apiVersionResponse);
        return apiVersion;
    }

    private String initCzamSinglePageApp(CestzamLoginContext loginContext, String debugTag, HttpClient client, CookieManager cookieManager) throws CestzamClientError {
        // 2022-01: Single-page app now.
        URI loginUri = loginContext.getLoginUri();
        String spEntityID = cestzamResponseService.parseURIParamsGroup1(loginUri, ".*spEntityID=([^&]*).*")
                .orElseThrow((() -> new CestzamClientError("Expected a spEntityID query param")));
        String service = cestzamResponseService.parseURIParamsGroup1(loginUri, ".*service=([^&]*).*")
                .orElseThrow((() -> new CestzamClientError("Expected a service query param")));
        String gotoValue = parseGotoValueFromLoginUro(loginUri);

        URI gotoValueUri = URI.create(gotoValue);
        String secondVisitUrlString = cestzamResponseService.parseURIParamsGroup1(gotoValueUri, ".*secondVisitUrl=([^&]*).*")
                .orElseThrow((() -> new CestzamClientError("Expected a secondVisitUrl query param")));
        String requestId = parseRequestIdFromSecondVisitUrl(secondVisitUrlString);

        // Just navigate to the app
        HttpResponse<String> loginResponse = cestzamRequestService.getHtml(debugTag, client, loginUri);

        // Set cookies. This is done in application code apparently
        URI cookieURI = URI.create(CZAM_ORIGIN);
        try {
            cookieManager.put(cookieURI, Map.of(
                    "Set-Cookie", List.of(
                            "i18next=fr; path=/fasui;Domain=.iamfas.belgium.be;Secure",
                            "auth-protocol=SAML; path=/fasui;Domain=.iamfas.belgium.be;Secure",
                            "request-id=" + requestId + "; path=/fasui;Domain=.iamfas.belgium.be;Secure",
                            "goto-url=" + gotoValueUri.toASCIIString() + "; path=/fasui;Domain=.iamfas.belgium.be;Secure"
                    )
            ));
        } catch (IOException e) {
            throw new CestzamClientError("Unable to set fasui cookies: " + e.getMessage());
        }

        // This post is probably not required, but may be useful as it returns the available auth methods
        // TODO: specific method & checks whether services is available
        // bmid = itsme
        // citizentokenservice = token codes
        // 2023-01: api Updated, no more requestId in path. api version 22.2.0-RC69
        HttpResponse<String> requestIdResponse = cestzamRequestService.getJson(debugTag, client, CZAM_ORIGIN, "/fasui/api/authInfo");
        checkNoError(requestIdResponse);
        //  {"authMeanMap":{"eid_group":["eidservice","bmid"],"twofactor_group":["mailotpservice","totpservice","otpservice"],"token_group":["citizentokenservice"],"european_group":["eidasservice"]},"keyCount":7,"serviceMessage":null,"language":null,"customLogos":{"de":" ... }}

        return requestId;
    }

    private String parseGotoValueFromLoginUro(URI loginUri) throws CestzamClientError {
        String gotoValue = cestzamResponseService.parseURIParamsGroup1(loginUri, ".*goto=([^&]*).*")
                .orElseThrow((() -> new CestzamClientError("Expected a goto query param")));
        return gotoValue;
    }

    private String parseRequestIdFromSecondVisitUrl(String secondVisitUrlString) throws CestzamClientError {
        URI secondVisitUri = URI.create(secondVisitUrlString);
        String requestId = cestzamResponseService.parseURIParamsGroup1(secondVisitUri, ".*ReqID=([^&]*).*")
                .orElseThrow((() -> new CestzamClientError("Expected a ReqID query param")));
        return requestId;
    }


    private void setCzamCapacity(HttpClient client, String debugTag, String requestId, String tokenId, String service, CzamCapacity czamCapacity) throws CestzamClientError {
        boolean capacityRequired;
        boolean capacitySet;
        {
            String capacityCheckPayload = Json.createObjectBuilder(Map.of(
                    "tokenId", tokenId,
                    "service", service,
                    "requestId", requestId,
                    "capacity", ""
            )).build().toString();
            HttpResponse<String> capacityCheckResponse = cestzamRequestService.postJsonGetJson(debugTag, client, capacityCheckPayload, CZAM_ORIGIN, "fasui/api/capacity/_check");
            checkNoError(capacityCheckResponse);
            // {
            //    "capacityRequired": true,
            //    "capacitySet": false
            //}
            JsonReader capacityCheckReader = Json.createReader(new StringReader(capacityCheckResponse.body()));
            JsonObject capacityCheckResponseObject = capacityCheckReader.readObject();
            capacityRequired = capacityCheckResponseObject.getBoolean("capacityRequired");
            capacitySet = capacityCheckResponseObject.getBoolean("capacitySet");
        }

        if (capacityRequired && !capacitySet) {
            String capacityPayload = Json.createObjectBuilder(Map.of(
                    "tokenId", tokenId,
                    "service", service,
                    "requestId", requestId,
                    "capacity", czamCapacity.getName()
            )).build().toString();
            HttpResponse<String> capacityResponse = cestzamRequestService.postJsonGetJson(debugTag, client, capacityPayload, CZAM_ORIGIN, "fasui/api/capacity");
            checkNoError(capacityResponse);
            JsonReader capacityCheckReader = Json.createReader(new StringReader(capacityResponse.body()));
            JsonObject capacityCheckResponseObject = capacityCheckReader.readObject();
            capacityRequired = capacityCheckResponseObject.getBoolean("capacityRequired");
            capacitySet = capacityCheckResponseObject.getBoolean("capacitySet");
        }

        if (capacityRequired || !capacitySet) {
            throw new CestzamClientError("Unable to set capacity");
        }
    }

}
