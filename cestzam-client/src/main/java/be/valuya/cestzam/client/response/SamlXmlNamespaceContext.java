package be.valuya.cestzam.client.response;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;
import java.util.Map;

public class SamlXmlNamespaceContext implements NamespaceContext {

    private final Map<String, String> namespaces = Map.of(
            "t", "http://schemas.xmlsoap.org/ws/2005/02/trust",
            "saml", "urn:oasis:names:tc:SAML:1.0:assertion"
    );

    @Override
    public String getNamespaceURI(String prefix) {
        return namespaces.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        return namespaces.entrySet()
                .stream()
                .filter(e -> e.getValue().equals(namespaceURI))
                .findAny()
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        return namespaces.keySet().iterator();
    }
}
