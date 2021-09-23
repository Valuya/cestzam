package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.api.context.WithCestzamContext;
import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.czam.CzamCitizenInfo;
import be.valuya.cestzam.client.czam.CestzamLoginContext;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.JsonbBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@ApplicationScoped
public class CestzamConverterService {

    public <T extends WithCestzamContext> void setCestzamContext(T withContext, CestzamLoginContext cestzamLoginContext) {
        GenericCestzamContext genericCestzamContext = new GenericCestzamContext();
        genericCestzamContext.setCookies(cestzamLoginContext.getCestzamCookies());
        genericCestzamContext.setGotoValue(cestzamLoginContext.getGotoValue());
        genericCestzamContext.setSpEntityId(cestzamLoginContext.getSpEntityId());
        genericCestzamContext.setService(cestzamLoginContext.getService());
        String encodedContext = encodeContext(genericCestzamContext);
        withContext.setCestzamContext(encodedContext);
    }

    public <T extends WithCestzamContext> void setCestzamContext(T withContext, CestzamAuthenticatedMyminfinContext myminfinContext) {
        GenericCestzamContext genericCestzamContext = new GenericCestzamContext();
        genericCestzamContext.setCookies(myminfinContext.getCookies());
        String encodedContext = encodeContext(genericCestzamContext);
        withContext.setCestzamContext(encodedContext);
    }

    public <T extends WithCestzamContext> void setCestzamCookies(T withContext, CestzamCookies cestzamCookies) {
        GenericCestzamContext genericCestzamContext = new GenericCestzamContext();
        genericCestzamContext.setCookies(cestzamCookies);
        setCestzamContext(withContext, genericCestzamContext);
    }

    public <T extends WithCestzamContext> void setCestzamContext(T withContext, GenericCestzamContext genericCestzamContext) {
        String encodedContext = encodeContext(genericCestzamContext);
        withContext.setCestzamContext(encodedContext);
    }

    public <T extends WithCestzamContext> GenericCestzamContext getCestzamContext(T withContext) {
        String cestzamContext = withContext.getCestzamContext();
        GenericCestzamContext genericCestzamContext = decodeContext(cestzamContext);
        return genericCestzamContext;
    }

    public <T extends WithCestzamContext> CestzamAuthenticatedMyminfinContext getAuthenticatedMyminfinContext(T withContext) {
        String cestzamContext = withContext.getCestzamContext();
        GenericCestzamContext genericCestzamContext = decodeContext(cestzamContext);
        CzamCitizenInfo czamCitizenInfo = new CzamCitizenInfo(
                genericCestzamContext.getFirstNames(),
                genericCestzamContext.getLastNames(),
                genericCestzamContext.getSsin()
        );
        CestzamAuthenticatedMyminfinContext myminfinContext = new CestzamAuthenticatedMyminfinContext(genericCestzamContext.getCookies(), czamCitizenInfo);
        return myminfinContext;
    }

    private String encodeContext(GenericCestzamContext genericCestzamContext) {
        String cookiesJson = JsonbBuilder.create()
                .toJson(genericCestzamContext);
        String encoded = Base64.getEncoder().encodeToString(cookiesJson.getBytes(StandardCharsets.UTF_8));
        return encoded;
    }


    private GenericCestzamContext decodeContext(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        GenericCestzamContext cestzamContext = JsonbBuilder.create()
                .fromJson(new String(decodedBytes, StandardCharsets.UTF_8), GenericCestzamContext.class);
        return cestzamContext;
    }
}
