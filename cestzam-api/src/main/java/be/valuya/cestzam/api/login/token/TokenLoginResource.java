package be.valuya.cestzam.api.login.token;


import be.valuya.cestzam.api.context.AuthenticatedCestzamContext;
import be.valuya.cestzam.api.util.ApiTagNames;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.Closeable;

@Tag(name = ApiTagNames.TAG_LOGIN_FLOW)
@Path("/cestzam/login/token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TokenLoginResource extends Closeable {

    @POST
    TokenLoginResponse login(TokenLoginRequest request);

    @POST
    @Path("verificationCode")
    AuthenticatedCestzamContext verifyCode(TokenCodeRequest request);

    @POST
    @Path("withCodes")
    AuthenticatedCestzamContext loginAndVerifyCode(TokenCodesLoginRequest request);

}
