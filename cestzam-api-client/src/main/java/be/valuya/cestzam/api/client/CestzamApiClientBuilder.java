package be.valuya.cestzam.api.client;

import be.valuya.cestzam.api.client.configuration.CestzamApiClientConfigutation;

import java.net.URI;

public class CestzamApiClientBuilder {

    private CestzamApiClientConfigutation apiClientConfigutation = new CestzamApiClientConfigutation();

    private CestzamApiClientBuilder() {
    }

    public static CestzamApiClientBuilder create() {
        return new CestzamApiClientBuilder();
    }

    public CestzamApiClientBuilder apiUri(URI apiURI) {
        apiClientConfigutation.setApiUri(apiURI);
        return this;
    }

    public CestzamApiClientBuilder state(String state) {
        apiClientConfigutation.setState(state);
        return this;
    }

    /**
     * Sets the timeout of the proxy requests
     *
     * @param timeoutSecond
     * @return
     */
    public CestzamApiClientBuilder clientTimeout(Long timeoutSecond) {
        apiClientConfigutation.setClientTimeout(timeoutSecond);
        return this;
    }


    public CestzamApiClient build() {
        CestzamApiClient apiClient = new CestzamApiClient(this.apiClientConfigutation);
        return apiClient;
    }

}
