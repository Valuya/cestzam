package be.valuya.cestzam.ws.resource;

import be.valuya.cestzam.api.context.AuthenticatedCestzamContext;
import be.valuya.cestzam.api.login.Capacity;
import be.valuya.cestzam.api.login.token.TokenCodeRequest;
import be.valuya.cestzam.api.login.token.TokenCodesLoginRequest;
import be.valuya.cestzam.api.login.token.TokenLoginRequest;
import be.valuya.cestzam.api.login.token.TokenLoginResource;
import be.valuya.cestzam.api.login.token.TokenLoginResponse;
import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.czam.CzamCapacity;
import be.valuya.cestzam.client.czam.CzamLoginClientService;
import be.valuya.cestzam.client.czam.CestzamAuthenticatedSamlResponse;
import be.valuya.cestzam.client.czam.CestzamLoginContext;
import be.valuya.cestzam.client.czam.CestzamTokenVerificationResponse;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.ws.converter.CzamCapacityConverter;
import be.valuya.cestzam.ws.converter.GenericCestzamContext;
import be.valuya.cestzam.ws.converter.CestzamConverterService;
import be.valuya.cestzam.ws.converter.CestzamLoginContextConverter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ServiceUnavailableException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class TokenLoginController implements TokenLoginResource {

    @Inject
    private CzamLoginClientService loginClientService;
    @Inject
    private CestzamConverterService cestzamConverterService;
    @Inject
    private CzamCapacityConverter czamCapacityConverter;
    @Inject
    private CestzamLoginContextConverter loginContextConverter;

    @Override
    public TokenLoginResponse login(TokenLoginRequest request) {
        String login = request.getLogin();
        String password = request.getPassword();
        GenericCestzamContext cestzamContext = cestzamConverterService.getCestzamContext(request);
        CestzamLoginContext loginContext = loginContextConverter.toCestzamLoginContext(cestzamContext);

        try {
            CestzamTokenVerificationResponse cestzamTokenVerificationResponse = loginClientService.startTokenLoginFlow(loginContext, login, password);
            int codeNumber = cestzamTokenVerificationResponse.getCodeNumber();
            String codeLabel = cestzamTokenVerificationResponse.getCodeLabel();
            CestzamCookies cookies2 = cestzamTokenVerificationResponse.getCookies();
            String czamRequestId = cestzamTokenVerificationResponse.getCzamRequestId();

            GenericCestzamContext genricContext2 = new GenericCestzamContext();
            genricContext2.setRequestId(czamRequestId);
            genricContext2.setCookies(cookies2);

            TokenLoginResponse loginResponse = new TokenLoginResponse();
            loginResponse.setVerificationTokenNumberLabel(codeLabel);
            cestzamConverterService.setCestzamContext(loginResponse, genricContext2);
            return loginResponse;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public AuthenticatedCestzamContext verifyCode(TokenCodeRequest request) {
        String tokenVerificationCode = request.getTokenVerificationCode();
        Capacity capacity = request.getCapacity();
        CzamCapacity czamCapacity = czamCapacityConverter.toCzamCapcity(capacity);
        GenericCestzamContext cestzamContext = cestzamConverterService.getCestzamContext(request);
        String requestId = cestzamContext.getRequestId();
        CestzamCookies cookies = cestzamContext.getCookies();

        try {
            CestzamAuthenticatedSamlResponse cestzamAuthenticatedSamlResponse = loginClientService.completeTokenLoginFlow(cookies, czamCapacity, requestId, tokenVerificationCode);
            CestzamCookies cookies2 = cestzamAuthenticatedSamlResponse.getCookies();
            String relayState = cestzamAuthenticatedSamlResponse.getRelayState();
            String samlResponse = cestzamAuthenticatedSamlResponse.getSamlResponse();
            GenericCestzamContext genericCestzamContext = new GenericCestzamContext();
            genericCestzamContext.setCookies(cookies2);
            genericCestzamContext.setRelayState(relayState);
            genericCestzamContext.setSamlResponse(samlResponse);

            AuthenticatedCestzamContext authenticatedCestzamContext = new AuthenticatedCestzamContext();
            cestzamConverterService.setCestzamContext(authenticatedCestzamContext, genericCestzamContext);
            return authenticatedCestzamContext;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public AuthenticatedCestzamContext loginAndVerifyCode(TokenCodesLoginRequest request) {
        String login = request.getLogin();
        String password = request.getPassword();
        Capacity capacity = request.getCapacity();
        Map<Integer, String> codes = this.getRequestCodes(request);
        CzamCapacity czamCapacity = czamCapacityConverter.toCzamCapcity(capacity);
        GenericCestzamContext cestzamContext = cestzamConverterService.getCestzamContext(request);
        String service = cestzamContext.getService();
        String spEntityId = cestzamContext.getSpEntityId();
        String gotoValue = cestzamContext.getGotoValue();
        CestzamCookies cookies = cestzamContext.getCookies();
        CestzamLoginContext loginContext = new CestzamLoginContext(spEntityId, service, gotoValue, cookies);

        try {
            CestzamAuthenticatedSamlResponse cestzamAuthenticatedSamlResponse = loginClientService.doTokenLogin(loginContext, czamCapacity, login, password, codes);
            CestzamCookies cookies2 = cestzamAuthenticatedSamlResponse.getCookies();
            String relayState = cestzamAuthenticatedSamlResponse.getRelayState();
            String samlResponse = cestzamAuthenticatedSamlResponse.getSamlResponse();
            Optional<String> serviceRedirectUrl = cestzamAuthenticatedSamlResponse.getServiceRedirectUrl();
            GenericCestzamContext genericCestzamContext = new GenericCestzamContext();
            genericCestzamContext.setCookies(cookies2);
            genericCestzamContext.setRelayState(relayState);
            genericCestzamContext.setSamlResponse(samlResponse);
            serviceRedirectUrl.ifPresent(genericCestzamContext::setServiceRedirectUrl);

            AuthenticatedCestzamContext authenticatedCestzamContext = new AuthenticatedCestzamContext();
            cestzamConverterService.setCestzamContext(authenticatedCestzamContext, genericCestzamContext);
            return authenticatedCestzamContext;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }

    }

    private Map<Integer, String> getRequestCodes(TokenCodesLoginRequest request) {
        Map<String, String> codes = request.getCodes();
        if (codes != null) {
            return codes.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            e -> Integer.parseInt(e.getKey()),
                            Map.Entry::getValue
                    ));
        }
        throw new BadRequestException("No codes provided");
    }

    @Override
    public void close() throws IOException {

    }
}
