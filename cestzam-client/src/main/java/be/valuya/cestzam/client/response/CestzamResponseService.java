package be.valuya.cestzam.client.response;

import be.valuya.cestzam.client.error.CestzamClientError;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
public class CestzamResponseService {

    public void assertSuccessStatusCode(HttpResponse<?> response) throws CestzamClientError {
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            throw new CestzamClientError("Error reponse: " + code);
        }
        // Some myminfin resources returns code 202 with '{"restMessages":[{"type":{"type":"ERROR"},"bundleKey":"UNAUTHORIZED","sticky":false}],"mustRedirectToHome":false,"error":true}'
        // TODO: move in myminfin rest clients
        boolean jsonResponseHeaderPresent = response.headers().firstValue("content-type")
                .filter("application/json"::equals)
                .isPresent();
        if (jsonResponseHeaderPresent) {
            Optional<String> restApiMessages = Optional.empty();
            try {
                String body = (String) response.body();
                StringReader stringReader = new StringReader(body);
                JsonReader jsonReader = Json.createReader(stringReader);
                JsonStructure jsonStructure = jsonReader.read();
                JsonObject jsonObject = jsonStructure.asJsonObject();
                JsonValue errorValue = jsonObject.get("error");
                if (JsonValue.TRUE.equals(errorValue)) {
                    String restMessageString = jsonObject.get("restMessages").toString();
                    restApiMessages = Optional.of(restMessageString);
                }
            } catch (Exception ignored) {
                // Silent possible errors while parsing unknown json
            }
            if (restApiMessages.isPresent()) {
                throw new CestzamClientError("Api json response error :" + restApiMessages.get());
            }
        }
    }

    public Optional<String> searchAttributeInDom(String dom, String cssQuery, String attributeName) {
        Element elementNullable = Jsoup.parse(dom)
                .selectFirst(cssQuery);
        return Optional.ofNullable(elementNullable)
                .map(element -> element.attr(attributeName));
    }

    public Optional<String> searchSelectedOptionValuesInDom(String dom, String selectElementCssQuery) {
        Element elementNullable = Jsoup.parse(dom)
                .selectFirst(selectElementCssQuery);
        return Optional.ofNullable(elementNullable)
                .map(Node::childNodes)
                .flatMap(children -> children.stream()
                        .filter(e -> e.hasAttr("selected"))
                        .map(e -> e.attr("value"))
                        .filter(s -> !s.isBlank())
                        .findAny());
    }

    public List<String> searchSelectOptionValuesInDom(String dom, String selectElementCssQuery) {
        Element elementNullable = Jsoup.parse(dom)
                .selectFirst(selectElementCssQuery);
        return Optional.ofNullable(elementNullable)
                .map(Node::childNodes)
                .map(children -> children.stream()
                        .map(e -> e.attr("value"))
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    public List<String> listAttributesInDom(String dom, String cssQuery, String attributeName) {
        return Jsoup.parse(dom)
                .select(cssQuery)
                .stream()
                .map(element -> element.attr(attributeName))
                .collect(Collectors.toList());
    }


    public Optional<String> searchTextContentInDom(String dom, String cssQuery) {
        Element elementNullable = Jsoup.parse(dom)
                .selectFirst(cssQuery);
        return Optional.ofNullable(elementNullable)
                .map(Element::text);
    }

    public Optional<String> parseURIParamsGroup1(URI uri, String patternWithCaptureGroup) {
        Matcher matcher = Pattern.compile(patternWithCaptureGroup).matcher(uri.getQuery());

        if (matcher.matches()) {
            String matchedValue = matcher.group(1);
            String decoded = URLDecoder.decode(matchedValue, StandardCharsets.UTF_8);
            return Optional.of(decoded);
        }
        return Optional.empty();
    }

    public Optional<String> parseStringGroup1(String value, String patternWithCaptureGroup) {
        Matcher matcher = Pattern.compile(patternWithCaptureGroup).matcher(value);

        if (matcher.matches()) {
            String matchedValue = matcher.group(1);
            String decoded = URLDecoder.decode(matchedValue, StandardCharsets.UTF_8);
            return Optional.of(decoded);
        }
        return Optional.empty();
    }

    public <T> T parseJson(HttpResponse<String> response, Type type) {
        String body = response.body();
        Jsonb jsonb = JsonbBuilder.create();
        T parsedEntity = jsonb.fromJson(body, type);
        return parsedEntity;
    }


    public Optional<String> searchSamlXpathStringValue(String samlDocument, String xpathQuery) {
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory
                    .newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(samlDocument)));
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new SamlXmlNamespaceContext());

            XPathExpression expr = xpath
                    .compile(xpathQuery);
            String result = (String) expr.evaluate(doc, XPathConstants.STRING);
            return Optional.of(result);
        } catch (Exception E) {
        }
        return Optional.empty();
    }

}
