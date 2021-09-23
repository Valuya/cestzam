package be.valuya.cestzam.ws;

import javax.annotation.security.PermitAll;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/")
@PermitAll
public class CestzamWsApplication extends Application {
}
