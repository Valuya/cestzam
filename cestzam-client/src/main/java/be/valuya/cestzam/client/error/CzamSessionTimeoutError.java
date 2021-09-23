package be.valuya.cestzam.client.error;

public class CzamSessionTimeoutError extends CestzamClientError {
    public CzamSessionTimeoutError() {
        super("CZAM session expired");
    }
}
