package be.valuya.cestzam.api.login.token;

import be.valuya.cestzam.api.context.WithCestzamContext;
import be.valuya.cestzam.api.login.Capacity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenCodesLoginRequest extends WithCestzamContext {
    private String login;
    private String password;
    private Capacity capacity;
    private Map<String, String> codes;
}
