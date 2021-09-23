package be.valuya.cestzam.client.cookie;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CestzamCookies {
    private List<SimpleHttpCookie> cookiesList = new ArrayList<>();
}
