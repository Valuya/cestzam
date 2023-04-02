package be.valuya.cestzam.api.service.myminfin.ubo;

import be.valuya.cestzam.api.util.ApiTagNames;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.Closeable;
import java.io.InputStream;

@Tag(name = ApiTagNames.TAG_SERVICE)
@Path("/cestzam/service/myminfin/ubo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MyminfinUboResource extends Closeable {

    @POST
    @Path("companies/search")
    UboCompanySearchResult searchCompanies(UboCompanySearch companySearch);

    @POST
    @Path("company/{companyId}/composition")
    UboCompanyCompositionNode getCompanyComposition(@PathParam("companyId") String companyId, UboRequestContext requestContext);

    @POST
    @Path("company/{companyId}/confirm")
    UboCompany confirmCompanyComposition(@PathParam("companyId") String companyId, UboRequestContext requestContext);

    @POST
    @Path("document/{documentId}/content")
    @Produces(MediaType.WILDCARD)
    InputStream getDocumentContent(@PathParam("documentId") Long documentId, UboRequestContext requestContext);

}
