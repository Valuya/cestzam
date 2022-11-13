package be.valuya.cestzam.client.czam;

import be.valuya.cestzam.client.cookie.CestzamCookies;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class CestzamAuthenticatedSamlResponse implements CestzamSamlResponse {
    private CestzamCookies cookies;
    private String apiVersion;
    private Boolean apiVersionSupported;
    private String samlResponse;
    private String relayState;
    private Optional<String> serviceRedirectUrl;

}
