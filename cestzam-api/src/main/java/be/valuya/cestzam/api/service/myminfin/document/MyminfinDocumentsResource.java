package be.valuya.cestzam.api.service.myminfin.document;

import be.valuya.cestzam.api.util.ApiTagNames;
import be.valuya.cestzam.api.util.ResultPage;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.Closeable;
import java.io.InputStream;

@Tag(name = ApiTagNames.TAG_SERVICE)
@Path("/cestzam/service/myminfin/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MyminfinDocumentsResource extends Closeable {

    @POST
    @Path("providers")
    ResultPage<MyminfinDocumentProvider> searchDocumentProviders(MyminfinDocumentsProvidersSearch providersSearch);

    @POST
    @Path("search")
    ResultPage<MyminfinDocument> searchDocuments(MyminfinDocumentsSearch documentsSearch);

    @POST
    @Path("download")
    InputStream downloadDocument(MyminfinDocumentsDownloadRequest downloadRequest);


}
