package example;

import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class MetricsTest {

    @Test
    void testMetricsEndpointWithSerializationModule(@Client("/") HttpClient httpClient) {
        var response = httpClient.toBlocking().exchange("/metrics/", Map.class);
        Map result = response.body();
        assertEquals(21, Runtime.version().feature());

        Assertions.assertNotNull(result);

        Object namesObject = result.get("names");
        Assertions.assertTrue(namesObject instanceof List<?>);
        List<String> names = (List<String>) namesObject;
        // this exact test is in the micronaut-micrometer:test-suite:micronaut-serializataion
        // it passes for jdk17 and fails for jdk21 (only "executor" exists)
        // but it passes for jdk21 here.
        Assertions.assertTrue(names.containsAll(List.of("executor.completed", "executor.queued")));
    }

    @Test
    void testExpectedMeters(MeterRegistry meterRegistry) {
        assertEquals(21, Runtime.version().feature());

        List<String> names = meterRegistry.getMeters().stream()
                .map(meter -> meter.getId().getName())
                .toList();
        ArrayList<String> sortedNames = new ArrayList<>(names);
        Collections.sort(sortedNames);

        // check that a subset of expected meters exist
        assertTrue(names.contains("jvm.memory.max"));
        assertTrue(names.contains("process.uptime"));
        assertTrue(names.contains("system.cpu.usage"));
        assertTrue(names.contains("process.files.open"));

        assertTrue(names.contains("executor.completed"));
        assertTrue(names.contains("executor.queued"));

    }
}
