package org.example.ilpcw1.client;

import org.example.ilpcw1.dto.DroneDTO;
import org.example.ilpcw1.dto.MedDispatchRecDTO;
import org.example.ilpcw1.dto.QueryAttributeDTO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

@Component
public class IlpClient {

    private static final Logger log = LoggerFactory.getLogger(IlpClient.class);

    @Value("${ILP_ENDPOINT:https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net}")
    private String ilpEndpoint;

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

    public List<String> queryAvailableDrones(List<MedDispatchRecDTO> reqs) {
        // Need to check:
        // 1. Drone has cooling if any request needs cooling
        // 2. Drone has sufficient capacity for weight and volume of all requests

        String url = ilpEndpoint;
        if (!url.endsWith("/")) url += "/";
        url += "drones";

        RestTemplate rest = new RestTemplate();
        List<String> suitableDrones = new ArrayList<>();




    }

    private final OperatorService operatorService;

    public IlpClient(OperatorService operatorService) {
        this.operatorService = operatorService;
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



} // final bracket