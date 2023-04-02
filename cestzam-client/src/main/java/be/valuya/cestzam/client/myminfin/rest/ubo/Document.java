package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Document {

    private Long id;
    // 000
    private String name;
    private String type;
    private Boolean removed;
    private BigDecimal size;
    private String dueDate;
    private String controlDocumentType;

    private DocumentType documentType;

}
