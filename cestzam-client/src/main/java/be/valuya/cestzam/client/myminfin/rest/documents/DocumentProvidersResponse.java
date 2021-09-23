package be.valuya.cestzam.client.myminfin.rest.documents;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DocumentProvidersResponse {
    private List<DocumentProviderGroup> groups;
    private Map<String, List<DocumentProviderGroup>> groupsByProvider;
    private List<String> providers;
}
