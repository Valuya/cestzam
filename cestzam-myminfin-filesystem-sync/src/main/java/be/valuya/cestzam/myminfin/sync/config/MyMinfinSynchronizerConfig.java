package be.valuya.cestzam.myminfin.sync.config;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
@Setter
public class MyMinfinSynchronizerConfig {

    private Path targetPath;

    private URI cestzamApiUri;
    private Map<String, String> authTokens;
    private String user;
    private String password;

    // Filters on customers (mandators)
    private String singleMandatorNationalNumber;
    private Pattern mandatorNamePattern;

    // Filters on docuemnt providers
    private Pattern documentProviderNamePattern;

    // Filters on docuemnts
    private LocalDate documentFromDate;

}
