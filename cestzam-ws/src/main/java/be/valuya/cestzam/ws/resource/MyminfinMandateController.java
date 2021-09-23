package be.valuya.cestzam.ws.resource;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import be.valuya.cestzam.api.service.myminfin.mandate.MandateActivationRequest;
import be.valuya.cestzam.api.service.myminfin.mandate.MyMinfinMandate;
import be.valuya.cestzam.api.service.myminfin.mandate.MyminfinMandateResource;
import be.valuya.cestzam.api.service.myminfin.mandate.MyminfinMandateSearch;
import be.valuya.cestzam.api.service.myminfin.mandate.MyminfinMandateType;
import be.valuya.cestzam.api.util.ResultPage;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;
import be.valuya.cestzam.client.myminfin.rest.MyminfinCustomerRestClientService;
import be.valuya.cestzam.client.myminfin.rest.mandate.ApplicationMandate;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.ws.converter.MyminfinMandateConverter;
import be.valuya.cestzam.ws.converter.CestzamConverterService;
import be.valuya.cestzam.ws.util.ConfigParam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.BeanParam;
import javax.ws.rs.ServiceUnavailableException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class MyminfinMandateController implements MyminfinMandateResource {

    @Inject
    private MyminfinCustomerRestClientService customerRestClientService;

    @Inject
    private CestzamConverterService cestzamConverterService;
    @Inject
    private MyminfinMandateConverter myminfinMandateConverter;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @BeanParam
    private ConfigParam configParam;


    @Override
    public ResultPage<MyMinfinMandate> listMandates(MyminfinMandateSearch mandateSearch) {
        MyminfinMandateType mandateType = mandateSearch.getMandateType();
        AuthenticatedMyminfinContext myminfinContext = mandateSearch.getMyminfinContext();
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        try {
            List<ApplicationMandate> applicationMandates = this.listMandates(mandateType, authenticatedMyminfinContext);
            List<MyMinfinMandate> minfinMandateList = applicationMandates.stream()
                    .map(myminfinMandateConverter::toMyMinfinMandate)
                    .collect(Collectors.toList());
            ResultPage<MyMinfinMandate> resultPage = new ResultPage<>(minfinMandateList.size(), minfinMandateList);
            return resultPage;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }


    @Override
    public AuthenticatedMyminfinContext activateMandate(MandateActivationRequest mandateActivationRequest) {
        MyMinfinMandate mandate = mandateActivationRequest.getMandate();
        AuthenticatedMyminfinContext myminfinContext = mandateActivationRequest.getMyminfinContext();
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        ApplicationMandate applicationMandate = myminfinMandateConverter.toApplicationMandate(mandate);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        try {
            CestzamAuthenticatedMyminfinContext updatedContext = customerRestClientService.activateMandate(authenticatedMyminfinContext, applicationMandate);
            AuthenticatedMyminfinContext updatedMyminfinContext = new AuthenticatedMyminfinContext();
            cestzamConverterService.setCestzamContext(updatedMyminfinContext, updatedContext);
            return updatedMyminfinContext;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    @Override
    public AuthenticatedMyminfinContext deactivateMandate(AuthenticatedMyminfinContext myminfinContext) {
        CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext = cestzamConverterService.getAuthenticatedMyminfinContext(myminfinContext);
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        try {
            CestzamAuthenticatedMyminfinContext updatedContext = customerRestClientService.deactivateMandate(authenticatedMyminfinContext);
            AuthenticatedMyminfinContext updatedMyminfinContext = new AuthenticatedMyminfinContext();
            cestzamConverterService.setCestzamContext(updatedMyminfinContext, updatedContext);
            return updatedMyminfinContext;
        } catch (CestzamClientError cestzamClientError) {
            throw new ServiceUnavailableException(60L, cestzamClientError);
        }
    }

    private List<ApplicationMandate> listMandates(MyminfinMandateType mandateType, CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext) throws CestzamClientError {
        switch (mandateType) {
            case CITIZEN:
                return customerRestClientService.getCitizenMandates(authenticatedMyminfinContext);
            case ENTERPRISE:
                return customerRestClientService.getEnterpriseMandates(authenticatedMyminfinContext);
            default:
                throw new BadRequestException("Invalid mandate type " + mandateType);
        }
    }

    @Override
    public void close() throws IOException {

    }
}
