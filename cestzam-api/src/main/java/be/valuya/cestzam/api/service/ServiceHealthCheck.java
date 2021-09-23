package be.valuya.cestzam.api.service;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceHealthCheck {
    private String checkName;
    private boolean up;
    private String message;
}
