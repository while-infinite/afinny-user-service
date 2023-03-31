package by.afinny.userservice.integration.config.annotation;

import by.afinny.userservice.integration.config.initializer.KafkaContainerInitializer;
import by.afinny.userservice.integration.config.initializer.PostgresContainerInitializer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = "kafka.enabled=true"
)
@Testcontainers
@ActiveProfiles("integration")
@ContextConfiguration(initializers = {PostgresContainerInitializer.class, KafkaContainerInitializer.class})
public @interface TestWithKafkaContainer {
}
