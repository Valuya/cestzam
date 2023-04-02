package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.api.service.myminfin.ubo.UboCompany;
import be.valuya.cestzam.client.myminfin.rest.ubo.Company;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UboCompanyConverter {

    public UboCompany convertUboCompany(Company company) {
        UboCompany uboCompany = new UboCompany();

        Optional.ofNullable(company.getId())
                .ifPresent(uboCompany::setId);

        Optional.ofNullable(company.getIdentificationNumber())
                .ifPresent(uboCompany::setIdentificationNumber);

        Optional.ofNullable(company.getName())
                .ifPresent(uboCompany::setName);

        Optional.ofNullable(company.getType())
                .ifPresent(uboCompany::setType);

        Optional.ofNullable(company.getCreationDate())
                .ifPresent(uboCompany::setCreationDate);

        Optional.ofNullable(company.getConfirmationType())
                .ifPresent(uboCompany::setConfirmationType);

        Optional.ofNullable(company.getConfirmationDate())
                .ifPresent(uboCompany::setConfirmationDate);

        Optional.ofNullable(company.getCompanyType())
                .ifPresent(uboCompany::setCompanyType);

        Optional.ofNullable(company.getCompanyLinkedToEstox())
                .ifPresent(uboCompany::setCompanyLinkedToEstox);

        Optional.ofNullable(company.getBelgian())
                .ifPresent(uboCompany::setBelgian);

        Optional.ofNullable(company.getNationalGazetteLinkDate())
                .ifPresent(uboCompany::setNationalGazetteLinkDate);

        return uboCompany;
    }
}
