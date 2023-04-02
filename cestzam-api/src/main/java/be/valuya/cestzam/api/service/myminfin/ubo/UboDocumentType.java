package be.valuya.cestzam.api.service.myminfin.ubo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UboDocumentType {
    private Long id;
    // TRESCFII_CTRLPROOF
    private String code;
    private String description;
    // TRESCFII_CTRLPROOF
    private String label;
    // TRESCFII_CTRLPROOF
    private String value;
}
