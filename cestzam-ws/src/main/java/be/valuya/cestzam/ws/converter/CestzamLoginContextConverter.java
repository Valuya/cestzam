package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.czam.CestzamLoginContext;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CestzamLoginContextConverter {

    public CestzamLoginContext toCestzamLoginContext(GenericCestzamContext cestzamContext) {
        String service = cestzamContext.getService();
        String spEntityId = cestzamContext.getSpEntityId();
        String gotoValue = cestzamContext.getGotoValue();
        CestzamCookies cookies = cestzamContext.getCookies();
        CestzamLoginContext loginContext = new CestzamLoginContext(spEntityId, service, gotoValue, cookies);
        return loginContext;
    }
}
