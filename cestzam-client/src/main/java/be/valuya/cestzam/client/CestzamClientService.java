package be.valuya.cestzam.client;

import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.cookie.SimpleHttpCookie;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CestzamClientService {

    @Inject
    private CestzamClientConfig cestzamClientConfig;

    public HttpClient createFollowRedirectClient(CestzamCookies cookies) {
        HttpClient.Builder cestzamClientBuilder = createCestzamClientBuilder(cookies);
        return cestzamClientBuilder.followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public HttpClient createNoRedirectClient(CestzamCookies cookies) {
        HttpClient.Builder cestzamClientBuilder = createCestzamClientBuilder(cookies);
        return cestzamClientBuilder.followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    public HttpClient.Builder createCestzamClientBuilder(CestzamCookies cookies) {
        Duration timeoutDuration = Duration.ofSeconds(cestzamClientConfig.getClientTimeoutSeconds());
        CookieManager cookieManager = createCookieManager(cookies);

        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(timeoutDuration)
                .cookieHandler(cookieManager);
        return builder;
    }

    public CestzamCookies extractCookies(HttpClient httpClient) {
        CestzamCookies cookies = new CestzamCookies();

        httpClient.cookieHandler()
                .ifPresent(handler -> this.fillCookies(handler, cookies));
        return cookies;
    }

    private void fillCookies(CookieHandler handler, CestzamCookies cookies) {
        if (handler instanceof CookieManager) {
            CookieManager cookieManager = (CookieManager) handler;
            CookieStore cookieStore = cookieManager.getCookieStore();

            List<SimpleHttpCookie> cookiesList = cookieStore.getCookies()
                    .stream()
                    .map(this::convertToSimpleHttpCookie)
                    .collect(Collectors.toList());
            cookies.getCookiesList().clear();
            cookies.getCookiesList().addAll(cookiesList);
        }
    }

    private CookieManager createCookieManager(CestzamCookies cookies) {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieStore cookieStore = cookieManager.getCookieStore();

        cookies.getCookiesList()
                .stream()
                .map(this::convertToHttpCookie)
                .forEach(c -> cookieStore.add(null, c));

        return cookieManager;
    }

    private SimpleHttpCookie convertToSimpleHttpCookie(HttpCookie httpCookie) {
        String name = httpCookie.getName();
        String value = httpCookie.getValue();
        String domain = httpCookie.getDomain();
        String path = httpCookie.getPath();
        boolean httpOnly = httpCookie.isHttpOnly();
        boolean secure = httpCookie.getSecure();
        int version = httpCookie.getVersion();

        return new SimpleHttpCookie(name, value, domain, path, httpOnly, secure, version);
    }

    private HttpCookie convertToHttpCookie(SimpleHttpCookie simpleHttpCookie) {
        String name = simpleHttpCookie.getName();
        String value = simpleHttpCookie.getValue();
        String domain = simpleHttpCookie.getDomain();
        String path = simpleHttpCookie.getPath();
        boolean httpOnly = simpleHttpCookie.isHttpOnly();
        boolean secure = simpleHttpCookie.isSecure();
        int version = simpleHttpCookie.getVersion();

        HttpCookie httpCookie = new HttpCookie(name, value);
        httpCookie.setDomain(domain);
        httpCookie.setPath(path);
        httpCookie.setHttpOnly(httpOnly);
        httpCookie.setSecure(secure);
        httpCookie.setVersion(version);

        return httpCookie;
    }
}
