package org.example.ilpcw1.dto;

import java.util.List;

public class DronePathDTO {
    private String droneId;
    private List<DeliveryDTO> deliveries;

    public String getDroneId() {
        return droneId;
    }

    public void setDroneId(String droneId) {
        this.droneId = droneId;
    }

    public List<DeliveryDTO> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<DeliveryDTO> deliveries) {
        this.deliveries = deliveries;
    }
}
