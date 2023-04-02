package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CompositionTreeNode {

    // ROOT / PERSON
    private String type;
    // non-decimal 0-100
    private String percent;
    private String percentVote;
    private String name;
    private Boolean direct;
    private CompanyControl control;

    private List<CompositionTreeNode> children;

}
