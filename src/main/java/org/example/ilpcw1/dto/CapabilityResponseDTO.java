package org.example.ilpcw1.dto;

public class CapabilityResponseDTO {
    private boolean cooling;
    private boolean heating;
    private double capacity;
    private int maxMoves;
    private double costPerMove;
    private double costInitial;
    private double costFinal;

    public CapabilityResponseDTO(boolean cooling, boolean heating, double capacity, int maxMoves, double costPerMove, double costInitial, double costFinal) {
        this.cooling = cooling;
        this.heating = heating;
        this.capacity = capacity;
        this.maxMoves = maxMoves;
        this.costPerMove = costPerMove;
        this.costInitial = costInitial;
        this.costFinal = costFinal;
    }

    // Getters
    public boolean isCooling() { return cooling; }
    public boolean isHeating() { return heating; }
    public double getCapacity() { return capacity; }
    public int getMaxMoves() { return maxMoves; }
    public double getCostPerMove() { return costPerMove; }
    public double getCostInitial() { return costInitial; }
    public double getCostFinal() { return costFinal; }
}
