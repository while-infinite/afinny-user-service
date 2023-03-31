package by.afinny.userservice.integration.config.initializer;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public class KafkaContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String KAFKA_VERSION = "latest";
    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:" + KAFKA_VERSION);

    @Container
    public static final KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE)
        .withEmbeddedZookeeper();

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        kafka.start();
        TestPropertyValues.of(
            "kafka.bootstrap-servers=" + kafka.getBootstrapServers(),
            "spring.kafka.bootstrap-servers=" + kafka.getBootstrapServers()
        ).applyTo(applicationContext.getEnvironment());
    }
}
