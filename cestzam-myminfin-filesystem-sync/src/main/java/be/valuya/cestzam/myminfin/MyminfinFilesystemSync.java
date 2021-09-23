package be.valuya.cestzam.myminfin;

import be.valuya.cestzam.myminfin.sync.MyminfinSyncException;
import be.valuya.cestzam.myminfin.sync.MyminfinSynchronizer;
import be.valuya.cestzam.myminfin.sync.config.MyMinfinSynchronizerConfig;
import be.valuya.cestzam.myminfin.sync.domain.SyncResult;

import javax.json.bind.JsonbBuilder;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class MyminfinFilesystemSync {

    public static final String CESTZAM_TARGET_PATH_ENV_VAR = "CESTZAM_TARGET_PATH";
    public static final String CESTZAM_API_URI_ENV_VAR = "CESTZAM_API_URI";
    public static final String CESTZAM_AUTH_LOGIN_ENV_VAR = "CESTZAM_AUTH_LOGIN";
    public static final String CESTZAM_AUTH_PASSWORD_ENV_VAR = "CESTZAM_AUTH_PASSWORD";
    public static final String CESTZAM_AUTH_TOKENS_JSON_ENV_VAR = "CESTZAM_AUTH_TOKENS_JSON";

    public static final String CESTZAM_MANDATOR_SINGLE_SSIN_ENV_VAR = "CESTZAM_MANDATOR_SINGLE_SSIN";
    public static final String CESTZAM_MANDATOR_NAME_PATTERN_ENV_VAR = "CESTZAM_MANDATOR_NAME_PATTERN";
    public static final String CESTZAM_DOCUMENT_PROVIDER_NAME_PATTERN_ENV_VAR = "CESTZAM_DOCUMENT_PROVIDER_NAME_PATTERN";
    public static final String CESTZAM_DOCUMENT_FROM_DATE_ENV_VAR = "CESTZAM_DOCUMENT_FROM_DATE";

    public static void main(String[] args) {
        MyMinfinSynchronizerConfig config = createSyncConfig();

        MyminfinSynchronizer synchronizer = new MyminfinSynchronizer(config);
        try {
            SyncResult syncResult = synchronizer.synchronize();
            long minutesDuration = syncResult.getStartTime().until(syncResult.getEndTime(), ChronoUnit.MINUTES);
            System.out.println(MessageFormat.format(
                    "Synchronization completed. {0} mandates processed, {1} files written, {2} bytes written, took {3} minutes",
                    syncResult.getMandatesProcessed(), syncResult.getDocumentWritten(), syncResult.getDocumentBytesWritten(),
                    minutesDuration
            ));
        } catch (Exception e) {
            System.err.println("Unable to synchronize");
            e.printStackTrace();
        }
    }

    private static MyMinfinSynchronizerConfig createSyncConfig() {
        MyMinfinSynchronizerConfig config = new MyMinfinSynchronizerConfig();

        String targetPathParam = getEnvVar(CESTZAM_TARGET_PATH_ENV_VAR);

        Path path = Paths.get(targetPathParam);
        if (!Files.exists(path)) {
            throw new MyminfinSyncException("Target path does not exist: " + targetPathParam);
        }
        config.setTargetPath(path);

        String apiUriParam = getEnvVar(CESTZAM_API_URI_ENV_VAR);

        URI apiUri = URI.create(apiUriParam);
        config.setCestzamApiUri(apiUri);


        String authLoginParam = getEnvVar(CESTZAM_AUTH_LOGIN_ENV_VAR);
        String authpasswordParam = getEnvVar(CESTZAM_AUTH_PASSWORD_ENV_VAR);
        String authTokenJsonParam = getEnvVar(CESTZAM_AUTH_TOKENS_JSON_ENV_VAR);

        Type hashMapType = new HashMap<String, String>() {
        }.getClass().getGenericSuperclass();
        Map<String, String> tokenMap = JsonbBuilder.create().fromJson(authTokenJsonParam, hashMapType);

        config.setUser(authLoginParam);
        config.setPassword(authpasswordParam);
        config.setAuthTokens(tokenMap);

        getOptionalEnvVar(CESTZAM_MANDATOR_NAME_PATTERN_ENV_VAR)
                .map(Pattern::compile)
                .ifPresent(config::setMandatorNamePattern);

        getOptionalEnvVar(CESTZAM_MANDATOR_SINGLE_SSIN_ENV_VAR)
                .ifPresent(config::setSingleMandatorNationalNumber);

        getOptionalEnvVar(CESTZAM_DOCUMENT_PROVIDER_NAME_PATTERN_ENV_VAR)
                .map(Pattern::compile)
                .ifPresent(config::setDocumentProviderNamePattern);

        getOptionalEnvVar(CESTZAM_DOCUMENT_FROM_DATE_ENV_VAR)
                .map(s -> LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE))
                .ifPresent(config::setDocumentFromDate);
        return config;
    }

    private static String getEnvVar(String cestzamTargetPathEnvVar) {
        String targetPathParam = System.getenv().get(cestzamTargetPathEnvVar);
        if (targetPathParam == null) {
            throw new MyminfinSyncException("Env var not defined: " + cestzamTargetPathEnvVar);
        }
        return targetPathParam;
    }


    private static Optional<String> getOptionalEnvVar(String varName) {
        String var = System.getenv().get(varName);
        return Optional.ofNullable(var);
    }
}
