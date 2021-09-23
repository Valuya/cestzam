package be.valuya.cestzam.api.service.myminfin.document;

import lombok.Getter;
import lombok.Setter;


import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Setter
public class MyminfinDocumentKey implements Serializable {
    private final static String UNIQUE_ID_STRING_FORMAT = "{0}.{1}.{2}";
    private final static String IDENTIFIERS_STRING_FORMAT = "{0}:{1}";
    private final static String IDENTIFIERS_SEPARATOR = ";";
    private final static Pattern UNIQUE_ID_STRING_PATTERN = Pattern.compile("^[^\\.]+\\.[^\\.]+\\.[^\\.]+.*"); // at least 3 groups

    private String providerName;
    private String documentSelectorTypeKey;
    private Map<String, String> identifiers;

    public static Optional<MyminfinDocumentKey> parseUniqueIdString(String idString) {
        Matcher matcher = UNIQUE_ID_STRING_PATTERN.matcher(idString);
        if (matcher.matches()) {
            String[] idParts = idString.split("(?<!\\\\)\\.");
            if (idParts.length == 3) {
                String group1 = idParts[0];
                String group2 = idParts[1];
                String group3 = idParts[2];

                MyminfinDocumentKey documentKey = new MyminfinDocumentKey();
                documentKey.setProviderName(unescapeAll(group1, "."));
                documentKey.setDocumentSelectorTypeKey(unescapeAll(group2, "."));

                Map<String, String> identifiers = new HashMap<>();
                String[] parts = group3.split("(?<!\\\\);");
                Arrays.stream(parts)
                        .map(identifier -> identifier.split("(?<!\\\\):"))
                        .filter(identifierParts -> identifierParts.length == 2)
                        .forEach(identifierParts -> {
                            String key = unescapeAll(identifierParts[0], ".:;");
                            String value = unescapeAll(identifierParts[1], ".:;");
                            identifiers.put(key, value);
                        });
                documentKey.setIdentifiers(identifiers);
                return Optional.of(documentKey);
            }
        }
        return Optional.empty();
    }

    public String toUniqueIdString() {
        String identifiersString = identifiers.entrySet().stream()
                .map(e -> escapeAll(e.getKey(), ".:;") + ":" + escapeAll(e.getValue(), ".:;"))
                .collect(Collectors.joining(IDENTIFIERS_SEPARATOR));
        String idString = MessageFormat.format(UNIQUE_ID_STRING_FORMAT,
                escapeAll(providerName, "."), escapeAll(documentSelectorTypeKey, "."), identifiersString);
        return idString;
    }

    private static String escapeAll(String value, String escapedChar) {
        String[] replacement = new String[]{value};
        for (char c : escapedChar.toCharArray()) {
            replacement[0] = replacement[0].replace("" + c, "\\" + c);
        }
        return replacement[0];
    }

    private static String unescapeAll(String value, String escapedChar) {
        String[] replacement = new String[]{value};
        for (char c : escapedChar.toCharArray()) {
            replacement[0] = replacement[0].replace("\\" + c, "" + c);
        }
        return replacement[0];
    }

    @Override
    public String toString() {
        String docId = identifiers.getOrDefault("FILENET_UUID",
                identifiers.getOrDefault("ID", "?"));
        return "MyminfinDocumentKey{" + providerName + " - " + docId + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyminfinDocumentKey that = (MyminfinDocumentKey) o;
        return Objects.equals(documentSelectorTypeKey, that.documentSelectorTypeKey) &&
                Objects.equals(identifiers, that.identifiers) &&
                Objects.equals(providerName, that.providerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentSelectorTypeKey, identifiers, providerName);
    }
}
