package org.example.ilpcw1.dto;

import org.example.ilpcw1.model.LngLat;

public class MedDispatchRecDTO {

    private String id;
    private String date;
    private String time;
    private Requirements requirements;
    private LngLat location;

    public MedDispatchRecDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public Requirements getRequirements() { return requirements; }
    public void setRequirements(Requirements requirements) { this.requirements = requirements; }

    public static class Requirements {
        private Double capacity;     // required
        private Boolean cooling;     // optional, default false
        private Boolean heating;     // optional, default false
        private Double maxCost;      // optional

        public Requirements() {}

        public Double getCapacity() { return capacity; }
        public void setCapacity(Double capacity) { this.capacity = capacity; }

        public Boolean getCooling() { return cooling; }
        public void setCooling(Boolean cooling) { this.cooling = cooling; }

        public Boolean getHeating() { return heating; }
        public void setHeating(Boolean heating) { this.heating = heating; }

        public Double getMaxCost() { return maxCost; }
        public void setMaxCost(Double maxCost) { this.maxCost = maxCost; }
    }
}
