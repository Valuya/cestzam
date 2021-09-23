package be.valuya.cestzam.api.login.token;

import be.valuya.cestzam.api.context.WithCestzamContext;
import be.valuya.cestzam.api.login.Capacity;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class TokenCodeRequest extends WithCestzamContext {
    private String tokenVerificationCode;
    private Capacity capacity;
}
