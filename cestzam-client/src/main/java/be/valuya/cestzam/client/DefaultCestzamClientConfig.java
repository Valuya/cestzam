package be.valuya.cestzam.client;

import be.valuya.cestzam.api.Cestzam;
import lombok.Getter;
import org.eclipse.microprofile.config.Config;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.Executor;

@Getter
public class DefaultCestzamClientConfig implements CestzamClientConfig {

    @Inject
    private Config config;
    @Inject
    @Cestzam
    protected Instance<Executor> cestzamClientExecutorInstance;


    public Long getClientTimeoutSeconds() {
        return config.getOptionalValue("be.valuya.cestzam.client.timeout", Long.class)
                .orElse(60L);
    }

    public String getClientUserAgent() {
        return config.getOptionalValue("be.valuya.cestzam.client.userAgent", String.class)
                .orElse("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36");
    }

    public Boolean getManagedExecutorEnabled() {
        return config.getOptionalValue("be.valuya.cestzam.client.executor.managed.enabled", Boolean.class)
                .orElse(true);
    }

    public Boolean getClientDebugEnabled() {
        return config.getOptionalValue("be.valuya.cestzam.client.debug.enabled", Boolean.class)
                .orElse(false);
    }

    public Boolean getClientCookiesDebugEnabled() {
        return config.getOptionalValue("be.valuya.cestzam.client.debug.cookies", Boolean.class)
                .orElse(false);
    }

    public Optional<String> getClientDebugOutputPath() {
        return config.getOptionalValue("be.valuya.cestzam.client.debug.outputPath", String.class);
    }

    public Instance<Executor> getCestzamClientExecutorInstance() {
        return cestzamClientExecutorInstance;
    }
}
