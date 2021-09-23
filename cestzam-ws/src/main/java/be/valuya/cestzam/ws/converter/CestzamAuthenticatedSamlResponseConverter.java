package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.czam.CestzamAuthenticatedSamlResponse;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class CestzamAuthenticatedSamlResponseConverter {

    public CestzamAuthenticatedSamlResponse toCestzamAuthenticatedSamlResponse(GenericCestzamContext cestzamContext) {
        CestzamCookies cookies = cestzamContext.getCookies();
        String relayState = cestzamContext.getRelayState();
        String samlResponse = cestzamContext.getSamlResponse();
        String serviceRedirectUrl = cestzamContext.getServiceRedirectUrl();

        CestzamAuthenticatedSamlResponse cestzamAuthenticatedSamlResponse = new CestzamAuthenticatedSamlResponse(cookies, samlResponse, relayState,
                Optional.ofNullable(serviceRedirectUrl));
        return cestzamAuthenticatedSamlResponse;
    }
}
