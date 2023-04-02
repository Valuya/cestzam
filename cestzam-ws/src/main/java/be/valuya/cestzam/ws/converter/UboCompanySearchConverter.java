package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.api.service.myminfin.ubo.UboCompany;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanySearch;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanySearchResult;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompaniesSearchRequest;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompaniesSearchResults;
import be.valuya.cestzam.client.myminfin.rest.ubo.Company;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class UboCompanySearchConverter {

    @Inject
    private UboCompanyConverter uboCompanyConverter;

    public CompaniesSearchRequest convertCompanySearch(UboCompanySearch uboCompanySearch) {
        CompaniesSearchRequest searchRequest = new CompaniesSearchRequest();

        Optional.ofNullable(uboCompanySearch.getPage())
                .ifPresent(searchRequest::setPage);

        Optional.ofNullable(uboCompanySearch.getLimit())
                .ifPresent(searchRequest::setLimit);

        Optional.ofNullable(uboCompanySearch.getName())
                .ifPresent(searchRequest::setName);

        Optional.ofNullable(uboCompanySearch.getIdentificationNumber())
                .ifPresent(searchRequest::setIdentificationNumber);

        return searchRequest;
    }


    public UboCompanySearchResult convertCompanySearchResult(CompaniesSearchResults companiesSearchResults) {
        UboCompanySearchResult uboCompanySearchResult = new UboCompanySearchResult();

        Optional.ofNullable(companiesSearchResults.getCurrentPage())
                .ifPresent(uboCompanySearchResult::setCurrentPage);

        Optional.ofNullable(companiesSearchResults.getTotalRecord())
                .ifPresent(uboCompanySearchResult::setTotalRecord);

        Optional.ofNullable(companiesSearchResults.getTotalPage())
                .ifPresent(uboCompanySearchResult::setTotalPage);

        List<Company> companies = Optional.ofNullable(companiesSearchResults.getContent())
                .orElseGet(ArrayList::new);
        List<UboCompany> uboCompanyList = companies.stream()
                .map(uboCompanyConverter::convertUboCompany)
                .collect(Collectors.toList());
        uboCompanySearchResult.setCompanies(uboCompanyList);

        return uboCompanySearchResult;
    }
}
