package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Country {
    private Long id;
    // 000
    private String code;
    private String description;
    private String label;
    private String value;
    private String iso3;
}
