package be.valuya.cestzam.api.client.domain;

import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Optional;

@Setter
public class MyminfinDocumentFilter {

    @NonNull
    private String provider;
    private LocalDate documentDateFrom;
    private LocalDate documentDateTo;
    private Integer documentYear;

    public Optional<String> getProviderOptional() {
        return Optional.ofNullable(provider);
    }

    public Optional<LocalDate> getFromDateOptional() {
        return Optional.ofNullable(documentDateFrom);
    }

    public Optional<LocalDate> getToDateOptional() {
        return Optional.ofNullable(documentDateTo);
    }

    public Optional<Integer> getDocumentYearOptional() {
        return Optional.ofNullable(documentYear);
    }
}
