package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeepAlive {

    private KeepAlivePopups popups;
    private Object response;
}
