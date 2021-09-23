package be.valuya.cestzam.client.debug;

import be.valuya.cestzam.client.CestzamClientConfig;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ApplicationScoped
public class CestzamDebugService {

    @Inject
    private CestzamClientConfig cestzamClientConfig;
    @Inject
    private Instance<Tracer> tracer;

    public String createFlowDebugTag(String... parts) {
        List<String> nameParts = new ArrayList<>();
        String nowTimeString = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        nameParts.add(nowTimeString);
        Arrays.stream(parts).forEach(nameParts::add);
        return String.join("_", nameParts);
    }

    public <T> HttpResponse<T> debugResponse(String debugTag, HttpResponse<T> response,
                                             Optional<CookieHandler> cookieHandler) {
        Optional<HttpRequest.BodyPublisher> bodyPublisher = response.request().bodyPublisher();
        return debugResponse(debugTag, response, cookieHandler, bodyPublisher);
    }

    public <T> HttpResponse<T> trace(String debugTag, HttpRequest request, Supplier<HttpResponse<T>> response) {
        if (tracer.isResolvable()) {
            Tracer tracer = this.tracer.get();
            return sendTraceSpan(tracer, debugTag, request, response);
        } else {
            return response.get();
        }
    }

    private <T> HttpResponse<T> debugResponse(String debugTag, HttpResponse<T> response,
                                              Optional<CookieHandler> cookieHandler,
                                              Optional<HttpRequest.BodyPublisher> originalRequestBodyPublisherOptional) {

        Boolean debugEnabled = cestzamClientConfig.getClientDebugEnabled();
        if (!debugEnabled) {
            return response;
        }

        HttpRequest request = response.request();

        response.previousResponse()
                .ifPresent(previousResponse -> debugResponse(debugTag, previousResponse, cookieHandler, originalRequestBodyPublisherOptional));


        PrintStream debugOutputStream = this.createDebugOutputStream(debugTag, request);
        debugResponseToStream(response, originalRequestBodyPublisherOptional, request, debugOutputStream);
        if (!debugOutputStream.equals(System.out)) {
            debugOutputStream.close();
        }

        Boolean clientCookiesDebugEnabled = cestzamClientConfig.getClientCookiesDebugEnabled();
        if (clientCookiesDebugEnabled) {
            cookieHandler.ifPresent(handler -> debugCookies((CookieManager) handler, debugTag, response));
        }

        return response;
    }


    private <T> HttpResponse<T> sendTraceSpan(Tracer tracer, String debugTag,
                                              HttpRequest request, Supplier<HttpResponse<T>> response) {
        URI uri = request.uri();
        String method = request.method();

        Tracer.SpanBuilder spanBuilder = tracer.buildSpan(method + " " + uri.getHost())
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.HTTP_METHOD.getKey(), method)
                .withTag(Tags.HTTP_URL.getKey(), uri.toASCIIString())
                .withTag(Tags.COMPONENT.getKey(), "cestzam-client")
                .withTag(Tags.PEER_HOSTNAME.getKey(), uri.getHost())
                .withTag("cestzam.debug", debugTag);
        try (Scope spanScope = spanBuilder.startActive(true)) {
            HttpResponse<T> httpResponse = response.get();
            int statusCode = httpResponse.statusCode();
            spanScope.span().setTag(Tags.HTTP_STATUS.getKey(), statusCode);
            return httpResponse;
        }
    }

    private PrintStream createDebugOutputStream(String flowId, HttpRequest request) {
        String formateddTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
        String host = request.uri().getHost();
        String method = request.method();
        String safePath = request.uri().getPath().replaceAll("\\W", "_");

        String fileName = MessageFormat.format("{0}_{1}_{2}_{3}.out",
                formateddTime, method, host, safePath);

        return cestzamClientConfig.getClientDebugOutputPath()
                .map(p -> this.createDebugOutputStream(p, flowId, fileName))
                .orElse(System.out);
    }

    private PrintStream createDebugOutputStream(String flowId, String paramName) {
        return cestzamClientConfig.getClientDebugOutputPath()
                .map(p -> this.createDebugOutputStream(p, flowId, paramName))
                .orElse(System.out);
    }

    private PrintStream createDebugOutputStream(String pathString, String flowId, String fileName) {
        Path directoryPath = Paths.get(pathString, flowId);
        try {
            Files.createDirectories(directoryPath);


            Path filePath = directoryPath.resolve(fileName);
            OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW);
            PrintStream printStream = new PrintStream(outputStream);
            return printStream;
        } catch (IOException e) {
            System.err.println("Cannot create file output directory " + directoryPath);
            return System.out;
        }
    }

    private <T> void debugResponseToStream(HttpResponse<T> httpResponse,
                                           Optional<HttpRequest.BodyPublisher> originalRequestBody,
                                           HttpRequest request, PrintStream outStream) {
        String method = request.method();
        String uri = request.uri().toString();
        outStream.println("> " + method + " " + uri);
        String requestHeaders = request.headers().map()
                .entrySet()
                .stream()
                .map(requestHeaderMapEntry -> requestHeaderMapEntry.getKey() + ": " + requestHeaderMapEntry.getValue() + "\n")
                .reduce("", String::concat);
        outStream.println(requestHeaders);

        boolean originalRequest = httpResponse.previousResponse()
                .isEmpty();
        if (originalRequest) {
            originalRequestBody.ifPresent(bodyPublisher -> logRequestBody(bodyPublisher, outStream));
        }

        outStream.println("< " + httpResponse.statusCode());
        HttpHeaders httpHeaders = httpResponse.headers();
        String headers = httpHeaders.map()
                .entrySet()
                .stream()
                .map(responseHeaderMapEntry -> responseHeaderMapEntry.getKey() + ": " + responseHeaderMapEntry.getValue() + "\n")
                .reduce("", String::concat);
        outStream.println(headers);

        outStream.println("---start response body---");
        outStream.println(httpResponse.body());
        outStream.println("");
    }

    private void logRequestBody(HttpRequest.BodyPublisher bodyPublisher, PrintStream outStream) {
        HttpResponse.BodySubscriber<String> stringBodySubscriber = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);
        DebugBodySubscriber stringSubscriber = new DebugBodySubscriber(stringBodySubscriber);
        bodyPublisher.subscribe(stringSubscriber);
        String bodyString = stringBodySubscriber.getBody()
                .toCompletableFuture()
                .join();
        int index = 0;
        int lineWidth = 128;
        outStream.println("POST data:");
        while (index + lineWidth <= bodyString.length()) {
            String line = bodyString.substring(index, index + lineWidth);
            outStream.println(line);
            index += lineWidth;
        }
        String lastLine = bodyString.substring(index);
        outStream.println(lastLine);
        outStream.println();
    }

    private void debugCookies(CookieManager cookieManager, String flowId, HttpResponse<?> response) {
        HttpRequest request = response.request();
        PrintStream debugOutputStream = this.createDebugOutputStream(flowId, request);

        List<HttpCookie> cookiesList = cookieManager.getCookieStore().getCookies();
        int cookies = cookiesList.size();
        debugOutputStream.println("============ " + cookies + " cookies:  ==================");

        cookiesList.stream()
                .map(c -> c.getDomain() + " " + c.getPath() + ": " + c.getName() + " = " + c.getValue())
                .forEach(debugOutputStream::println);
        debugOutputStream.println("");
    }

}
