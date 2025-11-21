package org.example.ilpcw1.dto;

import java.util.List;

public class DroneDTO {
    private String name;
    private String id;
    private DroneCapabilityDTO capability;
    private List<MedDispatchRecDTO> medDispatchRecs;

    public DroneDTO() {}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public DroneCapabilityDTO getCapability() {return capability;}
    public void setCapability(DroneCapabilityDTO capability) {this.capability = capability;}

    public List<MedDispatchRecDTO> getMedDispatchRecs() { return medDispatchRecs; }
    public void setMedDispatchRecs(List<MedDispatchRecDTO> medDispatchRecs) { this.medDispatchRecs = medDispatchRecs; }
}
