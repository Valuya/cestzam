package be.valuya.cestzam.api.service.myminfin.vatbalance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyminfinCurrentVatBalance {

    private String companyNumber;
    private String currentPeriod;
    private String currentDate;
    private String currentDueAmount;

}
