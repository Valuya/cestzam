package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompaniesSearchResults {
    private Long currentPage;
    private Long totalPage;
    private Long totalRecord;

    private List<Company> content;
}
