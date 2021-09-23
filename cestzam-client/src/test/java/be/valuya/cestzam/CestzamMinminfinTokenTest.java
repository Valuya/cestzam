package be.valuya.cestzam;

import be.valuya.cestzam.client.CestzamClientService;
import be.valuya.cestzam.client.czam.CzamCapacity;
import be.valuya.cestzam.client.czam.CzamLoginClientService;
import be.valuya.cestzam.client.czam.CestzamAuthenticatedSamlResponse;
import be.valuya.cestzam.client.czam.CestzamLoginContext;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.MyminfinClientService;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;
import be.valuya.cestzam.client.myminfin.rest.MyminfinCustomerRestClientService;
import be.valuya.cestzam.client.myminfin.rest.MyminfinDocumentsRestClientService;
import be.valuya.cestzam.client.myminfin.rest.MyminfinRestClientService;
import be.valuya.cestzam.client.myminfin.rest.UserData;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentDescription;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentKey;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentProviders;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentProvidersResponse;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentStream;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentsListResponse;
import be.valuya.cestzam.client.myminfin.rest.mandate.ApplicationMandate;
import be.valuya.cestzam.client.myminfin.rest.mandate.MandateApplication;
import be.valuya.cestzam.mock.CdiExtension;
import be.valuya.cestzam.mock.TestClientConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ExtendWith(CdiExtension.class) // Runs the test with CDI-Unit
public class CestzamMinminfinTokenTest {

    @Inject
    private MyminfinClientService myminfinClientService;
    @Inject
    private MyminfinRestClientService myminfinRestClientService;
    @Inject
    private MyminfinDocumentsRestClientService myminfinDocumentsRestClientService;
    @Inject
    private MyminfinCustomerRestClientService myminfinCustomerRestClientService;
    @Inject
    private CzamLoginClientService czamLoginClientService;
    @Inject
    private TestClientConfig clientConfig;
    @Inject
    private CestzamClientService clientService;


    @Test
    @Tag(value = TestTags.MANUAL_TESTS)
    public void testCitizenMandate() throws CestzamClientError, IOException {
        Map<Integer, String> tokenCodes = clientConfig.getTokenCodes();
        String tokenLogin = clientConfig.getTokenLogin();
        String tokenPassword = clientConfig.getTokenPassword();
        String myminfinRequestedVat = clientConfig.getMyminfinRequestedVat();


        // Login as citizen, list mandates
        {
            CestzamLoginContext loginContext = myminfinClientService.startLoginFlow();
            Assertions.assertNotNull(loginContext);

            CestzamAuthenticatedSamlResponse samlResponse = czamLoginClientService.doTokenLogin(loginContext, CzamCapacity.CITIZEN, tokenLogin, tokenPassword, tokenCodes);
            Assertions.assertNotNull(samlResponse);

            CestzamAuthenticatedMyminfinContext myminfinContext = myminfinClientService.completeLoginFlow(samlResponse);
            Assertions.assertNotNull(myminfinContext);

            UserData userData = myminfinRestClientService.getUserData(myminfinContext.getCookies());
            Assertions.assertNotNull(userData);

            debugJson(userData);

            DocumentProvidersResponse providers = myminfinDocumentsRestClientService.getProviders(myminfinContext);
            debugJson(providers.getProviders());

            List<ApplicationMandate> citizenMandates = myminfinCustomerRestClientService.getCitizenMandates(myminfinContext);
            debugJson(citizenMandates);

            List<ApplicationMandate> enterpriseMandates = myminfinCustomerRestClientService.getEnterpriseMandates(myminfinContext);
            debugJson(enterpriseMandates);
        }


        // Login as enterprise, list mandates
        CestzamAuthenticatedMyminfinContext enterpriseMyminfinContext;
        {
            CestzamLoginContext loginContext = myminfinClientService.startLoginFlow();
            Assertions.assertNotNull(loginContext);

            CestzamAuthenticatedSamlResponse samlResponse = czamLoginClientService.doTokenLogin(loginContext, CzamCapacity.ENTERPRISE, tokenLogin, tokenPassword, tokenCodes);
            Assertions.assertNotNull(samlResponse);

            enterpriseMyminfinContext = myminfinClientService.completeLoginFlow(myminfinRequestedVat, samlResponse);
            Assertions.assertNotNull(enterpriseMyminfinContext);

            UserData userData = myminfinRestClientService.getUserData(enterpriseMyminfinContext.getCookies());
            Assertions.assertNotNull(userData);

            debugJson(userData);

            DocumentProvidersResponse providers = myminfinDocumentsRestClientService.getProviders(enterpriseMyminfinContext);
            debugJson(providers.getProviders());

            // Status code 202 with error Unauthorized when listing mandates as citizen
            List<ApplicationMandate> citizenMandates = myminfinCustomerRestClientService.getCitizenMandates(enterpriseMyminfinContext);
            debugJson(citizenMandates);

            List<ApplicationMandate> enterpriseMandates = myminfinCustomerRestClientService.getEnterpriseMandates(enterpriseMyminfinContext);
            debugJson(enterpriseMandates);
        }

        // Get a document as the logged enterprise
        {
            DocumentsListResponse becotaxList = myminfinDocumentsRestClientService.listProviderDocuments(enterpriseMyminfinContext, DocumentProviders.FILENET_OSSTIRP);
            debugJson(becotaxList);

            DocumentDescription doc1 = becotaxList.getDocumentDescriptionList().stream()
                    .findAny()
                    .orElseThrow();
            DocumentKey doc1Key = doc1.getDocumentKey();
            DocumentStream documentStream = myminfinDocumentsRestClientService.downloadDocument(enterpriseMyminfinContext, doc1Key);
            System.out.println(documentStream.getFileName());

            Path tempFile = Files.createTempFile("", documentStream.getFileName());
            OutputStream outputStream = Files.newOutputStream(tempFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            documentStream.getInputStream().transferTo(outputStream);
            System.out.println(tempFile.toAbsolutePath().toString());
        }


        // Activate a mandate
        {
            List<ApplicationMandate> citizenMandates = myminfinCustomerRestClientService.getCitizenMandates(enterpriseMyminfinContext);
            debugJson(citizenMandates);

            List<ApplicationMandate> enterpriseMandates = myminfinCustomerRestClientService.getEnterpriseMandates(enterpriseMyminfinContext);
            debugJson(enterpriseMandates);

            ApplicationMandate mandate = citizenMandates.stream()
                    .filter(m -> m.getMandate().getMandatorNationalNumber().equalsIgnoreCase(clientConfig.getMandatorRN()))
                    .filter(m -> m.getApplicationName().equalsIgnoreCase(MandateApplication.MYMINFIN))
                    .findAny()
                    .orElseThrow(AssertionError::new);
            enterpriseMyminfinContext = myminfinCustomerRestClientService.activateMandate(enterpriseMyminfinContext, mandate);
        }

        // List documents as mandatee
        {
            DocumentsListResponse becotaxList2 = myminfinDocumentsRestClientService.listProviderDocuments(enterpriseMyminfinContext, DocumentProviders.FILENET_OSSTIRP);
            debugJson(becotaxList2);
        }
    }

    @Test
    @Tag(value = TestTags.MANUAL_TESTS)
    public void testEnterpriseMandate() throws CestzamClientError, IOException {
        Map<Integer, String> tokenCodes = clientConfig.getTokenCodes();
        String tokenLogin = clientConfig.getTokenLogin();
        String tokenPassword = clientConfig.getTokenPassword();
        String myminfinRequestedVat = clientConfig.getMyminfinRequestedVat();

        // Login as enterprise, list mandates
        CestzamAuthenticatedMyminfinContext enterpriseMyminfinContext;
        {
            CestzamLoginContext loginContext = myminfinClientService.startLoginFlow();
            Assertions.assertNotNull(loginContext);

            CestzamAuthenticatedSamlResponse samlResponse = czamLoginClientService.doTokenLogin(loginContext, CzamCapacity.ENTERPRISE, tokenLogin, tokenPassword, tokenCodes);
            Assertions.assertNotNull(samlResponse);

            enterpriseMyminfinContext = myminfinClientService.completeLoginFlow(myminfinRequestedVat, samlResponse);
            Assertions.assertNotNull(enterpriseMyminfinContext);

            UserData userData = myminfinRestClientService.getUserData(enterpriseMyminfinContext.getCookies());
            Assertions.assertNotNull(userData);

            debugJson(userData);

            DocumentProvidersResponse providers = myminfinDocumentsRestClientService.getProviders(enterpriseMyminfinContext);
            debugJson(providers.getProviders());

            // Status code 202 with error Unauthorized when listing mandates as citizen
            List<ApplicationMandate> citizenMandates = myminfinCustomerRestClientService.getCitizenMandates(enterpriseMyminfinContext);
            debugJson(citizenMandates);

            List<ApplicationMandate> enterpriseMandates = myminfinCustomerRestClientService.getEnterpriseMandates(enterpriseMyminfinContext);
            debugJson(enterpriseMandates);
        }

        // List mandate for a vat number
        List<ApplicationMandate> vatMandatesList;
        {
            List<ApplicationMandate> enterpriseMandates = myminfinCustomerRestClientService.getEnterpriseMandates(enterpriseMyminfinContext);
            debugJson(enterpriseMandates);

            vatMandatesList = enterpriseMandates.stream()
                    .filter(m -> m.getMandate().getMandatorIdentifier().equalsIgnoreCase(clientConfig.getMandatorVat()))
                    .collect(Collectors.toList());
            Assertions.assertFalse(vatMandatesList.isEmpty());
        }

        // Collect all providers and document for every mandate
        Map<ApplicationMandate, DocumentProvidersResponse> providersPerMandate = new HashMap<>();
        Map<ApplicationMandate, Map<String, DocumentsListResponse>> documentsPerProviderPerMandate = new HashMap<>();
        for (ApplicationMandate mandate : vatMandatesList) {
            enterpriseMyminfinContext = myminfinCustomerRestClientService.activateMandate(enterpriseMyminfinContext, mandate);
            DocumentProvidersResponse providersResponse = myminfinDocumentsRestClientService.getProviders(enterpriseMyminfinContext);
            providersPerMandate.put(mandate, providersResponse);

            Map<String, DocumentsListResponse> documentsPerProvider = new HashMap<>();
            for (String provider : providersResponse.getProviders()) {
                DocumentsListResponse providerDocumentList = myminfinDocumentsRestClientService.listProviderDocuments(enterpriseMyminfinContext, provider);
                documentsPerProvider.put(provider, providerDocumentList);
            }
            documentsPerProviderPerMandate.put(mandate, documentsPerProvider);
        }

        // Either mandate should return the same providers and documents.
        // This means that if this test pass, the various mandate applications can be ignored - they all grant access
        // to the same set of documents
        {
            // Test useless if a single mandate exists for vat number
            Assertions.assertTrue(vatMandatesList.size() > 1, "Needs more than 1 mandate for a vat number for this test");

            Set<String> allUniqueProvidersSet = providersPerMandate.values().stream()
                    .map(DocumentProvidersResponse::getProviders)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());
            providersPerMandate.values().stream()
                    .map(DocumentProvidersResponse::getProviders)
                    .forEach(providers -> {
                        HashSet<String> thisProvidersSet = new HashSet<>(providers);
                        Assertions.assertEquals(thisProvidersSet, allUniqueProvidersSet);
                    });

            for (String provider : allUniqueProvidersSet) {
                Set<String> allUniqueProviderDocuemnts = documentsPerProviderPerMandate.values().stream()
                        .map(m -> m.get(provider))
                        .map(DocumentsListResponse::getDocumentDescriptionList)
                        .flatMap(List::stream)
                        .map(DocumentDescription::getDocumentKey)
                        .map(this::toUniqueIdString)
                        .collect(Collectors.toSet());

                documentsPerProviderPerMandate.values().stream()
                        .map(m -> m.get(provider))
                        .map(DocumentsListResponse::getDocumentDescriptionList)
                        .forEach(docList -> {
                            Set<String> providerDocSet = docList.stream()
                                    .map(DocumentDescription::getDocumentKey)
                                    .map(this::toUniqueIdString)
                                    .collect(Collectors.toSet());
                            Assertions.assertEquals(allUniqueProviderDocuemnts, providerDocSet);
                        });
            }
        }
    }


    private void debugJson(Object data) {
        Jsonb jsonb = JsonbBuilder.create();
        String json = jsonb.toJson(data);
        System.out.println(json);
    }

    private final static String UNIQUE_ID_STRING_FORMAT = "{0}.{1}.{2}";
    private final static String IDENTIFIERS_SEPARATOR = ";";

    public String toUniqueIdString(DocumentKey documentKey) {
        Map<String, String> identifiers = documentKey.getIdentifiers();
        String documentSelectorTypeKey = documentKey.getDocumentSelectorTypeKey();
        String providerName = documentKey.getProviderName();
        String identifiersString = identifiers.entrySet().stream()
                .map(e -> escapeAll(e.getKey(), ".:;") + ":" + escapeAll(e.getValue(), ".:;"))
                .collect(Collectors.joining(IDENTIFIERS_SEPARATOR));
        String idString = MessageFormat.format(UNIQUE_ID_STRING_FORMAT,
                escapeAll(providerName, "."), escapeAll(documentSelectorTypeKey, "."), identifiersString);
        return idString;
    }

    private static String escapeAll(String value, String escapedChar) {
        String[] replacement = new String[]{value};
        for (char c : escapedChar.toCharArray()) {
            replacement[0] = replacement[0].replace("" + c, "\\" + c);
        }
        return replacement[0];
    }
}
