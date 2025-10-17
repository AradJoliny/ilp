package org.example.ilpcw1.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DistanceControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void distanceTo_returnsCorrectDistance() {
        Map<String, Map<String, Double>> requestBody = new HashMap<>();
        Map<String, Double> position1 = new HashMap<>();
        position1.put("lng", 1.0);
        position1.put("lat", 2.0);
        Map<String, Double> position2 = new HashMap<>();
        position2.put("lng", 2.0);
        position2.put("lat", 3.0);
        requestBody.put("position1", position1);
        requestBody.put("position2", position2);

        ResponseEntity<Double> response = restTemplate.postForEntity(
                "/api/v1/distanceTo",
                requestBody,
                Double.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isCloseTo(Math.sqrt(2.0), offset(1e-9));
    }

    @Test
    void isInRegion_returnsTrue_whenPointInside() {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Double> position = new HashMap<>();
        position.put("lng", -3.1900);
        position.put("lat", 55.9450);

        Map<String, Object> region = new HashMap<>();
        region.put("name", "central");
        Map<String, Double>[] vertices = new Map[]{
                Map.of("lng", -3.192473, "lat", 55.946233),
                Map.of("lng", -3.184319, "lat", 55.946233),
                Map.of("lng", -3.184319, "lat", 55.942617),
                Map.of("lng", -3.192473, "lat", 55.942617),
                Map.of("lng", -3.192473, "lat", 55.946233)
        };
        region.put("vertices", vertices);

        requestBody.put("position", position);
        requestBody.put("region", region);

        ResponseEntity<Boolean> response = restTemplate.postForEntity(
                "/api/v1/isInRegion",
                requestBody,
                Boolean.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isTrue();
    }

    @Test
    void isInRegion_returnsFalse_whenPointOutside() {
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Double> position = new HashMap<>();
        position.put("lng", 0.0);
        position.put("lat", 0.0);

        Map<String, Object> region = new HashMap<>();
        region.put("name", "central");
        Map<String, Double>[] vertices = new Map[]{
                Map.of("lng", -3.192473, "lat", 55.946233),
                Map.of("lng", -3.184319, "lat", 55.946233),
                Map.of("lng", -3.184319, "lat", 55.942617),
                Map.of("lng", -3.192473, "lat", 55.942617),
                Map.of("lng", -3.192473, "lat", 55.946233)
        };
        region.put("vertices", vertices);

        requestBody.put("position", position);
        requestBody.put("region", region);

        ResponseEntity<Boolean> response = restTemplate.postForEntity(
                "/api/v1/isInRegion",
                requestBody,
                Boolean.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isFalse();
    }
}
