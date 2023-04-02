package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {
    private Country country;
    // 000
    private String houseNumber;
    private String houseBoxNumber;
    private String localityName;
    private String streetName;
    private String zipCode;
}
