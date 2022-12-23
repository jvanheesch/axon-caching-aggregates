package io.github.jvanheesch.axon.aggregate.caching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExampleIT {
    private final GenericContainer<?> AXON_SERVER = new GenericContainer<>("axoniq/axonserver:4.6.7")
            .withExposedPorts(8124)
            .waitingFor(new LogMessageWaitStrategy()
                    .withRegEx(".*Started AxonServer.*"));
    private final GenericContainer<?> APP_INSTANCE_1 = new GenericContainer<>("docker.io/library/axon-aggregate-caching:1.0-SNAPSHOT")
            .withExposedPorts(8080)
            .waitingFor(new LogMessageWaitStrategy()
                    .withRegEx(".*Started Application.*"));
    private final GenericContainer<?> APP_INSTANCE_2 = new GenericContainer<>("docker.io/library/axon-aggregate-caching:1.0-SNAPSHOT")
            .withExposedPorts(8080)
            .waitingFor(new LogMessageWaitStrategy()
                    .withRegEx(".*Started Application.*"));

    private String host;
    private Integer port1;
    private Integer port2;

    @BeforeEach
    void setup() {
        AXON_SERVER.start();
        APP_INSTANCE_1.addEnv("axon.axonserver.servers", String.format("host.docker.internal:%d", AXON_SERVER.getMappedPort(8124)));
        APP_INSTANCE_2.addEnv("axon.axonserver.servers", String.format("host.docker.internal:%d", AXON_SERVER.getMappedPort(8124)));
        APP_INSTANCE_1.start();
        APP_INSTANCE_2.start();

        host = "localhost";
        port1 = APP_INSTANCE_1.getMappedPort(8080);
        port2 = APP_INSTANCE_2.getMappedPort(8080);
    }

    @Test
    void testStaleAggregateRefusesValidCommand() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(String.format("http://%s:%s?id=1", host, port1), null, String.class);
        restTemplate.postForObject(String.format("http://%s:%s/1/simple?version=0", host, port1), null, String.class);
        restTemplate.postForObject(String.format("http://%s:%s/1/simple?version=1", host, port2), null, String.class);
        assertThatThrownBy(() -> restTemplate.postForObject(String.format("http://%s:%s/1/simple?version=2", host, port1), null, String.class))
                // not really a server error but simpler this way
                .isInstanceOf(HttpServerErrorException.class)
                .hasMessage("500 : \"MyOptimisticLockingException: Expected version = 2, actual version = 1\"");
    }

    @Test
    void testAxonServerRefusesEventFromStaleAggregate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(String.format("http://%s:%s?id=1", host, port1), null, String.class);
        restTemplate.postForObject(String.format("http://%s:%s/1/simple?version=0", host, port1), null, String.class);
        restTemplate.postForObject(String.format("http://%s:%s/1/simple?version=1", host, port2), null, String.class);
        assertThatThrownBy(() -> restTemplate.postForObject(String.format("http://%s:%s/1/simple?version=1", host, port1), null, String.class))
                .isInstanceOf(HttpServerErrorException.class)
                .hasMessage("500 : \"OUT_OF_RANGE: [AXONIQ-2000] Invalid sequence number 2 for aggregate 1, expected 3\"");
    }

    @Test
    void testNoStaleAggregatesWithAxonServerCommandBus() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(String.format("http://%s:%s?id=1", host, port1), null, String.class);
        restTemplate.postForObject(String.format("http://%s:%s/1/axon-server?version=0", host, port1), null, String.class);
        restTemplate.postForObject(String.format("http://%s:%s/1/axon-server?version=1", host, port2), null, String.class);
        restTemplate.postForObject(String.format("http://%s:%s/1/axon-server?version=2", host, port1), null, String.class);
    }
}
