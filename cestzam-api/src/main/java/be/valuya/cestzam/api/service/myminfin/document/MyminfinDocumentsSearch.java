package be.valuya.cestzam.api.service.myminfin.document;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
public class MyminfinDocumentsSearch  {
    private AuthenticatedMyminfinContext myminfinContext;
    private String provider;
}
