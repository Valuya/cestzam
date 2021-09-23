package be.valuya.cestzam.ws.resource;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import be.valuya.cestzam.api.service.myminfin.user.MyminfinUser;
import be.valuya.cestzam.api.service.myminfin.user.MyminfinUserResource;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;
import be.valuya.cestzam.client.myminfin.rest.MyminfinRestClientService;
import be.valuya.cestzam.client.myminfin.rest.UserData;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.ws.converter.MyminfinUserConverter;
import be.valuya.cestzam.ws.converter.CestzamConverterService;
import be.valuya.cestzam.ws.util.ConfigParam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.ServiceUnavailableException;
import java.io.IOException;
import java.util.Optional;

@ApplicationScoped
public class MyminfinUserController implements MyminfinUserResource {

    @Inject
    private MyminfinRestClientService myminfinRestClientService;
    @Inject
    private CestzamConverterService cestzamConverterService;
    @Inject
    private MyminfinUserConverter myminfinUserConverter;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @BeanParam
    private ConfigParam configParam;

    @Override
    public MyminfinUser getLoggedUserData(AuthenticatedMyminfinContext myminfinContext) {
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        try {
            UserData userData = myminfinRestClientService.getUserData(authenticatedMyminfinContext.getCookies());
            MyminfinUser myminfinUser = myminfinUserConverter.toMyminfinUser(userData);
            return myminfinUser;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public void close() throws IOException {

    }
}
