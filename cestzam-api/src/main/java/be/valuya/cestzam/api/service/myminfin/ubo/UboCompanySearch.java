package be.valuya.cestzam.api.service.myminfin.ubo;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UboCompanySearch {
    private AuthenticatedMyminfinContext myminfinContext;

    private String identificationNumber;
    private String name;

    private Integer page;
    private Integer limit;
}
