package be.valuya.cestzam.api.service.myminfin.ubo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UboDocument {

    private Long id;
    // 000
    private String name;
    private String type;
    private Boolean removed;
    private Long size;
    private String dueDate;
    private String controlDocumentType;

    private UboDocumentType documentType;

}
