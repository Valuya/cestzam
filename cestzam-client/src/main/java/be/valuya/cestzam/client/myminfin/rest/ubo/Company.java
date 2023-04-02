package be.valuya.cestzam.client.myminfin.rest.ubo;

import be.valuya.cestzam.client.response.UboDateDeserializer;
import lombok.Getter;
import lombok.Setter;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbTypeDeserializer;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Company {
    private String id;
    // bce number
    private String identificationNumber;
    private String name;
    // COMPANY
    private String type;
    @JsonbTypeDeserializer(UboDateDeserializer.class)
    private LocalDate creationDate;
    // VALID
    private String confirmationType;
    @JsonbTypeDeserializer(UboDateDeserializer.class)
    private LocalDate confirmationDate;
    // ARTICLE2
    private String companyType;
    private Boolean companyLinkedToEstox;
    private Boolean belgian;
    @JsonbTypeDeserializer(UboDateDeserializer.class)
    private LocalDate nationalGazetteLinkDate;

    private CompanyLegalForm legalForm;
    private CompanyStatus status;
    private List<Address> addresses;

}
