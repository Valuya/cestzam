package be.valuya.cestzam.api.service.myminfin.document;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MyminfinDocument implements Serializable {
    private LocalDate documentDate;
    private Integer documentYear;
    private List<String> documentGroups;
    private MyminfinDocumentKey documentKey;
    private String documentTitle;
    private String status;
    private String typeTechnicalId;
}
