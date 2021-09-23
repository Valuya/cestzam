package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocument;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentGroup;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentKey;
import be.valuya.cestzam.api.service.myminfin.document.MyminfinDocumentProvider;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentDescription;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentKey;
import be.valuya.cestzam.client.myminfin.rest.documents.DocumentProviderGroup;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class MymininDocumentConverter {

    public MyminfinDocumentProvider toMyminfinDocumentProvider(String providerName, List<DocumentProviderGroup> providerGroups) {
        MyminfinDocumentProvider documentProvider = new MyminfinDocumentProvider();

        List<MyminfinDocumentGroup> groupList = providerGroups.stream()
                .map(group -> new MyminfinDocumentGroup(group.getLabel(), group.getValue()))
                .collect(Collectors.toList());
        documentProvider.setGroupList(groupList);

        documentProvider.setValue(providerName);
        return documentProvider;
    }

    public MyminfinDocument toMyminfinDocument(DocumentDescription documentDescription) {
        List<Object> children = documentDescription.getChildren();
        Instant documentDate = documentDescription.getDocumentDate();
        List<String> documentGroups = documentDescription.getDocumentGroups();
        DocumentKey documentKey = documentDescription.getDocumentKey();
        String documentTitle = documentDescription.getDocumentTitle();
        String status = documentDescription.getStatus();
        String typeTechnicalId = documentDescription.getTypeTechnicalId();
        String year = documentDescription.getYear();

        MyminfinDocumentKey myminfinDocumentKey = toMyminfinDocumentKey(documentKey);
        MyminfinDocument myminfinDocument = new MyminfinDocument();
        myminfinDocument.setDocumentDate(documentDate.atZone(ZoneId.systemDefault()).toLocalDate());
        myminfinDocument.setDocumentGroups(documentGroups);
        myminfinDocument.setDocumentKey(myminfinDocumentKey);
        myminfinDocument.setDocumentTitle(documentTitle);
        myminfinDocument.setStatus(status);
        myminfinDocument.setTypeTechnicalId(typeTechnicalId);
        Optional.ofNullable(year)
                .map(Integer::parseInt)
                .ifPresent(myminfinDocument::setDocumentYear);

        return myminfinDocument;
    }

    public MyminfinDocumentKey toMyminfinDocumentKey(DocumentKey documentKey) {
        Map<String, String> identifiers = documentKey.getIdentifiers();
        String documentSelectorTypeKey = documentKey.getDocumentSelectorTypeKey();
        String providerName = documentKey.getProviderName();

        MyminfinDocumentKey myminfinDocumentKey = new MyminfinDocumentKey();
        myminfinDocumentKey.setDocumentSelectorTypeKey(documentSelectorTypeKey);
        myminfinDocumentKey.setIdentifiers(identifiers);
        myminfinDocumentKey.setProviderName(providerName);
        return myminfinDocumentKey;
    }


    public DocumentKey toDocumentKey(MyminfinDocumentKey myminfinDocumentKey) {
        Map<String, String> identifiers = myminfinDocumentKey.getIdentifiers();
        String documentSelectorTypeKey = myminfinDocumentKey.getDocumentSelectorTypeKey();
        String providerName = myminfinDocumentKey.getProviderName();

        DocumentKey documentKey = new DocumentKey();
        documentKey.setDocumentSelectorTypeKey(documentSelectorTypeKey);
        documentKey.setIdentifiers(identifiers);
        documentKey.setProviderName(providerName);
        return documentKey;
    }
}
