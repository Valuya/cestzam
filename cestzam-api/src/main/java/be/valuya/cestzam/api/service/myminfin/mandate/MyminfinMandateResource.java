package be.valuya.cestzam.api.service.myminfin.mandate;

import be.valuya.cestzam.api.service.myminfin.AuthenticatedMyminfinContext;
import be.valuya.cestzam.api.util.ApiTagNames;
import be.valuya.cestzam.api.util.ResultPage;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.Closeable;

@Tag(name = ApiTagNames.TAG_SERVICE)
@Path("/cestzam/service/myminfin/mandate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MyminfinMandateResource extends Closeable {

    @POST
    @Path("/list")
    ResultPage<MyMinfinMandate> listMandates(MyminfinMandateSearch mandateSearch);

    @POST
    @Path("/activate")
    AuthenticatedMyminfinContext activateMandate(MandateActivationRequest mandateActivationRequest);

    @POST
    @Path("/deactivate")
    AuthenticatedMyminfinContext deactivateMandate(AuthenticatedMyminfinContext myminfinContext);

}
