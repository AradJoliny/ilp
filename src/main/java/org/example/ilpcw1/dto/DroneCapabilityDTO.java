package org.example.ilpcw1.dto;

import java.util.Map;

public class DroneCapabilityDTO {
    private boolean cooling;
    private boolean heating;
    private int capacity;
    private int maxMoves;
    private double costPerMove;
    private double costInitial;
    private double costFinal;

    public DroneCapabilityDTO() {}

    public boolean isCooling() {return cooling;}
    public void setCooling(boolean cooling) {this.cooling = cooling;}

    public boolean isHeating() {return heating;}
    public void setHeating(boolean heating) {this.heating = heating;}

    public int getCapacity() {return capacity;}
    public void setCapacity(Double capacity) {    this.capacity = capacity == null ? 0 : capacity.intValue();}

    public int getMaxMoves() {return maxMoves;}
    public void setMaxMoves(int maxMoves) {this.maxMoves = maxMoves;}

    public double getCostPerMove() {return costPerMove;}
    public void setCostPerMove(double costPerMove) {this.costPerMove = costPerMove;}

    public double getCostInitial() {return costInitial;}
    public void setCostInitial(double costInitial) {this.costInitial = costInitial;}

    public double getCostFinal() {return costFinal;}
    public void setCostFinal(double costFinal) {this.costFinal = costFinal;}

    public Map<String, Double> getAttributes() {
        return Map.of(
                "capacity", (double) capacity,
                "maxMoves", (double) maxMoves,
                "costPerMove", costPerMove,
                "costInitial", costInitial,
                "costFinal", costFinal
        );
    }
}

