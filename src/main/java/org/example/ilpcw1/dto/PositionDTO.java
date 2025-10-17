package org.example.ilpcw1.dto;

public class PositionDTO {
    private Double lng;
    private Double lat;
    private Double angle;

    public PositionDTO() {}

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getAngle() {
        return angle;
    }

    public void setAngle(Double angle) {
        this.angle = angle;
    }
}
