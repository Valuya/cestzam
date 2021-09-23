package be.valuya.cestzam.client.myminfin.rest.documents;

import be.valuya.cestzam.client.myminfin.rest.MyminfinRestConstants;
import lombok.Getter;
import lombok.Setter;

import javax.json.bind.annotation.JsonbDateFormat;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class DocumentDescription {
    private List<Object> children;
    @JsonbDateFormat(value = MyminfinRestConstants.DATE_FORMAT)
    private Instant documentDate;
    private List<String> documentGroups;
    private DocumentKey documentKey;
    private String documentTitle;
    private String status;
    private String typeTechnicalId;
    private String year;
}
