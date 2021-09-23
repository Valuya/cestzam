package be.valuya.cestzam.api.client;

public class CestzamApiClientError extends RuntimeException {
    public CestzamApiClientError(String message) {
        super(message);
    }
}
