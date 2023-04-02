package be.valuya.cestzam.api.service.myminfin.ubo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UboCompanyControl {

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

    private LocalDate controlStartsOn;

    private LocalDate controlEndsOn;

    private Object group;

    private UboCompanyControlType controlType;
    private UboCompany company;

    private UboCompanyBeneficiary beneficiary;
    private List<UboDocument> documents;

}
