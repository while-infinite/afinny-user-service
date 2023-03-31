package by.afinny.userservice.config.kafka;

import by.afinny.userservice.config.kafka.properties.KafkaConfigProperties;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaConfigProperties.class)
@ConditionalOnProperty(value = "kafka.enabled")
public class KafkaConfig {

    private final KafkaConfigProperties config;
    private KafkaProperties kafkaProperties;
    private String BOOTSTRAP_SERVERS;

    @PostConstruct
    private void createKafkaProperties() {
        kafkaProperties = config.getKafkaProperties();
        BOOTSTRAP_SERVERS = config.getBootstrapServers();
    }

    @Bean
    public DefaultKafkaProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(getKafkaProducerProperties());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public DefaultKafkaConsumerFactory<String, Object> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(getKafkaConsumerProperties());
    }

    @Bean(name = "listenerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> factory(ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

    private Map<String, Object> getKafkaConsumerProperties() {
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerProperties.get("key.deserializer"));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerProperties.get("value.deserializer"));
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, "false");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "by.afinny.userservice.dto.kafka.EmployerEvent");
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, ErrorHandlingDeserializer.class);

        return props;
    }

    private Map<String, Object> getKafkaProducerProperties() {
        Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, producerProperties.get("key.serializer"));
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, producerProperties.get("value.serializer"));

        return props;
    }
}
