package be.valuya.cestzam.client.czam;

import be.valuya.cestzam.client.cookie.CestzamCookies;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;

@Getter
@Setter
@AllArgsConstructor
public class CestzamLoginContext {
    private URI loginUri;
    private String saml2RequestToken;
    private String secondVisitUrl;
    private CestzamCookies cestzamCookies;
}
