package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompositionTreeRequest {

    private String language;
    private boolean includeOthers = true;
    private boolean withComposition = true;

}
