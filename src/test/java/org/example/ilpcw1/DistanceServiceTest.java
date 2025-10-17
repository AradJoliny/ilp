package org.example.ilpcw1;

import org.example.ilpcw1.model.LngLat;
import org.example.ilpcw1.services.DistanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DistanceServiceTest {

    private DistanceService distanceService;

    @BeforeEach
    void setUp() {
        distanceService = new DistanceService();
    }

    @Test
    void calculateDistance_returnsEuclideanDistance() {
        double d = distanceService.calculateDistance(1.0, 2.0, 2.0, 3.0);
        double expected = Math.sqrt(2.0);
        assertEquals(expected, d, 1e-12);
    }

    @Test
    void isClose_returnsTrueWhenWithinThreshold_andFalseWhenNot() {
        // within 0.00015 threshold
        assertTrue(distanceService.isClose(1.0, 2.0, 1.0001, 2.0001));
        // far apart
        assertFalse(distanceService.isClose(1.0, 2.0, 2.0, 3.0));
    }

    @Test
    void calculateNextPosition_movesByStep_forCardinalAngle() {
        LngLat next = distanceService.calculateNextPosition(1.0, 2.0, 90.0);
        // Expect longitude unchanged, latitude increased by 0.00015
        assertEquals(1.0, next.getLng(), 1e-9);
        assertEquals(2.00015, next.getLat(), 1e-9);

        LngLat test = distanceService.calculateNextPosition(1.0, 1.0, 90.0);
        System.out.println(test.getLng());
        System.out.println(test.getLat());
    }

    @Test
    void isPointInRegion_detectsInsideAndOutside() {
        // square: (1,2) -> (2,2) -> (2,3) -> (1,2) (closed)
        double[] xpoints = {1.0, 2.0, 2.0, 1.0};
        double[] ypoints = {2.0, 2.0, 3.0, 2.0};
        // point inside
        assertTrue(distanceService.isPointInRegion(1.5, 2.5, xpoints, ypoints));

        // point outside
        assertFalse(distanceService.isPointInRegion(0.0, 0.0, xpoints, ypoints));
    }
}
