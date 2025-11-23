package org.example.ilpcw1.dto;

public class DroneDetailsResponseDTO {
    private String name;
    private String id;
    private CapabilityResponseDTO capability;

    public DroneDetailsResponseDTO(String name, String id, CapabilityResponseDTO capability) {
        this.name = name;
        this.id = id;
        this.capability = capability;
    }

    public String getName() { return name; }
    public String getId() { return id; }
    public CapabilityResponseDTO getCapability() { return capability; }
}
