package by.afinny.userservice.integration.config.initializer;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String MONGO_VERSION = "5.0.12";
    private static final DockerImageName MONGO_IMAGE = DockerImageName.parse("mongo:" + MONGO_VERSION);

    public static final MongoDBContainer mongo = new MongoDBContainer(MONGO_IMAGE);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        mongo.start();
        TestPropertyValues.of("spring.data.mongodb.uri=" + mongo.getReplicaSetUrl())
                .applyTo(applicationContext.getEnvironment());
    }
}
