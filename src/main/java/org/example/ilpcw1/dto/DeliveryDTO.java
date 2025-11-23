package org.example.ilpcw1.dto;

import org.example.ilpcw1.model.LngLat;
import java.util.List;

public class DeliveryDTO {
    private String deliveryId;
    private List<LngLat> flightPath;

    public String getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

    public List<LngLat> getFlightPath() {
        return flightPath;
    }

    public void setFlightPath(List<LngLat> flightPath) {
        this.flightPath = flightPath;
    }
}
