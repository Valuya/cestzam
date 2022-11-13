package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.api.service.myminfin.vatbalance.MyminfinCurrentVatBalance;
import be.valuya.cestzam.client.myminfin.rest.vatbalance.CurrentVatBalance;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MyminfinVatBalanceConverter {

    public MyminfinCurrentVatBalance toMyminfinCurrentVatBalance(CurrentVatBalance vatBalance) {
        MyminfinCurrentVatBalance myminfinCurrentVatBalance = new MyminfinCurrentVatBalance();

        myminfinCurrentVatBalance.setCompanyNumber(vatBalance.getCompanyNumber());
        myminfinCurrentVatBalance.setCurrentPeriod(vatBalance.getCurrentPeriod());
        myminfinCurrentVatBalance.setCurrentDate(vatBalance.getCurrentDate());
        myminfinCurrentVatBalance.setCurrentDueAmount(vatBalance.getCurrentDueAmount());
        return myminfinCurrentVatBalance;
    }
}
