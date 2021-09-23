package be.valuya.cestzam.client;

public interface CestzamClientConfig {

    Long getClientTimeoutSeconds();

    String getClientUserAgent();

    Boolean getManagedExecutorEnabled();

    Boolean getClientDebugEnabled();

    Boolean getClientCookiesDebugEnabled();

    java.util.Optional<String> getClientDebugOutputPath();

}
