package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.api.service.myminfin.user.MyminfinUser;
import be.valuya.cestzam.api.service.myminfin.user.MyminfinUserType;
import be.valuya.cestzam.client.myminfin.rest.UserData;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MyminfinUserConverter {

    public MyminfinUser toMyminfinUser(UserData userData) {
        MyminfinUser myminfinUser = new MyminfinUser();

        String firstname = userData.getFirstname();
        String lastname = userData.getLastname();
        String name = userData.getName();
        String nationalNumber = userData.getNationalNumber();
        String customerType = userData.getCustomerType();
        String visitorType = userData.getVisitorType();
        String customerName = userData.getCustomerName();
        String customerNN = userData.getCustomerNN();
        Boolean customerSelected = userData.getCustomerIsSelected();
        String customerVat = userData.getCustomerVat();

        MyminfinUserType myminfinCustomerType = toMyminfinUserType(customerType);
        MyminfinUserType myminfinVisitorType = toMyminfinUserType(visitorType);

        myminfinUser.setFirstname(firstname);
        myminfinUser.setLastname(lastname);
        myminfinUser.setName(name);
        myminfinUser.setNationalNumber(nationalNumber);
        myminfinUser.setCustomerType(myminfinCustomerType);
        myminfinUser.setVisitorType(myminfinVisitorType);
        myminfinUser.setCustomerName(customerName);
        myminfinUser.setCustomerNN(customerNN);
        myminfinUser.setCustomerVat(customerVat);
        myminfinUser.setCustomerSelected(customerSelected);

        return myminfinUser;
    }

    private MyminfinUserType toMyminfinUserType(String typeName) {
        if (typeName == null) {
            return null;
        }
        switch (typeName.toLowerCase()) {
            case "pro":
                return MyminfinUserType.PRO;
            case "citizen":
                return MyminfinUserType.CITIZEN;
            case "guest":
                return MyminfinUserType.GUEST;
            default:
                throw new IllegalArgumentException(typeName);
        }
    }
}
