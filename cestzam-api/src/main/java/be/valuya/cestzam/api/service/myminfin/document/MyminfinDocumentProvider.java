package be.valuya.cestzam.api.service.myminfin.document;

import lombok.Getter;
import lombok.Setter;


import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class MyminfinDocumentProvider implements Serializable {
    private List<MyminfinDocumentGroup> groupList;
    private String value;
}
