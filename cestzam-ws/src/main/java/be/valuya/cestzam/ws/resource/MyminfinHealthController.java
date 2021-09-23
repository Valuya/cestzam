package be.valuya.cestzam.ws.resource;

import be.valuya.cestzam.api.service.ServiceHealthCheckResponse;
import be.valuya.cestzam.api.service.myminfin.MyminfinHealthResource;
import be.valuya.cestzam.client.request.CestzamRequestService;
import be.valuya.cestzam.ws.health.CestzamHealthService;
import be.valuya.cestzam.ws.util.ConfigParam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import java.io.IOException;
import java.util.Optional;

@ApplicationScoped
public class MyminfinHealthController implements MyminfinHealthResource {

    @Inject
    private CestzamHealthService healthService;
    @Inject
    private CestzamRequestService cestzamRequestService;
    @BeanParam
    private ConfigParam configParam;

    @Override
    public ServiceHealthCheckResponse checkHealth() {
        Optional.ofNullable(configParam.getTimeout())
                .ifPresent(cestzamRequestService::setClientTimeout);

        ServiceHealthCheckResponse healthCheckResponse = healthService.checkMyminfinHealth();
        return healthCheckResponse;
    }

    @Override
    public void close() throws IOException {

    }
}
