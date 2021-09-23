package be.valuya.cestzam.api.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceHealthCheckResponse {
    private boolean up;
    private String serviceName;
    private String message;
    private List<ServiceHealthCheck> checks;
}
