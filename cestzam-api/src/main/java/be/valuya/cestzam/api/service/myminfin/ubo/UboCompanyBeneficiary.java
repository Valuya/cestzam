package be.valuya.cestzam.api.service.myminfin.ubo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
public class UboCompanyBeneficiary {

    private String id;

    private Boolean belgian;

    private LocalDate birthDate;

    private String firstName;
    private String lastName;
    private String fullName;
    // SSIN
    private String identificationNumber;
    private String name;
    // PERSON
    private String type;
    private List<UboCountry> nationalities;
}
