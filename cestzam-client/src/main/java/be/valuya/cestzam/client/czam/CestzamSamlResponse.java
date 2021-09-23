package be.valuya.cestzam.client.czam;

import be.valuya.cestzam.client.cookie.CestzamCookies;

public interface CestzamSamlResponse {

    CestzamCookies getCookies();

    String getSamlResponse();

    String getRelayState();

}
