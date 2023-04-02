package be.valuya.cestzam.api.service.myminfin.ubo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UboCompanyControlType {

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
