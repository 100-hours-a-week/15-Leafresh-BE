package ktb.leafresh.backend.global.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonTimeFormatConfig {

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer customLocalTimeFormat() {
    return builder -> {
      JavaTimeModule timeModule = new JavaTimeModule();
      timeModule.addSerializer(
          LocalTime.class,
          new com.fasterxml.jackson.databind.JsonSerializer<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            @Override
            public void serialize(
                LocalTime value,
                com.fasterxml.jackson.core.JsonGenerator gen,
                com.fasterxml.jackson.databind.SerializerProvider serializers)
                throws java.io.IOException {
              gen.writeString(value.format(formatter));
            }
          });

      builder.modules(timeModule);
    };
  }
}
