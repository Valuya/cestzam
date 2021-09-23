package be.valuya.cestzam.client.cookie;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleHttpCookie {

    private String name;
    private String value;
    private String domain;
    private String path;
    private boolean httpOnly;
    private boolean secure;
    private int version;

}
