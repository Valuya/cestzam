package be.valuya.cestzam;

import be.valuya.cestzam.client.czam.CestzamAuthenticatedSamlResponse;
import be.valuya.cestzam.client.czam.CestzamLoginContext;
import be.valuya.cestzam.client.czam.CzamCapacity;
import be.valuya.cestzam.client.czam.CzamLoginClientService;
import be.valuya.cestzam.client.error.CestzamClientError;
import be.valuya.cestzam.client.myminfin.CestzamAuthenticatedMyminfinContext;
import be.valuya.cestzam.client.myminfin.MyminfinClientService;
import be.valuya.cestzam.client.myminfin.rest.MyminfinUboRestClientService;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompaniesSearchRequest;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompaniesSearchResults;
import be.valuya.cestzam.client.myminfin.rest.ubo.Company;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompanyControl;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompositionTreeNode;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompositionTreeRequest;
import be.valuya.cestzam.client.myminfin.rest.ubo.ControlRequest;
import be.valuya.cestzam.client.myminfin.rest.ubo.GetCompanyRequest;
import be.valuya.cestzam.client.myminfin.rest.ubo.UserInfo;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@ExtendWith(CdiExtension.class) // Runs the test with CDI-Unit
public class CestzamMinminfinUboTokenTest {

    @Inject
    private MyminfinClientService myminfinClientService;
    @Inject
    private MyminfinUboRestClientService myminfinUboRestClientService;
    @Inject
    private CzamLoginClientService czamLoginClientService;
    @Inject
    private TestClientConfig clientConfig;


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

            UserInfo userInfo = myminfinUboRestClientService.getUserInfo(enterpriseMyminfinContext.getCookies());
            Assertions.assertNotNull(userInfo);

            debugJson(userInfo);

            CompaniesSearchRequest searchRequest = new CompaniesSearchRequest();
            searchRequest.setLimit(5);
            CompaniesSearchResults companiesSearchResults = myminfinUboRestClientService.getCompaniesSearchResults(enterpriseMyminfinContext.getCookies(), searchRequest);
            Assertions.assertNotNull(companiesSearchResults);

            debugJson(companiesSearchResults);

            Company companyResult = companiesSearchResults.getContent().get(0);
            String companyId = companyResult.getId();

            GetCompanyRequest getCompanyRequest = new GetCompanyRequest();
            getCompanyRequest.setAdmin(true);
            Company company = myminfinUboRestClientService.getCompany(enterpriseMyminfinContext.getCookies(), companyId, getCompanyRequest);
            Assertions.assertNotNull(company);

            debugJson(company);

            CompositionTreeRequest compositionTreeRequest = new CompositionTreeRequest();
            compositionTreeRequest.setLanguage("fr");
            CompositionTreeNode companyCompositionTree = myminfinUboRestClientService.getCompanyCompositionTree(enterpriseMyminfinContext.getCookies(), companyId, compositionTreeRequest);
            Assertions.assertNotNull(companyCompositionTree);

            debugJson(companyCompositionTree);

            ControlRequest controlRequest = new ControlRequest();
            List<CompanyControl> companyControls = myminfinUboRestClientService.getCompanyControls(enterpriseMyminfinContext.getCookies(), companyId, controlRequest);
            Assertions.assertNotNull(companyControls);

            debugJson(companyControls);

            Company updatedCompany = myminfinUboRestClientService.confirmCompany(enterpriseMyminfinContext.getCookies(), companyId);
            Assertions.assertNotNull(updatedCompany);

            debugJson(updatedCompany);

            Assertions.assertEquals(LocalDate.now(), updatedCompany.getConfirmationDate());
        }
    }


    private void debugJson(Object data) {
        Jsonb jsonb = JsonbBuilder.create();
        String json = jsonb.toJson(data);
        System.out.println(json);
    }

}
