package be.valuya.cestzam.ws.resource;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import be.valuya.cestzam.api.service.myminfin.MyMinfinLoginResponse;
import be.valuya.cestzam.api.service.myminfin.MyminfinCompleteLoginRequest;
import be.valuya.cestzam.api.service.myminfin.MyminfinLoginResource;
import be.valuya.cestzam.client.czam.CestzamAuthenticatedSamlResponse;
import be.valuya.cestzam.client.czam.CestzamLoginContext;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.MyminfinClientService;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.ws.converter.GenericCestzamContext;
import be.valuya.cestzam.ws.converter.CestzamAuthenticatedSamlResponseConverter;
import be.valuya.cestzam.ws.converter.CestzamConverterService;
import be.valuya.cestzam.ws.util.ConfigParam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.ServiceUnavailableException;
import java.io.IOException;
import java.util.Optional;


@ApplicationScoped
public class MyminfinLoginController implements MyminfinLoginResource {

    @Inject
    private MyminfinClientService myminfinClientService;
    @Inject
    private CestzamConverterService cestzamConverterService;
    @Inject
    private CestzamAuthenticatedSamlResponseConverter cestzamAuthenticatedSamlResponseConverter;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @BeanParam
    private ConfigParam configParam;


    public MyMinfinLoginResponse startLogin() {
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);
        try {
            CestzamLoginContext cestzamLoginContext = myminfinClientService.startLoginFlow();
            MyMinfinLoginResponse response = new MyMinfinLoginResponse();
            cestzamConverterService.setCestzamContext(response, cestzamLoginContext);
            return response;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(Long.valueOf(60), cestzamClientError);
        }
    }

    @Override
    public AuthenticatedMyminfinContext completeLogin(MyminfinCompleteLoginRequest completeLoginRequest) {
        GenericCestzamContext genericCestzamContext = cestzamConverterService.getCestzamContext(completeLoginRequest);
        String requestedVatNumber = completeLoginRequest.getRequestedVatNumber();
        CestzamAuthenticatedSamlResponse cestzamAuthenticatedSamlResponse = cestzamAuthenticatedSamlResponseConverter.toCestzamAuthenticatedSamlResponse(genericCestzamContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        try {
            CestzamAuthenticatedMyminfinContext cestzamAuthenticatedMyminfinContext;
            if (requestedVatNumber == null) {
                cestzamAuthenticatedMyminfinContext = myminfinClientService.completeLoginFlow(cestzamAuthenticatedSamlResponse);
            } else {
                cestzamAuthenticatedMyminfinContext = myminfinClientService.completeLoginFlow(requestedVatNumber, cestzamAuthenticatedSamlResponse);
            }
            AuthenticatedMyminfinContext myminfinContextResponse = new AuthenticatedMyminfinContext();
            cestzamConverterService.setCestzamCookies(myminfinContextResponse, cestzamAuthenticatedMyminfinContext.getCookies());
            return myminfinContextResponse;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(Long.valueOf(60), cestzamClientError);
        }
    }

    @Override
    public void close() throws IOException {

    }
}
