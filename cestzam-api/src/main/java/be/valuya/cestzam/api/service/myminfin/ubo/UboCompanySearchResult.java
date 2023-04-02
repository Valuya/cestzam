package be.valuya.cestzam.api.service.myminfin.ubo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UboCompanySearchResult {
    private Long currentPage;
    private Long totalPage;
    private Long totalRecord;

    private List<UboCompany> companies;
}
