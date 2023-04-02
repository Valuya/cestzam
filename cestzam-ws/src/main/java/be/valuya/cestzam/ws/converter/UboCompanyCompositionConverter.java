package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.api.service.myminfin.ubo.UboCompany;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanyCompositionNode;
import be.valuya.cestzam.api.service.myminfin.ubo.UboCompanySearchResult;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompaniesSearchResults;
import be.valuya.cestzam.client.myminfin.rest.ubo.Company;
import be.valuya.cestzam.client.myminfin.rest.ubo.CompositionTreeNode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class UboCompanyCompositionConverter {

    @Inject
    private UboCompanyConverter uboCompanyConverter;
    @Inject
    private UboCompanyControlConverter uboCompanyControlConverter;

    public UboCompanyCompositionNode convertCompositionNode(CompositionTreeNode compositionTreeNode) {
        UboCompanyCompositionNode uboCompanyCompositionNode = new UboCompanyCompositionNode();

        Optional.ofNullable(compositionTreeNode.getType())
                .ifPresent(uboCompanyCompositionNode::setType);

        Optional.ofNullable(compositionTreeNode.getPercent())
                .ifPresent(uboCompanyCompositionNode::setPercent);

        Optional.ofNullable(compositionTreeNode.getName())
                .ifPresent(uboCompanyCompositionNode::setName);

        Optional.ofNullable(compositionTreeNode.getPercentVote())
                .ifPresent(uboCompanyCompositionNode::setPercentVote);

        Optional.ofNullable(compositionTreeNode.getDirect())
                .ifPresent(uboCompanyCompositionNode::setDirect);

        Optional.ofNullable(compositionTreeNode.getControl())
                .map(uboCompanyControlConverter::convertUboCompanyControl)
                .ifPresent(uboCompanyCompositionNode::setControl);

        List<UboCompanyCompositionNode> childrenList = Optional.ofNullable(compositionTreeNode.getChildren())
                .orElseGet(ArrayList::new)
                .stream()
                .map(this::convertCompositionNode)
                .collect(Collectors.toList());
        uboCompanyCompositionNode.setChildren(childrenList);

        return uboCompanyCompositionNode;
    }


}
