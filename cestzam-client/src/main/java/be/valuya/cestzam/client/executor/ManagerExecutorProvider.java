package be.valuya.cestzam.client.executor;

import be.valuya.cestzam.api.Cestzam;
import be.valuya.cestzam.client.CestzamClientConfig;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.concurrent.Executor;

@ApplicationScoped
public class ManagerExecutorProvider {

    @Inject
    private CestzamClientConfig cestzamClientConfig;
    @Resource
    private ManagedExecutorService managedExecutorService;

    @Produces
    @Cestzam
    public Executor getManagedExecutorNullable() {
        Boolean managedExecutorEnabled = cestzamClientConfig.getManagedExecutorEnabled();
        if (!managedExecutorEnabled) {
            return null;
        }

        return managedExecutorService;
    }
}
