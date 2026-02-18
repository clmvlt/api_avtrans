package bzh.stack.apiavtrans.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final ZoneId PARIS_ZONE = ZoneId.of("Europe/Paris");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static class CustomZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {
        @Override
        public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateString = p.getText();
            try {
                ZonedDateTime parsed = ZonedDateTime.parse(dateString);
                return parsed.withZoneSameInstant(PARIS_ZONE);
            } catch (Exception e) {
                try {
                    LocalDateTime localDateTime = LocalDateTime.parse(dateString);
                    return localDateTime.atZone(PARIS_ZONE);
                } catch (Exception ex) {
                    throw new IOException("Unable to parse date: " + dateString, ex);
                }
            }
        }
    }

    public static class CustomZonedDateTimeSerializer extends JsonSerializer<ZonedDateTime> {
        @Override
        public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            ZonedDateTime parisTime = value.withZoneSameInstant(PARIS_ZONE);
            gen.writeString(parisTime.format(FORMATTER));
        }
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Allow large base64 strings for APK uploads (500MB max)
        mapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxStringLength(500_000_000)
                        .build()
        );

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        javaTimeModule.addDeserializer(ZonedDateTime.class, new CustomZonedDateTimeDeserializer());
        javaTimeModule.addSerializer(ZonedDateTime.class, new CustomZonedDateTimeSerializer());

        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}
