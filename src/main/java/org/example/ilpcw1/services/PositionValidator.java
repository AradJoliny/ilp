package org.example.ilpcw1.services;

import org.example.ilpcw1.dto.PositionDTO;
import org.example.ilpcw1.dto.RegionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class PositionValidator {

    /**
     * Validates that a position has valid latitude and longitude coordinates.
     *
     * @param pos the PositionDTO to validate
     * @throws ResponseStatusException if coordinates are invalid
     */
    public void validatePosition(PositionDTO pos) {
        if (pos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Position cannot be null");
        }

        Double lng = pos.getLng();
        Double lat = pos.getLat();

        if (lng == null || lat == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordinates cannot contain null");
        }

        if (lng.isNaN() || lat.isNaN() || lng.isInfinite() || lat.isInfinite()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coordinate values");
        }

        if (lat > 90 || lat < -90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid latitude");
        }

        if (lng > 180 || lng < -180) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid longitude");
        }
    }

    /**
     * Validates that an angle is valid for movement calculations.
     *
     * @param angle the angle to validate
     * @throws ResponseStatusException if angle is invalid
     */
    public void validateAngle(Double angle) {
        if (angle == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Angle cannot be null");
        }

        if (angle.isNaN() || angle.isInfinite()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid angle value");
        }

        if (angle < 0 || angle > 360) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid angle");
        }

        if (angle % 22.5 != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Angle must be divisible by 22.5");
        }
    }

    /**
     * Validates that a region is properly closed and has valid vertices.
     *
     * @param region the RegionDTO to validate
     * @throws ResponseStatusException if region is invalid
     */
    public void validateRegion(RegionDTO region) {
        if (region == null || region.getVertices() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Region cannot be null");
        }

        List<PositionDTO> vertices = region.getVertices();

        if (vertices.size() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Region must have at least 3 vertices");
        }

        // Validate all vertices
        for (PositionDTO vertex : vertices) {
            validatePosition(vertex);
        }

        PositionDTO first = vertices.get(0);
        PositionDTO last = vertices.get(vertices.size() - 1);

        if (Double.compare(first.getLng(), last.getLng()) != 0 ||
                Double.compare(first.getLat(), last.getLat()) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Region is not closed");
        }
    }

}
