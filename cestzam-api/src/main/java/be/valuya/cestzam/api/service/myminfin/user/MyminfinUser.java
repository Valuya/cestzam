package be.valuya.cestzam.api.service.myminfin.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyminfinUser {
    private String firstname;
    private String lastname;
    private String name;
    private String nationalNumber;
    private MyminfinUserType visitorType;

    private MyminfinUserType customerType;
    private String customerName;
    private String customerNN;
    private String customerVat;
    private Boolean customerSelected;
}
