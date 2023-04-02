package be.valuya.cestzam.ws.util;

import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.QueryParam;

@Getter
@Setter
public class ConfigParam {

    @QueryParam("timeout")
    private Long timeout;

    @QueryParam("language")
    private String language;
}
