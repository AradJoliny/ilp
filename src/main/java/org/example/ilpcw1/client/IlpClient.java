package org.example.ilpcw1.client;

import org.example.ilpcw1.dto.*;
import org.example.ilpcw1.services.AvailabilityService;
import org.example.ilpcw1.services.DispatchAggregationService;
import org.example.ilpcw1.services.OperatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

@Component
public class IlpClient {

    private static final Logger log = LoggerFactory.getLogger(IlpClient.class);

    @Value("${ILP_ENDPOINT:https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net}")
    private String ilpEndpoint;

    private final OperatorService operatorService;
    private final AvailabilityService availabilityService;
    private final DispatchAggregationService dispatchAggregationService;


    // Constructor must come before methods
    public IlpClient(OperatorService operatorService, AvailabilityService availabilityService, DispatchAggregationService dispatchAggregationService) {
        this.operatorService = operatorService;
        this.availabilityService = availabilityService;
        this.dispatchAggregationService = dispatchAggregationService;
    }

    public List<String> getDronesWithCooling(boolean state) {
        String url = ilpEndpoint;
        if (!url.endsWith("/")) url += "/";
        url += "drones";

        RestTemplate rest = new RestTemplate();
        try {
            log.info("Calling ILP (list) URL: {}", url);
            ResponseEntity<DroneDTO[]> response = rest.getForEntity(url, DroneDTO[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Unexpected ILP response: status={} bodyNull={}", response.getStatusCode(), response.getBody() == null);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected response from ILP service");
            }

            List<String> result = new java.util.ArrayList<>();
            for (DroneDTO d : response.getBody()) {
                if (d != null && d.getCapability() != null && d.getCapability().isCooling() == state) {
                    result.add(d.getId());
                }
            }
            return result;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call ILP for list: {}", url, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call ILP service", e);
        }
    }


    public DroneDTO getDroneDetails(String id) {
        String url = ilpEndpoint;
        if (!url.endsWith("/")) url += "/";
        url += "drones";

        RestTemplate rest = new RestTemplate();
        try {
            log.info("Calling ILP (list) URL: {}", url);
            ResponseEntity<DroneDTO[]> response = rest.getForEntity(url, DroneDTO[].class);
            log.info("ILP (list) responded: {}", response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Unexpected ILP response: status={} bodyNull={}", response.getStatusCode(), response.getBody() == null);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected response from ILP service");
            }

            for (DroneDTO d : response.getBody()) {
                if (d != null && id != null && id.equals(d.getId())) {
                    return d;
                }
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Drone not found");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call ILP for list: {}", url, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call ILP service", e);
        }
    }

    public List<DroneDTO> getAllDrones() {
        String url = ilpEndpoint;
        if (!url.endsWith("/")) url += "/";
        url += "drones";

        RestTemplate rest = new RestTemplate();

        try {
            log.info("Calling ILP (list) URL: {}", url);
            ResponseEntity<DroneDTO[]> response = rest.getForEntity(url, DroneDTO[].class);
            log.info("ILP (list) responded: {}", response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Unexpected ILP response: status={} bodyNull={}", response.getStatusCode(), response.getBody() == null);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected response from ILP service");
            }

            DroneDTO[] arr = response.getBody();
            List<DroneDTO> list = new ArrayList<>();
            if (arr != null) {
                list.addAll(Arrays.asList(arr));
            }

            return list;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call ILP for list: {}", url, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call ILP service", e);
        }
    }

    public List<String> getDronesWithAttribute(String attribute, String attributeValue) {
        String url = ilpEndpoint;
        if (!url.endsWith("/")) url += "/";
        url += "drones";

        RestTemplate rest = new RestTemplate();

        try {
            ResponseEntity<DroneDTO[]> response = rest.getForEntity(url, DroneDTO[].class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Unexpected ILP response: status={} bodyNull={}", response.getStatusCode(), response.getBody() == null);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected response from ILP service");
            }

            DroneDTO[] arr = response.getBody();
            return operatorService.matchAttributes(arr, attribute, attributeValue);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call ILP for list: {}", url, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call ILP service", e);
        }
    }

    public List<ServicePointDronesDTO.DroneWithAvailabilityDTO> getDroneTimes() {
        String url = ilpEndpoint;
        if (!url.endsWith("/")) url += "/";
        url += "drones-for-service-points";

        RestTemplate rest = new RestTemplate();

        try {
            // First get the raw JSON as String to debug
            ResponseEntity<String> rawResponse = rest.getForEntity(url, String.class);
            log.info("Raw JSON response: {}", rawResponse.getBody());

            // Then try to deserialize
            ResponseEntity<ServicePointDronesDTO[]> response = rest.getForEntity(url, ServicePointDronesDTO[].class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Unexpected ILP response: status={} bodyNull={}", response.getStatusCode(), response.getBody() == null);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected response from ILP service");
            }

            List<ServicePointDronesDTO.DroneWithAvailabilityDTO> allDrones = new ArrayList<>();
            ServicePointDronesDTO[] arr = response.getBody();
            if (arr != null) {
                for (ServicePointDronesDTO sp : arr) {
                    if (sp != null && sp.getDrones() != null) {
                        allDrones.addAll(sp.getDrones());
                    }
                }
            }
            return allDrones;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call ILP for list: {}", url, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call ILP service", e);
        }
    }


    public List<String> getDronesWithAttributes(List<QueryAttributeDTO> queries) {
        String url = ilpEndpoint;
        if (!url.endsWith("/")) url += "/";
        url += "drones";

        RestTemplate rest = new RestTemplate();
        List<String> droneIds = new ArrayList<>();

        try {
            ResponseEntity<DroneDTO[]> response = rest.getForEntity(url, DroneDTO[].class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Unexpected ILP response: status={} bodyNull={}", response.getStatusCode(), response.getBody() == null);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected response from ILP service");
            }

            DroneDTO[] arr = response.getBody();
            if (arr != null) {
                for (DroneDTO drone : arr) {
                    if (drone != null && drone.getCapability() != null) {
                        boolean matchesAll = true;

                        for (QueryAttributeDTO query : queries) {
                            if (!operatorService.matchesQuery(drone, query)) {
                                matchesAll = false;
                                break;
                            }
                        }

                        if (matchesAll) {
                            droneIds.add(drone.getId());
                        }
                    }
                }
            }
            return droneIds;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to call ILP for list: {}", url, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call ILP service", e);
        }
    }

    public List<String> queryAvailableDrones(List<MedDispatchRecDTO> reqs) {
        // Need to check:
        // 1. Drone is available (not busy)
        // 2. Drone has cooling if any request needs cooling
        // 3. Drone has sufficient capacity for weight and volume of all requests

        String url = ilpEndpoint;
        if (!url.endsWith("/")) url += "/";
        url += "drones";

        RestTemplate rest = new RestTemplate();

        // Need to check:
        // 1. Drone is available (not busy) for all request times
        // 2. Drone has cooling if any request needs cooling
        // 3. Drone has heating if any request needs heating
        // 4. Drone has sufficient capacity for total weight/volume of all requests

        // 1. Fetch all drones from ILP and filter by availability for all times
        List<DroneDTO> drones = getAllDrones();
        List<ServicePointDronesDTO.DroneWithAvailabilityDTO> droneTimes = getDroneTimes();
        List<DroneDTO> available_drones = new ArrayList<>();

        for (DroneDTO d : drones) {
            boolean allTimesAvailable = true;
            for (MedDispatchRecDTO req : reqs) {
                LocalDateTime requestTime = LocalDateTime.of(
                        LocalDate.parse(req.getDate()),
                        LocalTime.parse(req.getTime())
                );
                if (!availabilityService.isDroneAvailable(d.getId(), droneTimes, requestTime)) {
                    allTimesAvailable = false;
                    break;
                }
            }
            if (allTimesAvailable) {
                available_drones.add(d);
            }
        }
        log.info("Available drones after time check: {}", available_drones.stream().map(DroneDTO::getId).toList());

        // 2. Filter by cooling/heating requirements only
        boolean needsCooling = reqs.stream()
                .anyMatch(r -> r.getRequirements() != null && Boolean.TRUE.equals(r.getRequirements().getCooling()));
        boolean needsHeating = reqs.stream()
                .anyMatch(r -> r.getRequirements() != null && Boolean.TRUE.equals(r.getRequirements().getHeating()));
        log.info("Requirements: needsCooling={}, needsHeating={}", needsCooling, needsHeating);

        // 3. Filter available drones based on cooling/heating ONLY (not capacity)
        List<String> availableDroneIds = new ArrayList<>();
        for (DroneDTO drone : available_drones) {
            if (drone.getCapability() == null) {
                log.info("Drone {} skipped: no capability", drone.getId());
                continue;
            }

            boolean hasCooling = drone.getCapability().isCooling();
            boolean hasHeating = drone.getCapability().isHeating();

            // Only check cooling/heating requirements, NOT total capacity
            if ((!needsCooling || hasCooling) && (!needsHeating || hasHeating)) {
                availableDroneIds.add(drone.getId());
                log.info("Drone {} added: hasCooling={}, hasHeating={}", drone.getId(), hasCooling, hasHeating);
            }
        }

        log.info("Final available drone IDs: {}", availableDroneIds);
        return availableDroneIds;
    }

    public ServicePointDTO[] getServicePoints() {
        String url = ilpEndpoint;
        if (!url.endsWith("/")) url += "/";
        url += "/service-points";

        RestTemplate rest = new RestTemplate();

        try {
            ResponseEntity<ServicePointDTO[]> response = rest.getForEntity(url, ServicePointDTO[].class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected response from ILP service");
            }
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call ILP for service points: {}", url, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call ILP service", e);
        }
    }

    public ServicePointDronesDTO[] getServicePointsWithDrones() {

        String url = ilpEndpoint;
        if (!url.endsWith("/")) url += "/";
        url += "/drones-for-service-points";

        RestTemplate rest = new RestTemplate();
        try {
            ResponseEntity<ServicePointDronesDTO[]> response = rest.getForEntity(url, ServicePointDronesDTO[].class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected response from ILP service");
            }
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call ILP for service points: {}", url, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call ILP service", e);
        }
    }

    public NoFlyZoneDTO[] getNoFlyZones() {
        String url = ilpEndpoint;
        if (!url.endsWith("/")) url += "/";
        url += "/restricted-areas";

        RestTemplate rest = new RestTemplate();
        try {
            // First get the raw JSON as String to debug
            ResponseEntity<String> rawResponse = rest.getForEntity(url, String.class);
            log.info("Raw JSON response for no-fly zones: {}", rawResponse.getBody());

            // Then try to deserialize
            ResponseEntity<NoFlyZoneDTO[]> response = rest.getForEntity(url, NoFlyZoneDTO[].class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unexpected response from ILP service");
            }
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call ILP for no-fly zones: {}", url, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to call ILP service", e);
        }
    }



} // final bracket