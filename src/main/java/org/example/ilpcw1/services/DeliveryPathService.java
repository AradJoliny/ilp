package org.example.ilpcw1.services;

import org.example.ilpcw1.client.IlpClient;
import org.example.ilpcw1.dto.*;
import org.example.ilpcw1.model.LngLat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DeliveryPathService {

    @Autowired
    private DistanceService distanceService;
    @Autowired
    private AvailabilityService availabilityService;
    @Autowired
    private DispatchAggregationService dispatchAggregationService;
    @Autowired
    private PathService pathService;
    @Autowired
    private IlpClient ilpClient;

    private static final Logger log = LoggerFactory.getLogger(DeliveryPathService.class);


    public DeliveryPathDTO calculateDeliveryPath(List<MedDispatchRecDTO> dispatches) {
        try {
            // Retrieve available drones
            List<String> availableDrones = ilpClient.queryAvailableDrones(dispatches);
            ServicePointDronesDTO[] servicePoints = ilpClient.getServicePointsWithDrones();
            ServicePointDTO[] servicePointsWithLocations = ilpClient.getServicePoints();

            // Aggregate requirements
            DispatchAggregationService.AggregatedRequirements aggregated = dispatchAggregationService.aggregateRequirements(dispatches);

            // Assign dispatches to drones
            Map<String, List<MedDispatchRecDTO>> droneAssignments = assignDispatchesToDrones(dispatches, availableDrones, servicePoints, servicePointsWithLocations, aggregated);

            if (droneAssignments.isEmpty()) {
                log.info("No drones have been chosen for delivery (assignments empty) - aborting calculations.");
            } else {
                // Build compact assignment summary: droneId:[id1,id2] droneId2:[...]
                StringBuilder summary = new StringBuilder();
                for (Map.Entry<String, List<MedDispatchRecDTO>> e : droneAssignments.entrySet()) {
                    summary.append(e.getKey()).append(":[");
                    List<MedDispatchRecDTO> list = e.getValue();
                    for (int i = 0; i < list.size(); i++) {
                        summary.append(list.get(i).getId());
                        if (i < list.size() - 1) summary.append(",");
                    }
                    summary.append("] ");
                }
                log.info("Selected drones before calculations: {}. Assignment summary: {}",
                        droneAssignments.keySet(), summary.toString().trim());
            }

            // Fetch no-fly zones
            NoFlyZoneDTO[] noFlyZonesResponse = ilpClient.getNoFlyZones();
            List<NoFlyZoneDTO> noFlyZones = noFlyZonesResponse != null ? Arrays.asList(noFlyZonesResponse) : Collections.emptyList();

            // Calculate paths and build result
            DeliveryPathDTO result = new DeliveryPathDTO();
            List<DronePathDTO> dronePaths = new ArrayList<>();
            double totalCost = 0.0;
            int totalMoves = 0;

            for (Map.Entry<String, List<MedDispatchRecDTO>> entry : droneAssignments.entrySet()) {
                String droneId = entry.getKey();
                List<MedDispatchRecDTO> assignedDispatches = entry.getValue();

                // Aggregate maxCost and calculate maxMoves

                DroneDTO droneDetails = ilpClient.getDroneDetails(droneId);
                double costPerMove = droneDetails.getCapability().getCostPerMove();
                double takeoffCost = droneDetails.getCapability().getCostInitial();
                double returnCost = droneDetails.getCapability().getCostFinal();
                double maxMoves = droneDetails.getCapability().getMaxMoves();

                log.info("Drone {} details: costPerMove={}, takeoffCost={}, returnCost={}, maxMoves={}",
                        droneId, costPerMove, takeoffCost, returnCost, maxMoves);


                int assignedCount = assignedDispatches == null ? 0 : assignedDispatches.size();
                if (assignedCount == 0) {
                    // nothing assigned to this drone — skip
                    continue;
                }
                double totalMaxCost = (takeoffCost + returnCost) + ((maxMoves * costPerMove) / (double) assignedCount);
                log.info("(takeoffCost {} + returnCost {} ) + (maxMoves {} * costPerMove {} ) divided by {} dispatches", takeoffCost, returnCost, maxMoves, costPerMove, assignedCount);
                log.info("Drone {} - Total maxCost requirement: {}", droneId, totalMaxCost);


                // Find service point
                ServicePointDTO servicePoint = findServicePointForDrone(droneId, servicePoints, servicePointsWithLocations);
                if (servicePoint == null) continue;
                LngLat start = new LngLat(servicePoint.getLocation().getLng(), servicePoint.getLocation().getLat());

                // Sort dispatches by distance
                assignedDispatches.sort(Comparator.comparingDouble(d -> distanceService.calculateDistance(start.getLng(), start.getLat(), d.getDelivery().getLng(), d.getDelivery().getLat())));
                // Build deliveries with flight paths
                List<DeliveryDTO> deliveries = buildDeliveries(start, assignedDispatches, noFlyZones, maxMoves, costPerMove);
                if (deliveries.isEmpty()) continue;

                // Calculate totals
                int droneMovesUsed = 0;
                for (DeliveryDTO delivery : deliveries) {
                    droneMovesUsed += delivery.getFlightPath().size() - 1;
                }

                // Apply the formula: Total Cost = (takeoff + return) + (moves * costPerMove)
                double droneTotalCost = (takeoffCost + returnCost) + (droneMovesUsed * costPerMove);

                if (droneTotalCost > totalMaxCost) {
                    log.warn("Drone {} cost {} exceeds maxCost requirement {}, skipping",
                            droneId, droneTotalCost, totalMaxCost);
                    continue;
                }

                totalMoves += droneMovesUsed;
                totalCost += droneTotalCost;

                DronePathDTO dronePath = new DronePathDTO();
                dronePath.setDroneId(droneId);
                dronePath.setDeliveries(deliveries);
                dronePaths.add(dronePath);
            }

            result.setDronePaths(dronePaths);
            result.setTotalCost(totalCost);
            result.setTotalMoves(totalMoves);
            log.info("Total moves used: {}", totalMoves);
            return result;
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error in calculateDeliveryPath: " + e.getMessage());
            e.printStackTrace();
            // Return a default or error response
            DeliveryPathDTO errorResult = new DeliveryPathDTO();
            errorResult.setTotalCost(0.0);
            errorResult.setTotalMoves(0);
            errorResult.setDronePaths(Collections.emptyList());
            return errorResult;
        }
    }

    public DeliveryPathDTO calculateSingleDroneDeliveryPath(List<MedDispatchRecDTO> dispatches) {
        try {
            // Retrieve available drones and select only one
            List<String> availableDrones = ilpClient.queryAvailableDrones(dispatches);

            if (availableDrones.isEmpty()) {
                throw new IllegalStateException("No available drones for these dispatches");
            }

            // Force use of only the first available drone
            List<String> singleDrone = Collections.singletonList(availableDrones.get(0));

            ServicePointDronesDTO[] servicePoints = ilpClient.getServicePointsWithDrones();
            ServicePointDTO[] servicePointsWithLocations = ilpClient.getServicePoints();

            DispatchAggregationService.AggregatedRequirements aggregated =
                    dispatchAggregationService.aggregateRequirements(dispatches);

            // Assign all dispatches to the single drone
            Map<String, List<MedDispatchRecDTO>> droneAssignments =
                    assignDispatchesToDrones(dispatches, singleDrone, servicePoints, servicePointsWithLocations, aggregated);

            if (droneAssignments.isEmpty()) {
                log.info("No drones have been chosen for delivery (assignments empty) - aborting calculations.");
            } else {
                // Build compact assignment summary: droneId:[id1,id2] droneId2:[...]
                StringBuilder summary = new StringBuilder();
                for (Map.Entry<String, List<MedDispatchRecDTO>> e : droneAssignments.entrySet()) {
                    summary.append(e.getKey()).append(":[");
                    List<MedDispatchRecDTO> list = e.getValue();
                    for (int i = 0; i < list.size(); i++) {
                        summary.append(list.get(i).getId());
                        if (i < list.size() - 1) summary.append(",");
                    }
                    summary.append("] ");
                }
                log.info("Selected drones before calculations: {}. Assignment summary: {}",
                        droneAssignments.keySet(), summary.toString().trim());
            }

            // Rest of the calculation logic (same as calculateDeliveryPath)
            NoFlyZoneDTO[] noFlyZonesResponse = ilpClient.getNoFlyZones();
            List<NoFlyZoneDTO> noFlyZones = noFlyZonesResponse != null ?
                    Arrays.asList(noFlyZonesResponse) : Collections.emptyList();

            DeliveryPathDTO result = new DeliveryPathDTO();
            List<DronePathDTO> dronePaths = new ArrayList<>();
            double totalCost = 0.0;
            int totalMoves = 0;

            for (Map.Entry<String, List<MedDispatchRecDTO>> entry : droneAssignments.entrySet()) {
                String droneId = entry.getKey();
                List<MedDispatchRecDTO> assignedDispatches = entry.getValue();

                // Aggregate maxCost and calculate maxMoves

                DroneDTO droneDetails = ilpClient.getDroneDetails(droneId);
                double costPerMove = droneDetails.getCapability().getCostPerMove();
                double takeoffCost = droneDetails.getCapability().getCostInitial();
                double returnCost = droneDetails.getCapability().getCostFinal();
                double maxMoves = droneDetails.getCapability().getMaxMoves();

                log.info("Drone {} details: costPerMove={}, takeoffCost={}, returnCost={}, maxMoves={}",
                        droneId, costPerMove, takeoffCost, returnCost, maxMoves);


                double totalMaxCost = (takeoffCost + returnCost) + (maxMoves * costPerMove);
                log.info("(takeoffCost {} + returnCost {} ) + (maxMoves {} * costPerMove {} )", takeoffCost, returnCost, maxMoves, costPerMove);
                log.info("Drone {} - Total maxCost requirement: {}", droneId, totalMaxCost);


                // Find service point
                ServicePointDTO servicePoint = findServicePointForDrone(droneId, servicePoints, servicePointsWithLocations);
                if (servicePoint == null) continue;
                LngLat start = new LngLat(servicePoint.getLocation().getLng(), servicePoint.getLocation().getLat());

                // Sort dispatches by distance
                assignedDispatches.sort(Comparator.comparingDouble(d -> distanceService.calculateDistance(start.getLng(), start.getLat(), d.getDelivery().getLng(), d.getDelivery().getLat())));
                // Build deliveries with flight paths
                List<DeliveryDTO> deliveries = buildDeliveries(start, assignedDispatches, noFlyZones, maxMoves, costPerMove);
                if (deliveries.isEmpty()) continue;

                // Calculate totals
                int droneMovesUsed = 0;
                for (DeliveryDTO delivery : deliveries) {
                    droneMovesUsed += delivery.getFlightPath().size() - 1;
                }

                // Apply the formula: Total Cost = (takeoff + return) + (moves * costPerMove)
                double droneTotalCost = (takeoffCost + returnCost) + (droneMovesUsed * costPerMove);

                if (droneTotalCost > totalMaxCost) {
                    log.warn("Drone {} cost {} exceeds maxCost requirement {}, skipping",
                            droneId, droneTotalCost, totalMaxCost);
                    continue;
                }

                totalMoves += droneMovesUsed;
                totalCost += droneTotalCost;

                DronePathDTO dronePath = new DronePathDTO();
                dronePath.setDroneId(droneId);
                dronePath.setDeliveries(deliveries);
                dronePaths.add(dronePath);
            }

            result.setDronePaths(dronePaths);
            result.setTotalCost(totalCost);
            result.setTotalMoves(totalMoves);
            log.info("Total moves used: {}", totalMoves);
            return result;
        } catch (Exception e) {
            System.err.println("Error in calculateDeliveryPath: " + e.getMessage());
            e.printStackTrace();
            // Return a default or error response
            DeliveryPathDTO errorResult = new DeliveryPathDTO();
            errorResult.setTotalCost(0.0);
            errorResult.setTotalMoves(0);
            errorResult.setDronePaths(Collections.emptyList());
            return errorResult;
        }
    }


    private Map<String, List<MedDispatchRecDTO>> assignDispatchesToDrones(
            List<MedDispatchRecDTO> dispatches,
            List<String> availableDrones,
            ServicePointDronesDTO[] servicePoints,
            ServicePointDTO[] servicePointsWithLocations,
            DispatchAggregationService.AggregatedRequirements aggregated) {

        Map<String, List<MedDispatchRecDTO>> assignments = new HashMap<>();

        for (MedDispatchRecDTO dispatch : dispatches) {
            DispatchAggregationService.AggregatedRequirements individualReq =
                    dispatchAggregationService.aggregateRequirements(Collections.singletonList(dispatch));

            ServicePointDronesDTO bestServicePoint = null;
            String assignedDrone = null;
            double minDistance = Double.MAX_VALUE;

            for (ServicePointDronesDTO servicePoint : servicePoints) {
                for (ServicePointDronesDTO.DroneWithAvailabilityDTO drone : servicePoint.getDrones()) {
                    if (availableDrones.contains(drone.getId())) {
                        DroneDTO droneDetails = ilpClient.getDroneDetails(drone.getId());

                        if (availabilityService.matchesRequirements(droneDetails, individualReq)) {
                            ServicePointDTO locationSP = findServicePointById(
                                    servicePoint.getServicePointId(), servicePointsWithLocations);
                            if (locationSP != null) {
                                double distance = distanceService.calculateDistance(
                                        dispatch.getDelivery().getLng(),
                                        dispatch.getDelivery().getLat(),
                                        locationSP.getLocation().getLng(),
                                        locationSP.getLocation().getLat());

                                // Add penalty for drones with existing assignments
                                int currentAssignments = assignments.getOrDefault(drone.getId(), Collections.emptyList()).size();
                                double adjustedDistance = distance * (1.0 + currentAssignments * 2.0);

                                double waste = adjustedDistance - minDistance;
                                if (minDistance == Double.MAX_VALUE || waste < 0) {
                                    minDistance = adjustedDistance;
                                    bestServicePoint = servicePoint;
                                    assignedDrone = drone.getId();
                                }
                            }
                        }
                    }
                }
            }

            if (assignedDrone != null) {
                assignments.computeIfAbsent(assignedDrone, k -> new ArrayList<>()).add(dispatch);
            }
        }
        return assignments;
    }


    private ServicePointDTO findServicePointForDrone(String droneId, ServicePointDronesDTO[] servicePoints, ServicePointDTO[] servicePointsWithLocations) {
        for (ServicePointDTO sp : servicePointsWithLocations) {
            for (ServicePointDronesDTO spDrones : servicePoints) {
                if (spDrones.getServicePointId() == sp.getId() && spDrones.getDrones().stream().anyMatch(d -> d.getId().equals(droneId))) {
                    return sp;
                }
            }
        }
        return null;
    }

    private ServicePointDTO findServicePointById(long id, ServicePointDTO[] servicePointsWithLocations) {
        for (ServicePointDTO sp : servicePointsWithLocations) {
            if (sp.getId() == id) return sp;
        }
        return null;
    }

    private List<DeliveryDTO> buildDeliveries(LngLat start, List<MedDispatchRecDTO> dispatches, List<NoFlyZoneDTO> noFlyZones, double maxMoves, double costPerMove) {
        List<DeliveryDTO> deliveries = new ArrayList<>();
        LngLat current = start;
        double movesUsed = 0;

        for (int i = 0; i < dispatches.size(); i++) {
            MedDispatchRecDTO dispatch = dispatches.get(i);
            LngLat target = new LngLat(dispatch.getDelivery().getLng(), dispatch.getDelivery().getLat());
            boolean isLastDelivery = (i == dispatches.size() - 1);

            double remainingMoves = maxMoves - movesUsed;
            if (remainingMoves <= 0) {
                System.err.println("No moves remaining, stopping deliveries");
                break;
            }

            if (pathService.isInNoFlyZone(target, noFlyZones)) {
                log.error("Delivery location {} is in a no-fly zone, skipping", dispatch.getId());
                continue;
            }

            // Calculate path from current position to delivery target
            List<LngLat> segment = pathService.findPath(current, target, noFlyZones, remainingMoves);
            if (segment.isEmpty()) {
                System.err.println("Cannot reach delivery " + dispatch.getId() + " with remaining moves: " + remainingMoves);
                break;
            }

            // Add hover at the delivery location (duplicate LngLat)
            segment.add(target);
            movesUsed += segment.size() - 1;

            // If this is the last delivery, append a return path to service point
            if (isLastDelivery) {
                remainingMoves = maxMoves - movesUsed;
                List<LngLat> returnSegment = pathService.findPath(target, start, noFlyZones, remainingMoves);
                if (!returnSegment.isEmpty()) {
                    segment.addAll(returnSegment);
                    movesUsed += returnSegment.size();
                }
            }

            DeliveryDTO delivery = new DeliveryDTO();
            delivery.setDeliveryId(dispatch.getId());
            delivery.setFlightPath(segment);
            deliveries.add(delivery);

            current = target;
        }

        return deliveries;
    }

    public String toGeoJson(DeliveryPathDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\": \"FeatureCollection\", \"features\": [");
        boolean firstFeature = true;
        for (DronePathDTO dronePath : dto.getDronePaths()) {
            for (DeliveryDTO delivery : dronePath.getDeliveries()) {
                if (!firstFeature) sb.append(",");
                sb.append("{\"type\": \"Feature\", \"properties\": {\"droneId\": \"").append(dronePath.getDroneId()).append("\", \"deliveryId\": \"").append(delivery.getDeliveryId()).append("\"}, \"geometry\": {\"type\": \"LineString\", \"coordinates\": [");
                boolean firstCoord = true;
                for (LngLat point : delivery.getFlightPath()) {
                    if (!firstCoord) sb.append(",");
                    sb.append("[").append(point.getLng()).append(",").append(point.getLat()).append("]");
                    firstCoord = false;
                }
                sb.append("]}}");
                firstFeature = false;
            }
        }
        sb.append("]}");
        return sb.toString();
    }


}

