package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyLegalForm {

    private Long id;
    // 610
    private String code;
    // ARTICLE2
    private String companyType;
    private String description;
    private String label;
    // 610
    private String value;

}
