package be.valuya.cestzam.api.service.myminfin.ubo;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UboRequestContext {
    private AuthenticatedMyminfinContext myminfinContext;

}
