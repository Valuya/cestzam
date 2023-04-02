package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentType {
    private Long id;
    // TRESCFII_CTRLPROOF
    private String code;
    private String description;
    // TRESCFII_CTRLPROOF
    private String label;
    // TRESCFII_CTRLPROOF
    private String value;
}
