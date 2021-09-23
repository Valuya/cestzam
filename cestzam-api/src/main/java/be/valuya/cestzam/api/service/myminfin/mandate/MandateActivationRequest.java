package be.valuya.cestzam.api.service.myminfin.mandate;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class MandateActivationRequest   {
    private AuthenticatedMyminfinContext myminfinContext;
    private MyMinfinMandate mandate;
}
