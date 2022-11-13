package be.valuya.cestzam.client.czam;

import be.valuya.cestzam.client.cookie.CestzamCookies;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CestzamTokenVerificationResponse {
    private CestzamCookies cookies;
    private String czamApiVersion;
    private boolean czamApiVersionSupported;
    private String czamRequestId;
    private String codeLabel;
    private int codeNumber;
    private String authId;

    private String saml2TokenRequest;
    private String secondVisitUrl;

}
