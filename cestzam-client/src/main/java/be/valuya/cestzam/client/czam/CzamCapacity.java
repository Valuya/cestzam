package be.valuya.cestzam.client.czam;

import java.util.Arrays;
import java.util.Optional;

public enum CzamCapacity {
    CITIZEN("citizen"),
    ENTERPRISE("enterprise"),
    ;

    private String name;

    CzamCapacity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<CzamCapacity> parseName(String name) {
        return Arrays.stream(CzamCapacity.values())
                .filter(v -> v.getName().equalsIgnoreCase(name))
                .findAny();
    }
}
