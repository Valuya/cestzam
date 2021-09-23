package be.valuya.cestzam.api.login.token;

import be.valuya.cestzam.api.context.WithCestzamContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenLoginRequest extends WithCestzamContext {
    private String login;
    private String password;
}
