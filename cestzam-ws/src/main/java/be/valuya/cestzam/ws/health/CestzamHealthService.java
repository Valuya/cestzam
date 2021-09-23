package be.valuya.cestzam.ws.health;

import be.valuya.cestzam.api.service.ServiceHealthCheck;
import be.valuya.cestzam.api.service.ServiceHealthCheckResponse;
import be.valuya.cestzam.client.czam.CzamCapacity;
import be.valuya.cestzam.client.czam.CzamCitizenInfo;
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
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentProviderGroup;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentProvidersResponse;
import be.valuya.cestzam.client.myminfin.rest.mandate.ApplicationMandate;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class CestzamHealthService {

    public static final String MYMINFIN_SERVICE_NAME = "myminfin";

    @Inject
    @ConfigProperty(name = "cestzam.health.enabled", defaultValue = "false")
    private Boolean healthChecksEnabled;

    @Inject
    @ConfigProperty(name = "cestzam.health.myminfin.enabled", defaultValue = "true")
    private Boolean myminfinHealthChecksEnabled;

    @Inject
    @ConfigProperty(name = "cestzam.health.czam.tokensJson")
    private Optional<String> czamTokensJson;

    @Inject
    @ConfigProperty(name = "cestzam.health.czam.user")
    private Optional<String> czamUser;

    @Inject
    @ConfigProperty(name = "cestzam.health.czam.password")
    private Optional<String> czamPassword;

    @Inject
    @ConfigProperty(name = "cestzam.health.myminfin.vatNumber")
    private Optional<String> myminfinVatNumber;

    @Inject
    private MyminfinClientService myminfinClientService;
    @Inject
    private CzamLoginClientService czamLoginClientService;
    @Inject
    private MyminfinDocumentsRestClientService myminfinDocumentsRestClientService;
    @Inject
    private MyminfinCustomerRestClientService myminfinCustomerRestClientService;
    @Inject
    private MyminfinRestClientService myminfinRestClientService;

    public ServiceHealthCheckResponse checkMyminfinHealth() {
        if (!this.healthChecksEnabled) {
            return createHealthDisabledResponse(MYMINFIN_SERVICE_NAME);
        }
        if (!this.myminfinHealthChecksEnabled) {
            return createHealthDisabledResponse(MYMINFIN_SERVICE_NAME);
        }

        if (czamTokensJson.isEmpty()) {
            return createHealthErrorResponse(MYMINFIN_SERVICE_NAME, "No tokens provided for health checks");
        }
        if (czamUser.isEmpty()) {
            return createHealthErrorResponse(MYMINFIN_SERVICE_NAME, "No user provided for health checks");
        }
        if (czamPassword.isEmpty()) {
            return createHealthErrorResponse(MYMINFIN_SERVICE_NAME, "No password provided for health checks");
        }

        try {
            String login = czamUser.get();
            String password = czamPassword.get();
            Map<Integer, String> tokens = getTokensMapFromJson(czamTokensJson.get());

            CestzamLoginContext loginContext = myminfinClientService.startLoginFlow();
            CestzamAuthenticatedSamlResponse authenticatedSamlResponse = czamLoginClientService.doTokenLogin(loginContext, CzamCapacity.ENTERPRISE, login, password, tokens);
            CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext;
            if (myminfinVatNumber.filter(s -> !s.strip().isBlank()).isEmpty()) {
                authenticatedMyminfinContext = myminfinClientService.completeLoginFlow(authenticatedSamlResponse);
            } else {
                authenticatedMyminfinContext = myminfinClientService.completeLoginFlow(myminfinVatNumber.get(), authenticatedSamlResponse);
            }

            ServiceHealthCheck czamUserCheck = checkMyminfinCzamUserInfo(authenticatedMyminfinContext);
            ServiceHealthCheck myminfinUserCheck = checkMyminfinUserInfo(authenticatedMyminfinContext);
            ServiceHealthCheck myminfinProvidersCheck = checkMyminfinProviders(authenticatedMyminfinContext);
            ServiceHealthCheck citizenMandatesCheck = checkMyminfinCitizenMandates(authenticatedMyminfinContext);
            ServiceHealthCheck enterpriseMandateCheck = checkMyminfinEnterpriseMandates(authenticatedMyminfinContext);
            return createHealthCheckResponse(MYMINFIN_SERVICE_NAME, List.of(
                    czamUserCheck, myminfinUserCheck, myminfinProvidersCheck, citizenMandatesCheck, enterpriseMandateCheck
            ));
        } catch (CestzamClientError cestzamClientError) {
            return createHealthErrorResponse(MYMINFIN_SERVICE_NAME, cestzamClientError.getMessage());
        }
    }


    private ServiceHealthCheck checkMyminfinCzamUserInfo(CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext) {
        String checkName = "myminfin.czam.user";
        CzamCitizenInfo czamCitizenInfo = authenticatedMyminfinContext.getCzamCitizenInfo();
        String citizen = czamCitizenInfo.getFirstNames() + " " + czamCitizenInfo.getLastNames();
        ServiceHealthCheck userInfoCheck = new ServiceHealthCheck(checkName, true, citizen);
        return userInfoCheck;
    }


    private ServiceHealthCheck checkMyminfinUserInfo(CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext) {
        String checkName = "myminfin.user";
        try {
            UserData userData = myminfinRestClientService.getUserData(authenticatedMyminfinContext.getCookies());
            String name = userData.getName();
            String visitorType = userData.getVisitorType();
            ServiceHealthCheck providersCheck = new ServiceHealthCheck(checkName, true, visitorType + " : " + name);
            return providersCheck;
        } catch (CestzamClientError cestzamClientError) {
            return createHealthCheckError(checkName, cestzamClientError);
        }
    }

    private ServiceHealthCheck checkMyminfinProviders(CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext) {
        String checkName = "myminfin.document.providers";
        try {
            DocumentProvidersResponse providers = myminfinDocumentsRestClientService.getProviders(authenticatedMyminfinContext);
            String providerGroups = providers.getGroups().stream()
                    .map(DocumentProviderGroup::getLabel)
                    .collect(Collectors.joining(","));
            ServiceHealthCheck providersCheck = new ServiceHealthCheck(checkName, true, providerGroups);
            return providersCheck;
        } catch (CestzamClientError cestzamClientError) {
            return createHealthCheckError(checkName, cestzamClientError);
        }
    }


    private ServiceHealthCheck checkMyminfinCitizenMandates(CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext) {
        String checkName = "myminfin.citizen.mandates";
        try {
            List<ApplicationMandate> citizenMandates = myminfinCustomerRestClientService.getCitizenMandates(authenticatedMyminfinContext);
            int citizenMandateCount = citizenMandates.size();
            ServiceHealthCheck citizenMandateCheck = new ServiceHealthCheck(checkName, true, "" + citizenMandateCount);
            return citizenMandateCheck;
        } catch (CestzamClientError cestzamClientError) {
            return createHealthCheckError(checkName, cestzamClientError);
        }
    }

    private ServiceHealthCheck checkMyminfinEnterpriseMandates(CestzamAuthenticatedMyminfinContext authenticatedMyminfinContext) {
        String checkName = "myminfin.enterprise.mandates";
        try {
            List<ApplicationMandate> enterpriseMandates = myminfinCustomerRestClientService.getEnterpriseMandates(authenticatedMyminfinContext);
            int entrepriseMandateCount = enterpriseMandates.size();
            ServiceHealthCheck entrepriseMandateCheck = new ServiceHealthCheck(checkName, true, "" + entrepriseMandateCount);
            return entrepriseMandateCheck;
        } catch (CestzamClientError cestzamClientError) {
            return createHealthCheckError(checkName, cestzamClientError);
        }
    }

    private ServiceHealthCheck createHealthCheckError(String checkName, Exception error) {
        ServiceHealthCheck healthCheck = new ServiceHealthCheck(
                checkName, false, error.getMessage()
        );
        return healthCheck;
    }

    private ServiceHealthCheckResponse createHealthErrorResponse(String serviceName, String message) {
        ServiceHealthCheckResponse healthCheckResponse = new ServiceHealthCheckResponse(
                false, serviceName, message, new ArrayList<>()
        );
        return healthCheckResponse;
    }

    private ServiceHealthCheckResponse createHealthDisabledResponse(String serviceName) {
        ServiceHealthCheckResponse healthCheckResponse = new ServiceHealthCheckResponse(
                true, serviceName, "Health check disabled", new ArrayList<>()
        );
        return healthCheckResponse;
    }

    private ServiceHealthCheckResponse createHealthCheckResponse(String serviceName, List<ServiceHealthCheck> checks) {
        boolean allUp = checks.stream()
                .allMatch(ServiceHealthCheck::isUp);
        boolean empty = checks.isEmpty();
        boolean checkUp = allUp && !empty;

        ServiceHealthCheckResponse healthCheckResponse = new ServiceHealthCheckResponse(
                checkUp, serviceName, "", checks
        );
        return healthCheckResponse;
    }

    private Map<Integer, String> getTokensMapFromJson(String codeJson) {
        JsonReader reader = Json.createReader(new StringReader(codeJson));
        JsonObject tokenJson = reader.readObject();
        Map<Integer, String> tokenCodes = IntStream.range(1, 51)
                .mapToObj(Integer::toString)
                .filter(tokenJson::containsKey)
                .collect(Collectors.toMap(
                        Integer::parseInt,
                        tokenJson::getString
                ));
        return tokenCodes;
    }

}
