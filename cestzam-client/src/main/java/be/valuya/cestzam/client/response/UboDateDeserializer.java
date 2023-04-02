package be.valuya.cestzam.client.response;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UboDateDeserializer implements JsonbDeserializer<LocalDate> {
    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        final String str = parser.getString();
        if (str == null || str.isBlank()) {
            return null;
        }
        return LocalDate.parse(str, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
