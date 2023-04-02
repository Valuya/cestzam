package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanyBeneficiary;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanyControl;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanyControlType;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCountry;
import be.valuya.cestzam.api.service.myminfin.ubo.UboDocument;
import be.valuya.cestzam.api.service.myminfin.ubo.UboDocumentType;
import be.valuya.cestzam.client.myminfin.rest.ubo.Beneficiary;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompanyControl;
import be.valuya.cestzam.client.myminfin.rest.ubo.ControlType;
import be.valuya.cestzam.client.myminfin.rest.ubo.Country;
import be.valuya.cestzam.client.myminfin.rest.ubo.Document;
import be.valuya.cestzam.client.myminfin.rest.ubo.DocumentType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class UboCompanyControlConverter {

    @Inject
    private UboCompanyConverter uboCompanyConverter;

    public UboCompanyControl convertUboCompanyControl(CompanyControl companyControl) {
        UboCompanyControl uboCompanyControl = new UboCompanyControl();

        Optional.ofNullable(companyControl.getId())
                .ifPresent(uboCompanyControl::setId);

        Optional.ofNullable(companyControl.getPercent())
                .ifPresent(uboCompanyControl::setPercent);

        Optional.ofNullable(companyControl.getPercentVote())
                .ifPresent(uboCompanyControl::setPercentVote);

        Optional.ofNullable(companyControl.getComment())
                .ifPresent(uboCompanyControl::setComment);

        Optional.ofNullable(companyControl.getDescription())
                .ifPresent(uboCompanyControl::setDescription);

        Optional.ofNullable(companyControl.getComposition())
                .ifPresent(uboCompanyControl::setComposition);

        Optional.ofNullable(companyControl.getValidated())
                .ifPresent(uboCompanyControl::setValidated);

        Optional.ofNullable(companyControl.getGrouped())
                .ifPresent(uboCompanyControl::setGrouped);

        Optional.ofNullable(companyControl.getDerogated())
                .ifPresent(uboCompanyControl::setDerogated);

        Optional.ofNullable(companyControl.getControlStartsOn())
                .ifPresent(uboCompanyControl::setControlStartsOn);

        Optional.ofNullable(companyControl.getControlEndsOn())
                .ifPresent(uboCompanyControl::setControlEndsOn);

        Optional.ofNullable(companyControl.getGroup())
                .ifPresent(uboCompanyControl::setGroup);

        Optional.ofNullable(companyControl.getControlType())
                .map(this::convertUboControlType)
                .ifPresent(uboCompanyControl::setControlType);

        Optional.ofNullable(companyControl.getCompany())
                .map(uboCompanyConverter::convertUboCompany)
                .ifPresent(uboCompanyControl::setCompany);

        Optional.ofNullable(companyControl.getBeneficiary())
                .map(this::convertUboBeneficiary)
                .ifPresent(uboCompanyControl::setBeneficiary);

        List<Document> documents = Optional.ofNullable(companyControl.getDocuments())
                .orElseGet(ArrayList::new);
        List<UboDocument> documentList = documents.stream()
                .map(this::convertUboDocument)
                .collect(Collectors.toList());
        uboCompanyControl.setDocuments(documentList);

        return uboCompanyControl;
    }

    private <R> UboDocument convertUboDocument(Document document) {
        UboDocument uboDocument = new UboDocument();

        Optional.ofNullable(document.getId())
                .ifPresent(uboDocument::setId);

        Optional.ofNullable(document.getName())
                .ifPresent(uboDocument::setName);

        Optional.ofNullable(document.getType())
                .ifPresent(uboDocument::setType);

        Optional.ofNullable(document.getRemoved())
                .ifPresent(uboDocument::setRemoved);

        Optional.ofNullable(document.getSize())
                .map(BigDecimal::longValue)
                .ifPresent(uboDocument::setSize);

        Optional.ofNullable(document.getDueDate())
                .ifPresent(uboDocument::setDueDate);

        Optional.ofNullable(document.getControlDocumentType())
                .ifPresent(uboDocument::setControlDocumentType);

        Optional.ofNullable(document.getDocumentType())
                .map(this::convertUboDocumentType)
                .ifPresent(uboDocument::setDocumentType);

        return uboDocument;
    }

    private <U> UboDocumentType convertUboDocumentType(DocumentType documentType) {
        UboDocumentType uboDocumentType = new UboDocumentType();

        Optional.ofNullable(documentType.getId())
                .ifPresent(uboDocumentType::setId);

        Optional.ofNullable(documentType.getCode())
                .ifPresent(uboDocumentType::setCode);

        Optional.ofNullable(documentType.getDescription())
                .ifPresent(uboDocumentType::setDescription);

        Optional.ofNullable(documentType.getLabel())
                .ifPresent(uboDocumentType::setLabel);

        Optional.ofNullable(documentType.getValue())
                .ifPresent(uboDocumentType::setValue);

        return uboDocumentType;
    }

    private <U> UboCompanyBeneficiary convertUboBeneficiary(Beneficiary beneficiary) {
        UboCompanyBeneficiary uboCompanyBeneficiary = new UboCompanyBeneficiary();

        Optional.ofNullable(beneficiary.getId())
                .ifPresent(uboCompanyBeneficiary::setId);

        Optional.ofNullable(beneficiary.getBelgian())
                .ifPresent(uboCompanyBeneficiary::setBelgian);

        Optional.ofNullable(beneficiary.getBirthDate())
                .ifPresent(uboCompanyBeneficiary::setBirthDate);

        Optional.ofNullable(beneficiary.getFirstName())
                .ifPresent(uboCompanyBeneficiary::setFirstName);

        Optional.ofNullable(beneficiary.getLastName())
                .ifPresent(uboCompanyBeneficiary::setLastName);

        Optional.ofNullable(beneficiary.getFullName())
                .ifPresent(uboCompanyBeneficiary::setFullName);

        Optional.ofNullable(beneficiary.getName())
                .ifPresent(uboCompanyBeneficiary::setName);

        Optional.ofNullable(beneficiary.getIdentificationNumber())
                .ifPresent(uboCompanyBeneficiary::setIdentificationNumber);

        Optional.ofNullable(beneficiary.getType())
                .ifPresent(uboCompanyBeneficiary::setType);

        List<Country> countries = Optional.ofNullable(beneficiary.getNationalities())
                .orElseGet(ArrayList::new);
        List<UboCountry> countryList = countries.stream()
                .map(this::convertUboCountry)
                .collect(Collectors.toList());
        uboCompanyBeneficiary.setNationalities(countryList);

        return uboCompanyBeneficiary;
    }

    private <R> UboCountry convertUboCountry(Country country) {
        UboCountry uboCountry = new UboCountry();

        Optional.ofNullable(country.getId())
                .ifPresent(uboCountry::setId);

        Optional.ofNullable(country.getCode())
                .ifPresent(uboCountry::setCode);

        Optional.ofNullable(country.getIso3())
                .ifPresent(uboCountry::setIso3);

        Optional.ofNullable(country.getDescription())
                .ifPresent(uboCountry::setDescription);

        Optional.ofNullable(country.getLabel())
                .ifPresent(uboCountry::setLabel);

        Optional.ofNullable(country.getValue())
                .ifPresent(uboCountry::setValue);

        return uboCountry;
    }

    private <U> UboCompanyControlType convertUboControlType(ControlType controlType) {
        UboCompanyControlType uboCompanyControlType = new UboCompanyControlType();

        Optional.ofNullable(controlType.getId())
                .ifPresent(uboCompanyControlType::setId);

        Optional.ofNullable(controlType.getCode())
                .ifPresent(uboCompanyControlType::setCode);

        Optional.ofNullable(controlType.getCompanyType())
                .ifPresent(uboCompanyControlType::setCompanyType);

        Optional.ofNullable(controlType.getDescription())
                .ifPresent(uboCompanyControlType::setDescription);

        Optional.ofNullable(controlType.getLabel())
                .ifPresent(uboCompanyControlType::setLabel);

        Optional.ofNullable(controlType.getValue())
                .ifPresent(uboCompanyControlType::setValue);

        return uboCompanyControlType;
    }
}
