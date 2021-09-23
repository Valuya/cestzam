package be.valuya.cestzam.api.client;

import be.valuya.cestzam.api.client.configuration.QueryParamRequestFilter;
import be.valuya.cestzam.api.client.configuration.CestzamApiClientConfigutation;
import be.valuya.cestzam.api.client.state.CestzamState;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;

public class CestzamApiClient implements AutoCloseable {

    private final CestzamState cestzamState = new CestzamState();
    private final URI apiUrl;
    private final Long clientTimeout;

    // on demand
    private MyminfinApiClient myminfin;

    CestzamApiClient(CestzamApiClientConfigutation configuration) {
        this.apiUrl = configuration.getApiUri();
        this.clientTimeout = configuration.getClientTimeout();
        this.cestzamState.setCestzamContext(configuration.getState());
    }

    void updateState(String cestzamContext, boolean authenticated) {
        this.cestzamState.setCestzamContext(cestzamContext);
        this.cestzamState.setCestzamAuthenticated(authenticated);
    }

    @Override
    public void close() throws Exception {
        if (this.myminfin != null) {
            this.myminfin.close();
            this.myminfin = null;
        }
    }

    public String getStateContext() throws CestzamApiClientError {
        return cestzamState.getCestzamContextOptional()
                .orElseThrow(() -> new CestzamApiClientError("Context unset"));
    }

    public MyminfinApiClient getMyminfin() {
        if (this.myminfin != null) {
            return this.myminfin;
        } else {
            this.myminfin = new MyminfinApiClient(this);
            return myminfin;
        }
    }

    public RestClientBuilder getClientBuilder() {
        return RestClientBuilder.newBuilder()
                .register(new QueryParamRequestFilter(this.clientTimeout))
                .baseUri(this.apiUrl);
    }

}
