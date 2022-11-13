package be.valuya.cestzam.ws.resource;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentProvider;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentsResource;
import be.valuya.cestzam.api.service.myminfin.vatbalance.MyMininVatBalanceResource;
import be.valuya.cestzam.api.service.myminfin.vatbalance.MyminfinCurrentVatBalance;
import be.valuya.cestzam.api.service.myminfin.vatbalance.MyminfinVatBalanceSearch;
import be.valuya.cestzam.api.util.ResultPage;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;
import be.valuya.cestzam.client.myminfin.rest.MyminfinVatBalanceClientService;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentProvidersResponse;
import be.valuya.cestzam.client.myminfin.rest.vatbalance.CurrentVatBalance;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.ws.converter.CestzamConverterService;
import be.valuya.cestzam.ws.converter.MyminfinVatBalanceConverter;
import be.valuya.cestzam.ws.converter.MymininDocumentConverter;
import be.valuya.cestzam.ws.util.ConfigParam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BeanParam;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class MyMinfinVatBalanceController implements MyMininVatBalanceResource {

    @Inject
    private MyminfinVatBalanceClientService myminfinVatBalanceClientService;
    @Inject
    private CestzamConverterService cestzamConverterService;
    @Inject
    private MyminfinVatBalanceConverter myminfinVatBalanceConverter;
    @Context
    private HttpServletResponse servletResponse;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @BeanParam
    private ConfigParam configParam;


    @Override
    public MyminfinCurrentVatBalance getCurrentVatBalance(MyminfinVatBalanceSearch vatBalanceSearch) {
        AuthenticatedMyminfinContext myminfinContext = vatBalanceSearch.getMyminfinContext();
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        try {
            CurrentVatBalance currentVatBalance = myminfinVatBalanceClientService.getCurrentVatBalance(authenticatedMyminfinContext);
            return myminfinVatBalanceConverter.toMyminfinCurrentVatBalance(currentVatBalance);
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public void close() throws IOException {

    }

}
