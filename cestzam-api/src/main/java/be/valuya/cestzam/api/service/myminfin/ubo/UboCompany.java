package be.valuya.cestzam.api.service.myminfin.ubo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UboCompany {

    private String id;
    // bce number
    private String identificationNumber;
    private String name;
    // COMPANY
    private String type;
    private LocalDate creationDate;
    // VALID
    private String confirmationType;
    private LocalDate confirmationDate;
    // ARTICLE2
    private String companyType;
    private Boolean companyLinkedToEstox;
    private Boolean belgian;
    private LocalDate nationalGazetteLinkDate;

}
