package be.valuya.cestzam.api.client.configuration;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.net.URI;

public class QueryParamRequestFilter implements ClientRequestFilter {

    private Long timeout;

    public QueryParamRequestFilter(Long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        if (timeout == null) {
            return;
        }
        URI uri = clientRequestContext.getUri();
        boolean hasNoQuery = uri.getQuery() == null || uri.getQuery().isBlank();
        URI nweUri = URI.create(uri.toASCIIString() + (hasNoQuery ? "?" : "?" + uri.getQuery() + "&") + "timeout=" + timeout);
        clientRequestContext.setUri(nweUri);
    }
}
