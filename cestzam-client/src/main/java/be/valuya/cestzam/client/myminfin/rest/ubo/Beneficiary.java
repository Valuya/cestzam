package be.valuya.cestzam.client.myminfin.rest.ubo;

import be.valuya.cestzam.client.response.UboDateDeserializer;
import lombok.Getter;
import lombok.Setter;

import javax.json.bind.annotation.JsonbTypeDeserializer;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Beneficiary {

    private String id;

    private Boolean belgian;

    @JsonbTypeDeserializer(UboDateDeserializer.class)
    private LocalDate birthDate;

    private String firstName;
    private String lastName;
    private String fullName;
    // SSIN
    private String identificationNumber;
    private String name;
    // PERSON
    private String type;
    private List<Country> nationalities;
}
