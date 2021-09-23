package be.valuya.cestzam.api.service.myminfin.mandate;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MyminfinMandateSearch    {
    @NotNull
    private AuthenticatedMyminfinContext myminfinContext;
    @NotNull
    private MyminfinMandateType mandateType;
}
