package be.valuya.cestzam.api.service.myminfin;

import be.valuya.cestzam.api.util.ApiTagNames;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.Closeable;

@Tag(name = ApiTagNames.TAG_SERVICE)
@Path("/cestzam/service/myminfin/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MyminfinLoginResource extends Closeable {

    @POST
    MyMinfinLoginResponse startLogin();

    @POST
    @Path("complete")
    AuthenticatedMyminfinContext completeLogin(MyminfinCompleteLoginRequest completeLoginRequest);

}
