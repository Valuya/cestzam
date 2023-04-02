package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserInfo {

    private Long id;

    private List<UserAuthorization> authorizations;
    private List<Object> contacts;

    private String firstname;
    private String lastname;
    private String cbeNumber;
    private String identificationNumber;

    private Boolean isControlAuthority;
    private Boolean isIam;
    private Boolean isLegalPerson;
    private Boolean isLiable;
    private Boolean isMandatee;


}
