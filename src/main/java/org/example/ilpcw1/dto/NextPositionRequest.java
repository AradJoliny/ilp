package org.example.ilpcw1.dto;

public class NextPositionRequest {
    private PositionDTO start;
    private double angle;

    public NextPositionRequest() {}

    public PositionDTO getStart() {
        return start;
    }

    public void setStart(PositionDTO start) {
        this.start = start;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
