package be.valuya.cestzam.client.myminfin.rest.vatbalance;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CurrentVatBalance {

    private List<Object> restMessages;
    private Boolean mustRedirectToHome;
    private String companyNumber;
    private String currentPeriod;
    private String currentDate;
    private Boolean previousSaldoAvailable;
    private String previousDueAmount;
    private String previousDueDate;
    private String previousUsageCode;
    private String previousUsageLabel;
    private String previousFines;
    private String previousInterests;
    private Boolean previousReset;
    private String currentDueAmount;
    private List<Object> arguments;
    private Boolean sessionExpired;
    private Boolean error;

}
