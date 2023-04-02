package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ControlType {
    // 12
    private Long id;
    // PERCENT
    private String code;
    // ARTICLE2
    private String companyType;
    private String description;
    private String label;
    // PERCENT
    private String value;
}
