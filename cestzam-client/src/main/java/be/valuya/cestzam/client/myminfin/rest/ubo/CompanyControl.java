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
public class CompanyControl {

    private Long id;

    // 4-decimals 0.0000 - 100.0000
    private String percent;
    private String percentVote;
    private String comment;
    private String description;

    private List<Object> composition;

    private Boolean validated;

    private Boolean grouped;
    private Boolean derogated;

    @JsonbTypeDeserializer(UboDateDeserializer.class)
    private LocalDate controlStartsOn;

    @JsonbTypeDeserializer(UboDateDeserializer.class)
    private LocalDate controlEndsOn;

    private Object group;

    private ControlType controlType;
    private Company company;

    private Beneficiary beneficiary;
    private List<Document> documents;
}
