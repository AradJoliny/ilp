package org.example.ilpcw1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.ilpcw1.controller.ServiceController;
import org.example.ilpcw1.model.LngLat;
import org.example.ilpcw1.dto.PositionDTO;
import org.example.ilpcw1.services.DistanceService;
import org.example.ilpcw1.services.PositionValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.ilpcw1.services.DistanceService;


@WebMvcTest(ServiceController.class)
class ServiceControllerTest {

    @MockitoBean
    private DistanceService distanceService;

    @MockitoBean
    private PositionValidator positionValidator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testUid() throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2538638"));
    }

    @Test
    void testDistanceTo() throws Exception {
        Map<String, Map<String, Double>> body = new HashMap<>();
        body.put("position1", Map.of("lng", 1.0, "lat", 2.0));
        body.put("position2", Map.of("lng", 2.0, "lat", 3.0));

        when(distanceService.calculateDistance(1.0, 2.0, 2.0, 3.0)).thenReturn(1.4142135623730951);

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber())
                .andExpect(content().string("1.4142135623730951"));
    }


    @Test
    void testIsCloseTo() throws Exception {
        // should return true as its within 0.00015
        Map<String, Map<String, Double>> body = new HashMap<>();
        body.put("position1", Map.of("lng", 1.0, "lat", 2.0));
        body.put("position2", Map.of("lng", 1.0001, "lat", 2.0001));
        when(distanceService.calculateDistance(1.0, 2.0, 2.0, 3.0)).thenReturn(1.4142135623730951);

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isBoolean());


        // should return false as it's outside 0.00015
        Map<String, Map<String, Double>> farBody = new HashMap<>();
        farBody.put("position1", Map.of("lng", 1.0, "lat", 2.0));
        farBody.put("position2", Map.of("lng", 2.0, "lat", 3.0));
        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(farBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testNextPosition() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("start", Map.of("lng", 1.0, "lat", 2.0));
        body.put("angle", 90.0);

        LngLat expected = new LngLat(1.0, 2.00015);

        when(distanceService.calculateNextPosition(1.0, 2.0, 90.0)).thenReturn(expected);

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").exists())
                .andExpect(jsonPath("$.lat").exists());
    }


    @Test
    void testIsInRegion() throws Exception {
        Map<String, Double> position = Map.of("lng", 1.0, "lat", 2.0);
        List<Map<String, Double>> vertices = Arrays.asList(
                Map.of("lng", 1.0, "lat", 2.0),
                Map.of("lng", 2.0, "lat", 2.0),
                Map.of("lng", 2.0, "lat", 3.0),
                Map.of("lng", 1.0, "lat", 2.0) // closed region
        );
        Map<String, Object> region = new HashMap<>();
        region.put("name", "test");
        region.put("vertices", vertices);

        Map<String, Object> body = new HashMap<>();
        body.put("position", position);
        body.put("region", region);

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isBoolean());
    }
}
