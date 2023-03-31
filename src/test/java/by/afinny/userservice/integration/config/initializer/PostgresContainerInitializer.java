package by.afinny.userservice.integration.config.initializer;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String DB_NAME = "user-service";
    private static final String DB_USERNAME = "myuser";
    private static final String DB_PASSWORD = "123";

    private static final String POSTGRES_VERSION = "latest";
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:" + POSTGRES_VERSION);

    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USERNAME)
            .withPassword(DB_PASSWORD)
            .withExposedPorts(5432)
            .withInitScript("schema-postgres.sql");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        postgres.start();
        TestPropertyValues.of(
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword(),
                "spring.datasource.driver-class-name=" + postgres.getDriverClassName()
        ).applyTo(applicationContext.getEnvironment());
    }
}
