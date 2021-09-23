package be.valuya.cestzam.api.client;

import be.valuya.cestzam.api.client.domain.MyminfinDocumentFilter;
import be.valuya.cestzam.api.login.Capacity;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocument;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentProvider;
import be.valuya.cestzam.api.service.myminfin.mandate.MyMinfinMandate;
import be.valuya.cestzam.api.service.myminfin.mandate.MyminfinMandateType;
import be.valuya.cestzam.api.service.myminfin.user.MyminfinUser;
import be.valuya.cestzam.api.service.myminfin.user.MyminfinUserType;
import be.valuya.cestzam.api.util.ResultPage;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentProviders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ExtendWith(CdiExtension.class) // Runs the test with CDI-Unit
public class MyminfinClientTest {

    private static MyminfinApiClient myminfinClient;
    private static String tokenLogin;
    private static String tokenPassword;
    private static String mandatorRN;
    private static Map<String, String> tokenCodes;


    @BeforeAll
    public static void init() {
        String apiUriString = System.getProperty("cestzam.api.uri");
        URI uri = URI.create(apiUriString);
        myminfinClient = CestzamApiClientBuilder.create()
                .apiUri(uri)
                .build()
                .getMyminfin();

        tokenLogin = System.getProperty("cestzam.token.login");
        tokenPassword = System.getProperty("cestzam.token.password");
        mandatorRN = System.getProperty("cestzam.myminfin.mandate.mandatorNationalNumber");
        String codeJson = System.getProperty("cestzam.token.codesJson");

        JsonReader reader = Json.createReader(new StringReader(codeJson));
        JsonObject tokenJson = reader.readObject();
        tokenCodes = IntStream.range(1, 51)
                .mapToObj(Integer::toString)
                .filter(tokenJson::containsKey)
                .collect(Collectors.toMap(
                        a -> a,
                        tokenJson::getString
                ));
    }

    @Test
    @Tag(value = TestTags.MANUAL_TESTS)
    public void test() {
        {
            // Not authenticated
            Assertions.assertFalse(myminfinClient.getAuthenticatedUserOptional().isPresent());

            // Auth with token
            myminfinClient.authenticateWithToken(tokenLogin, tokenPassword, tokenCodes, Capacity.ENTERPRISE);
            // Auththenticated
            Assertions.assertTrue(myminfinClient.getAuthenticatedUserOptional().isPresent());
        }

        {
            // Logged as ENTERPRISE, viewing enterprise content
            MyminfinUser authenticatedUser = myminfinClient.getAuthenticatedUser();
            Assertions.assertEquals(MyminfinUserType.PRO, authenticatedUser.getVisitorType());
            Assertions.assertEquals(MyminfinUserType.PRO, authenticatedUser.getCustomerType());
            Optional<MyMinfinMandate> activeMandateOptional = myminfinClient.getActiveMandateOptional();
            // No active mandate
            Assertions.assertFalse(activeMandateOptional.isPresent());
        }

        {
            // List documents, providers, download works
            ResultPage<MyminfinDocumentProvider> providers = myminfinClient.listDocumentsProviders();
            Assertions.assertTrue(providers.getTotalCount() > 0);

            MyminfinDocumentFilter documentFilter = new MyminfinDocumentFilter();
            documentFilter.setProvider(DocumentProviders.FILENET_OSSTIRP);
            ResultPage<MyminfinDocument> documentResultPage = myminfinClient.searchDocuments(documentFilter);
            Assertions.assertTrue(documentResultPage.getTotalCount() > 0);
        }

        {
            // Activate a citizen mandate
            List<MyMinfinMandate> availableMandates = myminfinClient.getAvailableMandates(MyminfinMandateType.CITIZEN);
            Assertions.assertTrue(availableMandates.size() > 0);

            MyMinfinMandate minfinMandate = availableMandates.stream()
                    .filter(m -> m.getMandatorNationalNumber().equalsIgnoreCase(mandatorRN))
                    .findAny()
                    .orElseThrow(AssertionError::new);
            myminfinClient.setActiveMandate(minfinMandate);

            // Logged as ENTERPRISE, viewing citizen content
            MyminfinUser authenticatedUser = myminfinClient.getAuthenticatedUser();
            Assertions.assertEquals(MyminfinUserType.PRO, authenticatedUser.getVisitorType());
            Assertions.assertEquals(MyminfinUserType.CITIZEN, authenticatedUser.getCustomerType());
            Optional<MyMinfinMandate> activeMandateOptional = myminfinClient.getActiveMandateOptional();
            // Citizen mandate
            MyMinfinMandate activeMandate = activeMandateOptional.orElseThrow(AssertionError::new);
            Assertions.assertTrue(activeMandate.getMandatorIdentifier().equalsIgnoreCase(mandatorRN));
        }


        {
            // List documents, providers, download works
            ResultPage<MyminfinDocumentProvider> providers = myminfinClient.listDocumentsProviders();
            Assertions.assertTrue(providers.getTotalCount() > 0);

            MyminfinDocumentFilter documentFilter = new MyminfinDocumentFilter();
            documentFilter.setProvider(DocumentProviders.FILENET_OSSTIRP);
            ResultPage<MyminfinDocument> documentResultPage = myminfinClient.searchDocuments(documentFilter);
            Assertions.assertTrue(documentResultPage.getTotalCount() > 0);
        }


        {
            // Deactivate mandate
            myminfinClient.unsetActiveMandate();

            // Logged as ENTERPRISE, viewing enterprise content
            MyminfinUser authenticatedUser = myminfinClient.getAuthenticatedUser();
            Assertions.assertEquals(MyminfinUserType.PRO, authenticatedUser.getVisitorType());
            Assertions.assertEquals(MyminfinUserType.PRO, authenticatedUser.getCustomerType());
            Optional<MyMinfinMandate> activeMandateOptional = myminfinClient.getActiveMandateOptional();
            // No active mandate
            Assertions.assertFalse(activeMandateOptional.isPresent());
        }

    }
}
