package be.valuya.cestzam.api.service.myminfin.ubo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UboCountry {
    private Long id;
    // 000
    private String code;
    private String description;
    private String label;
    private String value;
    private String iso3;
}
