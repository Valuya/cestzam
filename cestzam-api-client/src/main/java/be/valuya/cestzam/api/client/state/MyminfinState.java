package be.valuya.cestzam.api.client.state;

import be.valuya.cestzam.api.service.myminfin.mandate.MyMinfinMandate;
import be.valuya.cestzam.api.service.myminfin.user.MyminfinUser;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Setter
public class MyminfinState {

    private MyminfinUser user;
    private List<MyMinfinMandate> availableEnterpriseMandates;
    private List<MyMinfinMandate> availableCitizenMandates;

    public Optional<MyminfinUser> getUserOptional() {
        return Optional.ofNullable(user);
    }

    public Optional<List<MyMinfinMandate>> getAvailableEnterpriseMandatesOptional() {
        return Optional.ofNullable(availableEnterpriseMandates);
    }

    public Optional<List<MyMinfinMandate>> getAvailableCitizenMandatesOptional() {
        return Optional.ofNullable(availableCitizenMandates);
    }
}
