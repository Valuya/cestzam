package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompaniesSearchRequest {

    private String identificationNumber;
    // 003... //TODO: api/references/legal-forms
    private String legalForm;
    private String name;

    private String language;
    private Integer page;
    private Integer limit;
}
