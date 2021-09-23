package be.valuya.cestzam.mock;

import be.valuya.cestzam.client.CestzamClientConfig;
import lombok.Getter;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public class TestClientConfig implements CestzamClientConfig {

    private final Long clientTimeoutSeconds;
    private final String clientUserAgent;
    private final Boolean managedExecutorEnabled;
    private final Boolean clientDebugEnabled;
    private final Boolean clientCookiesDebugEnabled;
    private final Optional<String> clientDebugOutputPath;
    private final Config config;
    private final String mandatorRN;
    private final String mandatorVat;
    @Inject
    private Instance<Executor> cestzamClientExecutorInstance;

    private String tokenLogin;
    private String tokenPassword;
    private String myminfinRequestedVat;
    private Map<Integer, String> tokenCodes;

    public TestClientConfig() {
        config = ConfigProvider.getConfig();
        clientTimeoutSeconds = 60L;
        clientUserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36";
        managedExecutorEnabled = false;
        clientDebugEnabled = true;
        clientCookiesDebugEnabled = false;
        clientDebugOutputPath = Optional.empty();

        tokenLogin = System.getProperty("cestzam.token.login");
        tokenPassword = System.getProperty("cestzam.token.password");
        myminfinRequestedVat = System.getProperty("cestzam.myminfin.vatNumber");
        mandatorRN = System.getProperty("cestzam.myminfin.mandate.mandatorNationalNumber");
        mandatorVat = System.getProperty("cestzam.myminfin.mandate.mandatorVatNumber");
        String codeJson = System.getProperty("cestzam.token.codesJson");

        JsonReader reader = Json.createReader(new StringReader(codeJson));
        JsonObject tokenJson = reader.readObject();
        tokenCodes = IntStream.range(1, 51)
                .mapToObj(Integer::toString)
                .filter(tokenJson::containsKey)
                .collect(Collectors.toMap(
                        Integer::parseInt,
                        tokenJson::getString
                ));
    }

}
