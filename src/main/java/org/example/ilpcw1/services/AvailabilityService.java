package org.example.ilpcw1.services;

import org.example.ilpcw1.client.IlpClient;
import org.example.ilpcw1.dto.DroneDTO;
import org.example.ilpcw1.dto.ServicePointDronesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AvailabilityService {

    public boolean isDroneAvailable(String droneId, List<ServicePointDronesDTO.DroneWithAvailabilityDTO> drones, LocalDateTime requestTime) {
        DayOfWeek currentDay = requestTime.getDayOfWeek();
        LocalTime currentTime = requestTime.toLocalTime();

        for (ServicePointDronesDTO.DroneWithAvailabilityDTO drone : drones) {
            if (drone.getId().equals(droneId)) {
                for (ServicePointDronesDTO.AvailabilityDTO availability : drone.getAvailability()) {
                    if (availability.getDayOfWeek().equalsIgnoreCase(currentDay.toString())) {
                        LocalTime from = LocalTime.parse(availability.getFrom());
                        LocalTime until = LocalTime.parse(availability.getUntil());
                        if (!currentTime.isBefore(from) && currentTime.isBefore(until)) {
                            return true;  // Drone is available during this slot (inclusive of start)
                        }
                    }
                }
            }
        }
        return false;  // No matching availability slot
    }

    public boolean matchesRequirements(DroneDTO drone, DispatchAggregationService.AggregatedRequirements reqs) {
        return (!reqs.needsCooling() || drone.getCapability().isCooling()) &&
                (!reqs.needsHeating() || drone.getCapability().isHeating()) &&
                drone.getCapability().getCapacity() >= reqs.totalCapacity();
    }
}
