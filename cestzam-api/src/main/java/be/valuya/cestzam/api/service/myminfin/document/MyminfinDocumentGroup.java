package be.valuya.cestzam.api.service.myminfin.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyminfinDocumentGroup implements Serializable {

    private String label;
    private String value;
}
