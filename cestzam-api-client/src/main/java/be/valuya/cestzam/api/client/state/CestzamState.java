package be.valuya.cestzam.api.client.state;

import lombok.Setter;

import java.util.Optional;

@Setter
public class CestzamState {

    private String cestzamContext;
    private boolean cestzamAuthenticated;

    public Optional<String> getCestzamContextOptional() {
        return Optional.ofNullable(cestzamContext);
    }

    public boolean isCestzamAuthenticated() {
        return cestzamAuthenticated;
    }
}
