package org.example.ilpcw1.dto;

import org.example.ilpcw1.model.LngLat;
import java.util.List;

public class ServicePointDronesDTO {
    private long servicePointId;
    private LngLat location;
    private List<DroneWithAvailabilityDTO> drones;

    public long getServicePointId() {
        return servicePointId;
    }

    public void setServicePointId(long servicePointId) {
        this.servicePointId = servicePointId;
    }

    public LngLat getLocation() {
        return location;
    }

    public void setLocation(LngLat location) {
        this.location = location;
    }

    public List<DroneWithAvailabilityDTO> getDrones() {
        return drones;
    }

    public void setDrones(List<DroneWithAvailabilityDTO> drones) {
        this.drones = drones;
    }

    public static class DroneWithAvailabilityDTO {
        private String id;
        private List<AvailabilityDTO> availability;

        public DroneWithAvailabilityDTO() {
        }

        public List<AvailabilityDTO> getAvailability() {
            return availability;
        }

        public void setAvailability(List<AvailabilityDTO> availability) {
            this.availability = availability;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class AvailabilityDTO {
        private String dayOfWeek;
        private String from;
        private String until;

        public AvailabilityDTO() {
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(String dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getUntil() {
            return until;
        }

        public void setUntil(String until) {
            this.until = until;
        }
    }
}
