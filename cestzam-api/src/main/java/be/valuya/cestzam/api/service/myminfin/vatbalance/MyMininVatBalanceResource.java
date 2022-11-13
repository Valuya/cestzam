package be.valuya.cestzam.api.service.myminfin.vatbalance;

import be.valuya.cestzam.api.util.ApiTagNames;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.Closeable;

@Tag(name = ApiTagNames.TAG_SERVICE)
@Path("/cestzam/service/myminfin/vatbalance")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MyMininVatBalanceResource extends Closeable {

    @POST
    @Path("current")
    MyminfinCurrentVatBalance getCurrentVatBalance(MyminfinVatBalanceSearch vatBalanceSearch);

}
