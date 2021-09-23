package be.valuya.cestzam.client.myminfin;

import be.valuya.cestzam.client.cookie.CestzamCookies;
import be.valuya.cestzam.client.czam.CzamCitizenInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CestzamAuthenticatedMyminfinContext {
    private CestzamCookies cookies;
    private CzamCitizenInfo czamCitizenInfo;
}
