package be.valuya.cestzam.ws.converter;

import be.valuya.cestzam.api.login.Capacity;
import be.valuya.cestzam.client.czam.CzamCapacity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CzamCapacityConverter {

    public CzamCapacity toCzamCapcity(Capacity capacity) {
        switch (capacity) {
            case CITIZEN:
                return CzamCapacity.CITIZEN;
            case ENTERPRISE:
                return CzamCapacity.ENTERPRISE;
            default:
                throw new IllegalArgumentException(capacity.name());
        }
    }

    public Capacity toCapacity(CzamCapacity capacity) {
        switch (capacity) {
            case CITIZEN:
                return Capacity.CITIZEN;
            case ENTERPRISE:
                return Capacity.ENTERPRISE;
            default:
                throw new IllegalArgumentException(capacity.name());
        }
    }
}
