package be.valuya.cestzam.client.czam;

import be.valuya.cestzam.client.cookie.CestzamCookies;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CestzamLoginContext {
    private String spEntityId;
    private String service;
    private String gotoValue;
    private CestzamCookies cestzamCookies;
}
