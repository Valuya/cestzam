package be.valuya.cestzam.api.client;

import be.valuya.cestzam.api.client.domain.MyminfinDocumentFilter;
import be.valuya.cestzam.api.client.domain.MyminfinDocumentStream;
import be.valuya.cestzam.api.client.state.MyminfinState;
import be.valuya.cestzam.api.context.AuthenticatedCestzamContext;
import be.valuya.cestzam.api.login.Capacity;
import be.valuya.cestzam.api.login.token.TokenCodesLoginRequest;
import be.valuya.cestzam.api.login.token.TokenLoginResource;
import be.valuya.cestzam.api.service.ServiceHealthCheckResponse;
import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import be.valuya.cestzam.api.service.myminfin.MyMinfinLoginResponse;
import be.valuya.cestzam.api.service.myminfin.MyminfinCompleteLoginRequest;
import be.valuya.cestzam.api.service.myminfin.MyminfinHealthResource;
import be.valuya.cestzam.api.service.myminfin.MyminfinLoginResource;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocument;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentKey;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentProvider;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentsDownloadRequest;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentsProvidersSearch;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentsResource;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentsSearch;
import be.valuya.cestzam.api.service.myminfin.mandate.MandateActivationRequest;
import be.valuya.cestzam.api.service.myminfin.mandate.MyMinfinMandate;
import be.valuya.cestzam.api.service.myminfin.mandate.MyminfinMandateResource;
import be.valuya.cestzam.api.service.myminfin.mandate.MyminfinMandateSearch;
import be.valuya.cestzam.api.service.myminfin.mandate.MyminfinMandateType;
import be.valuya.cestzam.api.service.myminfin.ubo.MyminfinUboResource;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompany;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanyCompositionNode;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanySearch;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanySearchResult;
import be.valuya.cestzam.api.service.myminfin.ubo.UboRequestContext;
import be.valuya.cestzam.api.service.myminfin.user.MyminfinUser;
import be.valuya.cestzam.api.service.myminfin.user.MyminfinUserResource;
import be.valuya.cestzam.api.service.myminfin.user.MyminfinUserType;
import be.valuya.cestzam.api.service.myminfin.vatbalance.MyMininVatBalanceResource;
import be.valuya.cestzam.api.service.myminfin.vatbalance.MyminfinCurrentVatBalance;
import be.valuya.cestzam.api.service.myminfin.vatbalance.MyminfinVatBalanceSearch;
import be.valuya.cestzam.api.util.ResultPage;

import javax.validation.Valid;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MyminfinApiClient implements AutoCloseable {

    private final MyminfinLoginResource loginResource;
    private final MyminfinUserResource userResource;
    private final MyminfinMandateResource mandateResource;
    private final MyminfinDocumentsResource documentsResource;
    private final MyMininVatBalanceResource vatBalanceResource;
    private final MyminfinUboResource uboResource;
    private final TokenLoginResource tokenLoginResource;
    private final CestzamApiClient cestzamApiClient;
    private final MyminfinHealthResource healthResource;

    private final MyminfinState myminfinState = new MyminfinState();

    public MyminfinApiClient(CestzamApiClient cestzamApiClient) {
        this.cestzamApiClient = cestzamApiClient;
        this.loginResource = cestzamApiClient.getClientBuilder()
                .build(MyminfinLoginResource.class);
        tokenLoginResource = cestzamApiClient.getClientBuilder()
                .build(TokenLoginResource.class);
        userResource = cestzamApiClient.getClientBuilder()
                .build(MyminfinUserResource.class);
        mandateResource = cestzamApiClient.getClientBuilder()
                .build(MyminfinMandateResource.class);
        documentsResource = cestzamApiClient.getClientBuilder()
                .build(MyminfinDocumentsResource.class);
        vatBalanceResource = cestzamApiClient.getClientBuilder()
                .build(MyMininVatBalanceResource.class);
        uboResource = cestzamApiClient.getClientBuilder()
                .build(MyminfinUboResource.class);
        healthResource = cestzamApiClient.getClientBuilder()
                .build(MyminfinHealthResource.class);
    }

    @Override
    public void close() throws Exception {
        loginResource.close();
        userResource.close();
        mandateResource.close();
        documentsResource.close();
        vatBalanceResource.close();
        uboResource.close();
        tokenLoginResource.close();
        healthResource.close();
    }

    public ServiceHealthCheckResponse checkHealth() {
        ServiceHealthCheckResponse healthCheckResponse = healthResource.checkHealth();
        return healthCheckResponse;
    }

    /**
     * Login for an enterprise, using the provided vat number if multiple enterprises are avalaible.
     *
     * @param login
     * @param password
     * @param tokenCodes
     * @param entrepriseVat
     */
    public void authenticateWithToken(String login, String password, Map<String, String> tokenCodes, String entrepriseVat) {
        authenticateWithToken(login, password, tokenCodes, Capacity.ENTERPRISE, Optional.of(entrepriseVat));
    }

    /**
     * Login for the provided capacity.
     * If the capacity is ENTERPRISE and multiple enterprise are available, use the method taking the enterprise vat
     * number as final argument.
     *
     * @param login
     * @param password
     * @param tokenCodes
     * @param capacity
     */
    public void authenticateWithToken(String login, String password, Map<String, String> tokenCodes, Capacity capacity) {
        authenticateWithToken(login, password, tokenCodes, capacity, Optional.empty());
    }


    private void authenticateWithToken(String login, String password, Map<String, String> tokenCodes, Capacity capacity, Optional<String> enterpriseVatNumber) {
        MyMinfinLoginResponse response = loginResource.startLogin();

        TokenCodesLoginRequest codesLoginRequest = new TokenCodesLoginRequest(login, password, capacity, tokenCodes);
        codesLoginRequest.setCestzamContext(response.getCestzamContext());
        AuthenticatedCestzamContext authenticatedCestzamContext = tokenLoginResource.loginAndVerifyCode(codesLoginRequest);

        MyminfinCompleteLoginRequest loginRequest = new MyminfinCompleteLoginRequest();
        loginRequest.setCestzamContext(authenticatedCestzamContext.getCestzamContext());
        enterpriseVatNumber.ifPresent(loginRequest::setRequestedVatNumber);
        AuthenticatedMyminfinContext myminfinContext = loginResource.completeLogin(loginRequest);

        this.resetMyminfinState(myminfinContext);
    }

    public boolean isAuthenticated() {
        return myminfinState.getUserOptional().isPresent();
    }

    public Optional<MyminfinUser> getAuthenticatedUserOptional() {
        return myminfinState.getUserOptional();
    }

    public MyminfinUser getAuthenticatedUser() throws MyminfinApiClientError {
        return myminfinState.getUserOptional()
                .orElseThrow(() -> new MyminfinApiClientError("Not authenticated"));
    }

    public List<MyMinfinMandate> getAvailableMandates(MyminfinMandateType mandateType) throws MyminfinApiClientError {
        switch (mandateType) {
            case CITIZEN:
                return myminfinState.getAvailableCitizenMandatesOptional()
                        .orElseGet(() -> {
                            List<MyMinfinMandate> mandateList = this.loadMandates(mandateType);
                            myminfinState.setAvailableCitizenMandates(mandateList);
                            return mandateList;
                        });
            case ENTERPRISE:
                return myminfinState.getAvailableEnterpriseMandatesOptional()
                        .orElseGet(() -> {
                            List<MyMinfinMandate> mandateList = this.loadMandates(mandateType);
                            myminfinState.setAvailableEnterpriseMandates(mandateList);
                            return mandateList;
                        });
            default:
                throw new MyminfinApiClientError("Unknown mandate type " + mandateType.name());
        }
    }

    public Optional<MyMinfinMandate> getActiveMandateOptional() {
        MyminfinUser authenticatedUser = getAuthenticatedUser();
        boolean customerSelected = Optional.ofNullable(authenticatedUser.getCustomerSelected())
                .orElse(false);
        if (!customerSelected) {
            return Optional.empty();
        }
        MyminfinUserType customerType = authenticatedUser.getCustomerType();
        String customerVat = authenticatedUser.getCustomerVat();
        String customerNN = authenticatedUser.getCustomerNN();

        MyminfinMandateType mandateType = getMandateType(customerType);
        List<MyMinfinMandate> availableMandates = getAvailableMandates(mandateType);
        Optional<MyMinfinMandate> activeMandateOptional = availableMandates.stream()
                .filter(m -> {
                    String mandatorIdentifier = m.getMandatorIdentifier();
                    return mandatorIdentifier.equalsIgnoreCase(customerVat)
                            || mandatorIdentifier.equalsIgnoreCase(customerNN);
                })
                .findAny();
        if (activeMandateOptional.isEmpty()) {
            throw new MyminfinApiClientError("A mandate is active, but could not be found in the available mandate list");
        }
        return activeMandateOptional;
    }

    public void setActiveMandate(MyMinfinMandate mandate) {
        AuthenticatedMyminfinContext authenticatedContext = getAuthenticatedContext();
        MandateActivationRequest activationRequest = new MandateActivationRequest();
        activationRequest.setMandate(mandate);
        activationRequest.setMyminfinContext(authenticatedContext);

        AuthenticatedMyminfinContext myminfinContext = mandateResource.activateMandate(activationRequest);
        resetMyminfinState(myminfinContext);
    }

    public void unsetActiveMandate() {
        AuthenticatedMyminfinContext authenticatedContext = getAuthenticatedContext();
        AuthenticatedMyminfinContext myminfinContext = mandateResource.deactivateMandate(authenticatedContext);
        resetMyminfinState(myminfinContext);
    }


    public ResultPage<MyminfinDocumentProvider> listDocumentsProviders() {
        AuthenticatedMyminfinContext authenticatedContext = getAuthenticatedContext();

        MyminfinDocumentsProvidersSearch providersSearch = new MyminfinDocumentsProvidersSearch();
        providersSearch.setMyminfinContext(authenticatedContext);
        ResultPage<MyminfinDocumentProvider> documentProvidersPage = documentsResource.searchDocumentProviders(providersSearch);
        return documentProvidersPage;
    }

    public ResultPage<MyminfinDocument> searchDocuments(@Valid MyminfinDocumentFilter documentFilter) {
        String provider = documentFilter.getProviderOptional()
                .orElseThrow(() -> new MyminfinApiClientError("No document provider provided"));
        MyminfinDocumentsSearch myminfinDocumentsSearch = new MyminfinDocumentsSearch();
        myminfinDocumentsSearch.setMyminfinContext(getAuthenticatedContext());
        myminfinDocumentsSearch.setProvider(provider);
        ResultPage<MyminfinDocument> alLDocumentsPage = documentsResource.searchDocuments(myminfinDocumentsSearch);
        int totalCount = alLDocumentsPage.getTotalCount();

        List<MyminfinDocument> filterdDocuments = alLDocumentsPage.getPageItems()
                .stream()
                .filter(doc -> this.isDocumentIncluded(doc, documentFilter))
                .collect(Collectors.toList());
        return new ResultPage<>(totalCount, filterdDocuments);
    }

    public MyminfinDocumentStream downloadDocument(MyminfinDocumentKey documentKey) {
        MyminfinDocumentsDownloadRequest downloadRequest = new MyminfinDocumentsDownloadRequest();
        downloadRequest.setDocumentKey(documentKey);
        downloadRequest.setMyminfinContext(getAuthenticatedContext());
        InputStream documentStream = documentsResource.downloadDocument(downloadRequest);

        MyminfinDocumentStream myminfinDocumentStream = new MyminfinDocumentStream();
        myminfinDocumentStream.setInputStream(documentStream);

        // TODO: use resppnse header values
        String providerName = documentKey.getProviderName();
        MyminfinDocumentFilter documentFilter = new MyminfinDocumentFilter();
        documentFilter.setProvider(providerName);
        searchDocuments(documentFilter).getPageItems()
                .stream()
                .filter(d -> d.getDocumentKey().equals(documentKey))
                .findAny()
                .ifPresent(docDesc -> {
                    String documentTitle = docDesc.getDocumentTitle();
                    myminfinDocumentStream.setFileName(documentTitle + ".pdf");
                    myminfinDocumentStream.setMimeType("application/pdf");
                });
        return myminfinDocumentStream;
    }

    public MyminfinCurrentVatBalance getCurrentVatBalance() {
        MyminfinVatBalanceSearch vatBalanceSearch = new MyminfinVatBalanceSearch();
        vatBalanceSearch.setMyminfinContext(getAuthenticatedContext());
        return vatBalanceResource.getCurrentVatBalance(vatBalanceSearch);
    }

    public UboCompanySearchResult searchUboCompanies() {
        return searchUboCompanies(1, 20, Optional.empty(), Optional.empty());
    }

    public UboCompanySearchResult searchUboCompanies(int page, int limit,
                                                     Optional<String> identificationNumberOptional,
                                                     Optional<String> nameOptional) {
        UboCompanySearch uboCompanySearch = new UboCompanySearch();
        uboCompanySearch.setMyminfinContext(getAuthenticatedContext());
        uboCompanySearch.setPage(page);
        uboCompanySearch.setLimit(limit);
        identificationNumberOptional.ifPresent(uboCompanySearch::setIdentificationNumber);
        nameOptional.ifPresent(uboCompanySearch::setName);

        return uboResource.searchCompanies(uboCompanySearch);
    }

    public UboCompanyCompositionNode getUboCompanyComposition(String companyId) {
        UboRequestContext requestContext = new UboRequestContext();
        requestContext.setMyminfinContext(getAuthenticatedContext());

        return uboResource.getCompanyComposition(companyId, requestContext);
    }

    public UboCompany confirmUboCompanyComposition(String companyId) {
        UboRequestContext requestContext = new UboRequestContext();
        requestContext.setMyminfinContext(getAuthenticatedContext());

        return uboResource.confirmCompanyComposition(companyId, requestContext);
    }

    public InputStream getUboDocumentContent(Long documentId) {
        UboRequestContext requestContext = new UboRequestContext();
        requestContext.setMyminfinContext(getAuthenticatedContext());

        return uboResource.getDocumentContent(documentId, requestContext);
    }

    private boolean isDocumentIncluded(MyminfinDocument doc, MyminfinDocumentFilter documentFilter) {
        LocalDate documentDate = doc.getDocumentDate();
        Integer documentYear = doc.getDocumentYear();

        Boolean fromDatePredicate = documentFilter.getFromDateOptional()
                .map(from -> from.isAfter(documentDate))
                .orElse(true);
        Boolean toDatePredicate = documentFilter.getToDateOptional()
                .map(to -> !to.isAfter(documentDate))
                .orElse(true);
        Boolean documentYearPredicate = documentFilter.getDocumentYearOptional()
                .map(year -> year.equals(documentYear))
                .orElse(true);

        return fromDatePredicate && toDatePredicate && documentYearPredicate;
    }


    private MyminfinMandateType getMandateType(MyminfinUserType customerType) {
        switch (customerType) {
            case CITIZEN:
                return MyminfinMandateType.CITIZEN;
            case PRO:
                return MyminfinMandateType.ENTERPRISE;
            case GUEST:
            default:
                throw new MyminfinApiClientError("No mandate for customer type " + customerType);
        }
    }


    private void resetMyminfinState(AuthenticatedMyminfinContext myminfinContext) {
        this.cestzamApiClient.updateState(myminfinContext.getCestzamContext(), true);
        MyminfinUser loggedUserData = userResource.getLoggedUserData(myminfinContext);

        myminfinState.setUser(loggedUserData);
        myminfinState.setAvailableCitizenMandates(null);
        myminfinState.setAvailableEnterpriseMandates(null);
    }


    private List<MyMinfinMandate> loadMandates(MyminfinMandateType mandateType) {
        AuthenticatedMyminfinContext authenticatedContext = getAuthenticatedContext();
        MyminfinMandateSearch mandateSearch = new MyminfinMandateSearch(authenticatedContext, mandateType);
        ResultPage<MyMinfinMandate> mandates = mandateResource.listMandates(mandateSearch);
        return mandates.getPageItems();
    }

    private AuthenticatedMyminfinContext getAuthenticatedContext() {
        String stateContext = cestzamApiClient.getStateContext();
        AuthenticatedMyminfinContext myminfinContext = new AuthenticatedMyminfinContext();
        myminfinContext.setCestzamContext(stateContext);
        return myminfinContext;
    }
}
