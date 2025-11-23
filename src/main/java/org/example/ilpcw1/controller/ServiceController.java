package org.example.ilpcw1.controller;

import org.apache.coyote.Response;
import org.example.ilpcw1.dto.*;
import org.example.ilpcw1.model.LngLat;
import org.example.ilpcw1.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.View;
import org.example.ilpcw1.client.IlpClient;
import org.springframework.http.HttpStatus;

import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.util.*;


@RestController
@RequestMapping("/api/v1")
public class ServiceController {

    @Autowired
    private DistanceService distanceService;
    @Autowired
    private PositionValidator positionValidator;
    @Autowired
    private IlpClient IlpClient;
    @Autowired
    private DispatchAggregationService dispatchAggregationService;
    @Autowired
    private AvailabilityService availabilityService;
    @Autowired
    private PathService pathService;
    @Autowired
    private DeliveryPathService deliveryPathService;

    /**
     * Handles GET requests to "/uid" and returns the my student id as a string
     * without any further formatting.
     *
     * @return the user ID string
     */
    @GetMapping("/uid")
    public String uid() {
        return "s2538638";
    }


    /**
     * Handles POST requests to "/distanceTo" and calculates the Euclidean distance
     * between two geographic positions provided in the request body.
     *
     * The request body must contain two maps: "position1" and "position2", each with
     * "lng" (longitude) and "lat" (latitude) values.
     * Validates that latitude is between -90 and 90, and longitude is between -180 and 180.
     * Returns HTTP 400 if coordinates are invalid.
     *
     * @param body a map containing "position1" and "position2" with their coordinates
     * @return ResponseEntity containing the calculated distance as a Double, or HTTP 400 on error
     */
    @PostMapping("/distanceTo")
    public ResponseEntity<Double> distanceTo(@RequestBody PositionPairDTO body) {
        try {
            if (body == null || body.getPosition1() == null || body.getPosition2() == null) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Missing positions");
            }

            PositionDTO pos1 = body.getPosition1();
            PositionDTO pos2 = body.getPosition2();

            // Use the helper method for validation
            positionValidator.validatePosition(pos1);
            positionValidator.validatePosition(pos2);

            // Calculation
            double distance = distanceService.calculateDistance(
                    pos1.getLng(), pos1.getLat(),
                    pos2.getLng(), pos2.getLat()
            );
            return ResponseEntity.ok(distance);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }



    /**
     * Handles POST requests to "/isCloseTo" and determines if two geographic positions
     * are within a specified proximity threshold.
     *
     * The request body must contain two maps: "position1" and "position2", each with
     * "lng" (longitude) and "lat" (latitude) values.
     * Validates that latitude is between -90 and 90, and longitude is between -180 and 180.
     * Returns HTTP 400 if coordinates are invalid.
     * Returns true if the Euclidean distance between the positions is less than 0.00015.
     *
     * @param body a map containing "position1" and "position2" with their coordinates
     * @return ResponseEntity containing true if positions are close, false otherwise, or HTTP 400 on error
     */
    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody PositionPairDTO body) {
        try {
            PositionDTO pos1 = body.getPosition1();
            PositionDTO pos2 = body.getPosition2();

            positionValidator.validatePosition(pos1);
            positionValidator.validatePosition(pos2);

            boolean isClose = distanceService.isClose(
                    pos1.getLng(), pos1.getLat(),
                    pos2.getLng(), pos2.getLat()
            );
            return ResponseEntity.ok(isClose);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Handles POST requests to "/nextPosition" and calculates the next geographic position
     * based on the provided starting coordinates and movement angle.
     *
     * The request body must be a PositionDTO containing "lng" (longitude), "lat" (latitude),
     * and "angle" (direction in degrees).
     * Validates that latitude is between -90 and 90, longitude is between -180 and 180,
     * and angle is between 0 and 360. Returns HTTP 400 if any value is invalid.
     * Calculates the next position by moving a fixed step (0.00015) in the given angle.
     *
     * @param body the PositionDTO containing starting coordinates and movement angle
     * @return ResponseEntity containing the new LngLat position, or HTTP 400 on error
     */
    @PostMapping("/nextPosition")
    public ResponseEntity<LngLat> nextPosition(@RequestBody NextPositionRequest body) {
        try {
            if (body == null || body.getStart() == null) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Request body and start position cannot be null");
            }

            // Validate position coordinates
            positionValidator.validatePosition(body.getStart());

            // Validate angle
            positionValidator.validateAngle(body.getAngle());

            // Calculation
            LngLat position = distanceService.calculateNextPosition(
                    body.getStart().getLng(),
                    body.getStart().getLat(),
                    body.getAngle()
            );
            return ResponseEntity.ok(position);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    private boolean isPointOnEdge(double px, double py, double[] xpoints, double[] ypoints) {
        for (int i = 0; i < xpoints.length - 1; i++) {
            double x1 = xpoints[i], y1 = ypoints[i];
            double x2 = xpoints[i + 1], y2 = ypoints[i + 1];
            // Check if point (px, py) is on the line segment (x1, y1)-(x2, y2)
            double cross = (px - x1) * (y2 - y1) - (py - y1) * (x2 - x1);
            if (Math.abs(cross) < 1e-10) { // close to zero: collinear
                double dot = (px - x1) * (px - x2) + (py - y1) * (py - y2);
                if (dot <= 0) return true;
            }
        }
        return false;
    }

    /**
     * Handles POST requests to "/isInRegion" and determines if a geographic position
     * is inside a specified polygonal region.
     *
     * The request body must contain a "position" map with "lng" (longitude) and "lat" (latitude),
     * and a "region" map with a "name" and a list of "vertices" (each vertex is a map with "lng" and "lat").
     * Validates that all coordinates are within valid ranges and that the region is closed (first and last vertices match).
     * Returns HTTP 400 if any value is invalid or the region is not closed.
     *
     * @param body a map containing the position and region definition
     * @return ResponseEntity containing true if the position is inside the region, false otherwise, or HTTP 400 on error
     */

    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody RegionCheckDTO body) {
        try {
            if (body == null || body.getPosition() == null || body.getRegion() == null) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Missing position or region");
            }

            // Validate position
            positionValidator.validatePosition(body.getPosition());

            // Validate region
            positionValidator.validateRegion(body.getRegion());

            // Extract coordinates
            double lng = body.getPosition().getLng();
            double lat = body.getPosition().getLat();

            List<PositionDTO> vertices = body.getRegion().getVertices();
            double[] xpoints = new double[vertices.size()];
            double[] ypoints = new double[vertices.size()];

            for (int i = 0; i < vertices.size(); i++) {
                xpoints[i] = vertices.get(i).getLng();
                ypoints[i] = vertices.get(i).getLat();
            }

            // Calculation
            boolean contains = distanceService.isPointInRegion(lng, lat, xpoints, ypoints);
            return ResponseEntity.ok(contains);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // COURSEWORK 2 STARTING POINT


    @GetMapping("/dronesWithCooling/{state}")
    public ResponseEntity<List<String>> dronesWithCooling(@PathVariable boolean state) {
        try {
            List<String> ids = IlpClient.getDronesWithCooling(state);
            return ResponseEntity.ok(ids);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    @GetMapping("/droneDetails/{id}")
    public ResponseEntity<DroneDetailsResponseDTO> droneDetails(@PathVariable String id) {
        try {
            DroneDTO drone = IlpClient.getDroneDetails(id);
            CapabilityResponseDTO capability = new CapabilityResponseDTO(
                    drone.getCapability().isCooling(),
                    drone.getCapability().isHeating(),
                    drone.getCapability().getCapacity(),
                    (int) drone.getCapability().getMaxMoves(),
                    drone.getCapability().getCostPerMove(),
                    drone.getCapability().getCostInitial(),
                    drone.getCapability().getCostFinal()
            );
            DroneDetailsResponseDTO response = new DroneDetailsResponseDTO(
                    drone.getName(),
                    drone.getId(),
                    capability
            );
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }


    // Q3 (a)
    @GetMapping("/queryAsPath/{attribute}/{attribute_value}")
    public ResponseEntity<List<String>> queryAsPath(@PathVariable String attribute, @PathVariable String attribute_value) {
        try {
            List<String> droneIds = IlpClient.getDronesWithAttribute(attribute, attribute_value);
            return ResponseEntity.ok(droneIds);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    // Q3 (b)
    @PostMapping("/query")
    public ResponseEntity<List<String>> query(@RequestBody List<QueryAttributeDTO> queryAttributeDTO) {
        try {
            List<String> droneIds = IlpClient.getDronesWithAttributes(queryAttributeDTO);
            return ResponseEntity.ok(droneIds);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    // Q4
    @PostMapping("/queryAvailableDrones")
    public ResponseEntity<List<String>> queryAvailableDrones(@RequestBody List<MedDispatchRecDTO> requirements) {
        try {
            List<String> droneIds = IlpClient.queryAvailableDrones(requirements);
            return ResponseEntity.ok(droneIds);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }

    }

//    @PostMapping("/calcDeliveryPath")
//    public ResponseEntity<DeliveryPathDTO> calcDeliveryPath(@RequestBody List<MedDispatchRecDTO> dispatches) {
//        try {
//            DeliveryPathDTO result = deliveryPathService.calculateDeliveryPath(dispatches);
//            return ResponseEntity.ok(result);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<String> calcDeliveryPath(@RequestBody List<MedDispatchRecDTO> dispatches) {
        try {
            DeliveryPathDTO result = deliveryPathService.calculateDeliveryPath(dispatches);
            String geoJson = deliveryPathService.toGeoJson(result);
            return ResponseEntity.ok(geoJson);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/calcDeliveryPathAsGeoJson")
    public ResponseEntity<String> calcDeliveryPathAsGeoJson(@RequestBody List<MedDispatchRecDTO> dispatches) {
        try {
            DeliveryPathDTO result = deliveryPathService.calculateSingleDroneDeliveryPath(dispatches);
            String geoJson = deliveryPathService.toGeoJson(result);
            return ResponseEntity.ok(geoJson);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


} // Service controller defining bracket