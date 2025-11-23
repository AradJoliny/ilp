package org.example.ilpcw1.dto;

import org.example.ilpcw1.client.IlpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

public class DroneDTO {
    private String name;
    private String id;
    private double costPerMove;

    public double getCostPerMove() {
        return costPerMove;
    }

    public void setCostPerMove(double costPerMove) {
        this.costPerMove = costPerMove;
    }

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    private DroneCapabilityDTO capability;
    private List<MedDispatchRecDTO> medDispatchRecs;

    public DroneCapabilityDTO getCapability() {return capability;}
    public void setCapability(DroneCapabilityDTO capability) {this.capability = capability;}

    public List<MedDispatchRecDTO> getMedDispatchRecs() { return medDispatchRecs; }
    public void setMedDispatchRecs(List<MedDispatchRecDTO> medDispatchRecs) { this.medDispatchRecs = medDispatchRecs; }

}
