package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.czam.CzamCapacity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A generic context sent back and forth between cestzam clients and cestzam rest api.
 * The cestzam cookies reprensent the cookies cestzam obtained as a client to the public services.
 * Other fields may contains user session information, mostly used during authentication.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GenericCestzamContext {

    private CestzamCookies cookies;
    // On saml respnse only:
    private String samlResponse;
    private String relayState;
    private String fgovWResult;
    // On login request only:
    private String spEntityId;
    private String service;
    private String gotoValue;
    // The redirect url from czam back to the service
    private String serviceRedirectUrl;
    private CzamCapacity czamCapacity;
    // On login flow response
    private String requestId;
    // When citizen info fetched
    private String ssin;
    private String lastNames;
    private String firstNames;
}
