package be.valuya.cestzam.client.czam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CzamCitizenInfo {
    private String firstNames;
    private String lastNames;
    private String ssin;

}
