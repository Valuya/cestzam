package be.valuya.cestzam.api.service.myminfin.mandate;

import lombok.Getter;
import lombok.Setter;


import java.util.Map;

@Getter
@Setter
public class MyMinfinMandate   {
    private String applicationName;
    private String mandateApplication;
    private Map<String, String> mandateTypeNamesByLocale;
    private String mandateeCompanyNumber;
    private String mandateeIdentifier;
    private String mandateeName;
    private String mandateePersIdf;
    private String mandatorIdentifier;
    private String mandatorName;
    private String mandatorNationalNumber;
    private String mandatorPersIdf;
    private String mandatorType;
}
