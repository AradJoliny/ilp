package org.example.ilpcw1.dto;

import java.util.List;

public class DeliveryPathDTO {
    private double totalCost;
    private int totalMoves;
    private List<DronePathDTO> dronePaths;

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public int getTotalMoves() {
        return totalMoves;
    }

    public void setTotalMoves(int totalMoves) {
        this.totalMoves = totalMoves;
    }

    public List<DronePathDTO> getDronePaths() {
        return dronePaths;
    }

    public void setDronePaths(List<DronePathDTO> dronePaths) {
        this.dronePaths = dronePaths;
    }
}
