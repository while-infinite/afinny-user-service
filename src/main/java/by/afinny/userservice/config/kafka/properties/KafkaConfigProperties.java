package by.afinny.userservice.config.kafka.properties;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
public class KafkaConfigProperties {

    private String bootstrapServers;
    private KafkaProperties kafkaProperties;
    private Boolean enabled;
    private Map<String, Topic> topics = new HashMap<>();

    @Getter
    @Setter
    public static class Topic {
        private String path;
        private boolean enabled;
    }
}
