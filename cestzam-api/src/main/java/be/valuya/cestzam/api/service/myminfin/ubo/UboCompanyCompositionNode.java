package be.valuya.cestzam.api.service.myminfin.ubo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UboCompanyCompositionNode {

    // ROOT / PERSON
    private String type;
    // non-decimal 0-100
    private String percent;
    private String percentVote;
    private String name;
    private Boolean direct;
    private UboCompanyControl control;

    private List<UboCompanyCompositionNode> children;

}
