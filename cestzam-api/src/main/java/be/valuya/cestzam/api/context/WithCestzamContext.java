package be.valuya.cestzam.api.context;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class WithCestzamContext {
    private String cestzamContext;
}
