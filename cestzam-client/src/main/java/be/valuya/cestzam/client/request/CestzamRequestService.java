package be.valuya.cestzam.client.request;

import be.valuya.cestzam.client.CestzamClientConfig;
import be.valuya.cestzam.client.debug.CestzamDebugService;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ApplicationScoped
public class CestzamRequestService {

    @Inject
    private CestzamClientConfig cestzamClientConfig;
    @Inject
    private CestzamDebugService cestzamDebugService;

    private RequestConfig requestConfig = new RequestConfig();

    @PostConstruct
    public void init() {
        this.requestConfig.setTimeoutSecond(cestzamClientConfig.getClientTimeoutSeconds());
    }

    public void setClientTimeout(long clientTimeout) {
        this.requestConfig.setTimeoutSecond(clientTimeout);
    }

    public HttpResponse<String> getHtml(String debugTag, HttpClient client, String origin, String... parts) {
        URI uri = createUri(origin, parts);
        return getHtml(debugTag, client, uri);
    }

    public HttpResponse<String> getHtml(String debugTag, HttpClient client, URI uri) {
        HttpRequest request = createNewHtmlRequestBuilder()
                .GET()
                .uri(uri)
                .build();
        return getResponse(debugTag, client, request,
                () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .orTimeout(requestConfig.getTimeoutSecond(), TimeUnit.SECONDS)
                        .join());
    }


    public HttpResponse<String> getJson(String debugTag, HttpClient client, String origin, String... parts) {
        URI uri = createUri(origin, parts);
        return getJson(debugTag, client, uri);
    }

    public HttpResponse<String> getJson(String debugTag, HttpClient client, URI uri) {
        return getJson(debugTag, client, Map.of(), uri);
    }

    public HttpResponse<String> getJson(String debugTag, HttpClient client, Map<String, String> extraHeaders, URI uri) {
        HttpRequest.Builder requestBuilder = createNewJsonRequestBuilder();
        extraHeaders.forEach(requestBuilder::header);
        HttpRequest request = requestBuilder
                .GET()
                .uri(uri)
                .build();
        return getResponse(debugTag, client, request,
                () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .orTimeout(requestConfig.getTimeoutSecond(), TimeUnit.SECONDS)
                        .join());
    }

    public HttpResponse<String> postFormUrlEncodedAcceptHtml(String debugTag,
                                                             HttpClient client,
                                                             Map<String, String> formData,
                                                             String origin, String... parts) {
        HttpRequest request = createNewPostFormUrlEncodedHtmlRequest(formData, createUri(origin, parts))
                .build();
        return getResponse(debugTag, client, request,
                () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .orTimeout(requestConfig.getTimeoutSecond(), TimeUnit.SECONDS)
                        .join());
    }

    public HttpResponse<String> postFormUrlEncodedAcceptHtml(String debugTag,
                                                             HttpClient client,
                                                             Map<String, String> formData,
                                                             URI uri) {
        HttpRequest request = createNewPostFormUrlEncodedHtmlRequest(formData, uri)
                .build();
        return getResponse(debugTag, client, request,
                () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .orTimeout(requestConfig.getTimeoutSecond(), TimeUnit.SECONDS)
                        .join());
    }

    public HttpResponse<InputStream> postFormUrlEncodedGetSlowStream(String debugTag,
                                                                     HttpClient client,
                                                                     Map<String, String> formData,
                                                                     String origin, String parts
    ) {
        HttpRequest request = createNewPostFormUrlEncodedHtmlRequest(formData, createUri(origin, parts))
                .build();
        HttpResponse<InputStream> response = cestzamDebugService.trace(debugTag, request,
                () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                        .orTimeout(requestConfig.getTimeoutSecond(), TimeUnit.SECONDS)
                        .join());
        return response;
    }


    public HttpResponse<String> postJsonGetHtml(String debugTag,
                                                HttpClient client,
                                                String payload,
                                                String origin, String... parts) {
        HttpRequest request = createNewPostJsonHtmlRequest(payload, origin, parts)
                .build();
        return getResponse(debugTag, client, request,
                () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .orTimeout(requestConfig.getTimeoutSecond(), TimeUnit.SECONDS)
                        .join());
    }

    public String formatJson(Object obj) {
        Jsonb jsonb = JsonbBuilder.create();
        String json = jsonb.toJson(obj);
        return json;
    }

    public HttpResponse<String> postJsonGetJson(String debugTag,
                                                HttpClient client,
                                                String payload,
                                                String origin, String... parts) {
        HttpRequest request = createNewPostJsonJsonRequest(payload, origin, parts)
                .build();
        return getResponse(debugTag, client, request,
                () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .orTimeout(requestConfig.getTimeoutSecond(), TimeUnit.SECONDS)
                        .join());
    }

    public HttpResponse<String> postJsonGetJson(String debugTag,
                                                HttpClient client,
                                                Map<String, String> extraHeaders,
                                                String payload,
                                                String origin, String... parts) {
        HttpRequest request = createNewPostJsonJsonRequest(extraHeaders, payload, origin, parts)
                .build();
        return getResponse(debugTag, client, request,
                () -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .orTimeout(requestConfig.getTimeoutSecond(), TimeUnit.SECONDS)
                        .join());
    }


    public HttpResponse<String> followRedirectsOnOrigin(String debugTag,
                                                        HttpClient client,
                                                        HttpResponse<String> response) {
        if (response.statusCode() != 301 && response.statusCode() != 302) {
            return response;
        }
        HttpResponse<String> redirectedResponse = response.headers().firstValue("location")
                .map(redirectUri -> followRedirect(debugTag, client, response, redirectUri, true))
                .orElseGet(() -> response);
        return redirectedResponse;
    }

    public HttpResponse<String> followRedirects(String debugTag,
                                                HttpClient client, HttpResponse<String> response) {
        if (response.statusCode() != 301 && response.statusCode() != 302 && response.statusCode() != 303) {
            return response;
        }
        HttpResponse<String> redirectedResponse = response.headers().firstValue("location")
                .map(redirectUri -> followRedirect(debugTag, client, response, redirectUri, false))
                .orElseGet(() -> response);
        return redirectedResponse;
    }


    public HttpRequest.Builder createNewHtmlRequestBuilder() {
        String clientUserAgent = this.cestzamClientConfig.getClientUserAgent();
        return HttpRequest.newBuilder()
                .header("User-Agent", clientUserAgent)
                .headers("Upgrade-Insecure-Requests", "1")
                .headers("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .headers("Pragma", "no-cache")
                .headers("Cache-Control", "no-cache");
    }

    public HttpRequest.Builder createNewJsonRequestBuilder() {
        String clientUserAgent = this.cestzamClientConfig.getClientUserAgent();
        return HttpRequest.newBuilder()
                .header("User-Agent", clientUserAgent)
                .headers("Upgrade-Insecure-Requests", "1")
                .headers("Accept", "application/json, text/plain, */*; q=0.01")
                .headers("Pragma", "no-cache")
                .headers("Cache-Control", "no-cache");
    }

    public HttpRequest.Builder createNewPostFormUrlEncodedHtmlRequest(Map<String, String> formData, URI uri) {
        return createNewHtmlRequestBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(formEncodedData(formData))
                .uri(uri);
    }

    public HttpRequest.Builder createNewPostJsonHtmlRequest(String payload, String origin, String... parts) {
        return createNewHtmlRequestBuilder()
                .header("Content-Type", "application/json")
                .POST(payload == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(payload))
                .uri(createUri(origin, parts));
    }

    public HttpRequest.Builder createNewPostJsonJsonRequest(String payload, String origin, String... parts) {
        return createNewPostJsonJsonRequest(Map.of(), payload, origin, parts);
    }

    public HttpRequest.Builder createNewPostJsonJsonRequest(Map<String, String> extraHeaders, String payload, String origin, String... parts) {
        HttpRequest.Builder builder = createNewJsonRequestBuilder()
                .header("Content-Type", "application/json");
        extraHeaders.forEach(builder::header);
        return builder
                .POST(payload == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(payload))
                .uri(createUri(origin, parts));
    }

    public URI createUri(String first, String... parts) {
        String path = Arrays.stream(parts)
                .collect(Collectors.joining("/"));
        if (path.isEmpty()) {
            return URI.create(first).normalize();
        } else {
            return URI.create(first + "/" + path).normalize();
        }
    }

    private HttpRequest.BodyPublisher formEncodedData(Map<String, String> valueMap) {
        return HttpRequest.BodyPublishers.ofString(
                encodeBodyFormData(valueMap)
        );
    }

    private String encodeBodyFormData(Map<String, String> valueMap) {
        return valueMap.entrySet()
                .stream()
                .map(this::encodeEntry)
                .reduce("", (a, b) -> {
                    if (a.isEmpty()) {
                        return b;
                    } else {
                        return a + "&" + b;
                    }
                });
    }

    private String encodeEntry(Map.Entry<?, ?> entry) {
        Object value = entry.getValue();
        Object key = entry.getKey();
        String encodedKey = URLEncoder.encode(key.toString(), StandardCharsets.UTF_8);
        String encodedValue = URLEncoder.encode(value.toString(), StandardCharsets.UTF_8);
        return encodedKey + "=" + encodedValue;
    }

    private HttpResponse<String> followRedirect(String debugTag,
                                                HttpClient client, HttpResponse<String> response,
                                                String redirectUriString, boolean stayOnOrigin) {
        URI redirectUri;
        if (redirectUriString.startsWith("/")) {
            String host = response.uri().getHost();
            String scheme = response.uri().getScheme();
            redirectUri = URI.create(scheme + "://" + host + redirectUriString);
        } else {
            redirectUri = URI.create(redirectUriString);
            if (stayOnOrigin) {
                String responseHost = response.uri().getHost();
                String redirectHost = redirectUri.getHost();
                if (!responseHost.equalsIgnoreCase(redirectHost)) {
                    return response;
                }
            }
        }
        HttpResponse<String> nextResponse = this.getHtml(debugTag, client, redirectUri);
        return stayOnOrigin ? followRedirectsOnOrigin(debugTag, client, nextResponse) : followRedirects(debugTag, client, nextResponse);
    }

    private <T> HttpResponse<T> getResponse(String debugTag, HttpClient client, HttpRequest request, Supplier<HttpResponse<T>> responseSupplier) {
        HttpResponse<T> response = cestzamDebugService.trace(debugTag, request, responseSupplier);
        cestzamDebugService.debugResponse(debugTag, response, client.cookieHandler());
        return response;
    }
}
