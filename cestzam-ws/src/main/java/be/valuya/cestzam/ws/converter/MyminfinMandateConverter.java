package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.api.service.myminfin.mandate.MyMinfinMandate;
import be.valuya.cestzam.client.myminfin.rest.mandate.ApplicationMandate;
import be.valuya.cestzam.client.myminfin.rest.mandate.Mandate;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MyminfinMandateConverter {

    public MyMinfinMandate toMyMinfinMandate(ApplicationMandate applicationMandate) {
        Mandate mandate = applicationMandate.getMandate();

        MyMinfinMandate minfinMandate = new MyMinfinMandate();
        minfinMandate.setApplicationName(applicationMandate.getApplicationName());
        minfinMandate.setMandateApplication(mandate.getApplication());
        minfinMandate.setMandateTypeNamesByLocale(mandate.getMandateTypeNamesByLocale());
        minfinMandate.setMandateeCompanyNumber(mandate.getMandateeCompanyNumber());
        minfinMandate.setMandateeIdentifier(mandate.getMandateeIdentifier());
        minfinMandate.setMandateeName(mandate.getMandateeName());
        minfinMandate.setMandateePersIdf(mandate.getMandateePersIdf());
        minfinMandate.setMandatorIdentifier(mandate.getMandatorIdentifier());
        minfinMandate.setMandatorName(mandate.getMandatorName());
        minfinMandate.setMandatorNationalNumber(mandate.getMandatorNationalNumber());
        minfinMandate.setMandatorPersIdf(mandate.getMandatorPersIdf());
        minfinMandate.setMandatorType(mandate.getMandatorType());

        return minfinMandate;
    }


    public ApplicationMandate toApplicationMandate(MyMinfinMandate myMinfinMandate) {

        ApplicationMandate applicationMandate = new ApplicationMandate();
        Mandate mandate = new Mandate();
        mandate.setApplication(myMinfinMandate.getMandateApplication());
        mandate.setMandateTypeNamesByLocale(myMinfinMandate.getMandateTypeNamesByLocale());
        mandate.setMandateeCompanyNumber(myMinfinMandate.getMandateeCompanyNumber());
        mandate.setMandateeIdentifier(myMinfinMandate.getMandateeIdentifier());
        mandate.setMandateeName(myMinfinMandate.getMandateeName());
        mandate.setMandateePersIdf(myMinfinMandate.getMandateePersIdf());
        mandate.setMandatorIdentifier(myMinfinMandate.getMandatorIdentifier());
        mandate.setMandatorName(myMinfinMandate.getMandatorName());
        mandate.setMandatorNationalNumber(myMinfinMandate.getMandatorNationalNumber());
        mandate.setMandatorPersIdf(myMinfinMandate.getMandatorPersIdf());
        mandate.setMandatorType(myMinfinMandate.getMandatorType());
        applicationMandate.setMandate(mandate);
        applicationMandate.setApplicationName(myMinfinMandate.getApplicationName());

        return applicationMandate;
    }


}
