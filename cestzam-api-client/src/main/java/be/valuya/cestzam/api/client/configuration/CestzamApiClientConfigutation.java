package be.valuya.cestzam.api.client.configuration;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;

@Setter
@Getter
public class CestzamApiClientConfigutation {

    private URI apiUri;
    private String state;
    private Long clientTimeout;

}
