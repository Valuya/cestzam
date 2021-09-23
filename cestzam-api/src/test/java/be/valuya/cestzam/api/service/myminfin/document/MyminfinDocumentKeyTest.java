package be.valuya.cestzam.api.service.myminfin.document;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class MyminfinDocumentKeyTest {

    public static final String PROVIDER_NAME = "PROV.1";
    public static final String DOCIDSELECTOR = "doc-;id.Sel:ect\\or€";
    public static final String ID_KEY_1 = "ID_key.1";
    public static final String ID_VALUE_1 = "v@lue|";
    public static final String ID_KEY_2 = "";
    public static final String ID_VALUE_2 = "va;lue2";

    @Test
    void parseUniqueIdString() {
        MyminfinDocumentKey documentKey = new MyminfinDocumentKey();
        documentKey.setProviderName(PROVIDER_NAME);
        documentKey.setDocumentSelectorTypeKey(DOCIDSELECTOR);
        Map<String, String> ids = Map.of(
                ID_KEY_1, ID_VALUE_1,
                ID_KEY_2, ID_VALUE_2
        );
        documentKey.setIdentifiers(ids);

        String uniqueIdString = documentKey.toUniqueIdString();
        System.out.println(uniqueIdString);

        MyminfinDocumentKey parsedKey = MyminfinDocumentKey.parseUniqueIdString(uniqueIdString)
                .orElseThrow();
        Assertions.assertEquals(PROVIDER_NAME, parsedKey.getProviderName());
        Assertions.assertEquals(DOCIDSELECTOR, parsedKey.getDocumentSelectorTypeKey());

        Map<String, String> identifiers = parsedKey.getIdentifiers();
        Assertions.assertEquals(2, identifiers.size());
        Assertions.assertTrue(identifiers.containsKey(ID_KEY_1));
        Assertions.assertEquals(ID_VALUE_1, identifiers.get(ID_KEY_1));
        Assertions.assertTrue(identifiers.containsKey(ID_KEY_2));
        Assertions.assertEquals(ID_VALUE_2, identifiers.get(ID_KEY_2));
    }
}
