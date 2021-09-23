package be.valuya.cestzam.client.myminfin.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserData {
    private String firstname;
    private String lastname;
    private String name;
    private String nationalNumber;
    private String visitorType;
    private Boolean canSelectCustomer;

    // Active mandate
    private String customerType;
    private String customerName;
    private String customerNN;
    private String customerVat;
    private Boolean customerIsSelected;
}
