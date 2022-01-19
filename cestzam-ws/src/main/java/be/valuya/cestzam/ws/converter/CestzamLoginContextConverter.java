package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.czam.CestzamLoginContext;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;

@ApplicationScoped
public class CestzamLoginContextConverter {

    public CestzamLoginContext toCestzamLoginContext(GenericCestzamContext cestzamContext) {
        String loginUriString = cestzamContext.getLoginUri();
        URI loginUri = URI.create(loginUriString);
        CestzamCookies cookies = cestzamContext.getCookies();
        String saml2RequestToken = cestzamContext.getSaml2RequestToken();
        String secondVisitUrl = cestzamContext.getSecondVisitUrl();
        CestzamLoginContext loginContext = new CestzamLoginContext(loginUri, saml2RequestToken, secondVisitUrl, cookies);
        return loginContext;
    }
}
