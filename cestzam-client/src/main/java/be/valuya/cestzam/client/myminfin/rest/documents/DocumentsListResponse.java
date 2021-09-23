package be.valuya.cestzam.client.myminfin.rest.documents;

import be.valuya.cestzam.client.myminfin.rest.MyminfinRestConstants;
import lombok.Getter;
import lombok.Setter;

import javax.json.bind.annotation.JsonbDateFormat;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class DocumentsListResponse {
    @JsonbDateFormat(value = MyminfinRestConstants.DATE_FORMAT)
    private Instant date;
    private List<DocumentDescription> documentDescriptionList;
    private List<Object> groupsWithError;
}
