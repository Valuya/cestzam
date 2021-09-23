package be.valuya.cestzam.myminfin.sync.config;

import be.valuya.cestzam.myminfin.sync.MyminfinSyncException;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Map;

public class MyMinfinSynchronizerConfigValidator {

    public static void validateConfig(MyMinfinSynchronizerConfig config) {
        Path targetPath = config.getTargetPath();
        validateTargetPath(targetPath);

        URI cestzamApiUri = config.getCestzamApiUri();
        validateCestzamApiUri(cestzamApiUri);

        String password = config.getPassword();
        String user = config.getUser();
        Map<String, String> authTokens = config.getAuthTokens();
        validateAuth(user, password, authTokens);
    }

    private static void validateAuth(String user, String password, Map<String, String> authTokens) {
        if (user == null || user.isBlank()) {
            throw new MyminfinSyncException("Invalid config: no user provided");
        }
        if (password == null || password.isBlank()) {
            throw new MyminfinSyncException("Invalid config: no password provided");
        }
        if (authTokens == null || authTokens.isEmpty()) {
            throw new MyminfinSyncException("Invalid config: no authentication token list provider");
        }
    }

    private static void validateCestzamApiUri(URI cestzamApiUri) {
        if (cestzamApiUri == null) {
            throw new MyminfinSyncException("Invalid config: no api uri");
        }
    }

    private static void validateTargetPath(Path targetPath) {
        if (targetPath == null) {
            throw new MyminfinSyncException("Invalid config: no target path");
        }

        boolean targetExists = Files.exists(targetPath);
        if (!targetExists) {
            throw new MyminfinSyncException(MessageFormat.format(
                    "Invalid config: target path {0} does not exists",
                    targetPath
            ));
        }

        boolean targetDirectory = Files.isDirectory(targetPath);
        if (!targetDirectory) {
            throw new MyminfinSyncException(MessageFormat.format(
                    "Invalid config: target path {0} is not a directory",
                    targetPath
            ));
        }

        boolean targetWritable = Files.isWritable(targetPath);
        if (!targetWritable) {
            throw new MyminfinSyncException(MessageFormat.format(
                    "Invalid config: target path {0} is not writable",
                    targetPath
            ));
        }
    }
}
