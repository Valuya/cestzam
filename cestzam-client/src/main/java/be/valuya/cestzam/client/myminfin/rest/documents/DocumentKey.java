package be.valuya.cestzam.client.myminfin.rest.documents;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class DocumentKey {
    private String documentSelectorTypeKey;
    private Map<String, String> identifiers;
    private String providerName;
}
