package be.valuya.cestzam.client.myminfin.rest.ubo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KeepAlivePopups {

    private List<Object> error;
    private List<Object> success;
    private List<Object> warning;
}
