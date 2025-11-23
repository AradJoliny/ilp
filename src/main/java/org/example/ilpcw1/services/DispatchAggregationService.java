package org.example.ilpcw1.services;

import org.example.ilpcw1.dto.MedDispatchRecDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DispatchAggregationService {

    private double totalMaxCost;
    public double getTotalMaxCost() {
        return totalMaxCost;
    }

    public void setTotalMaxCost(double totalMaxCost) {
        this.totalMaxCost = totalMaxCost;
    }

    public AggregatedRequirements aggregateRequirements(List<MedDispatchRecDTO> reqs) {
        boolean needsCooling = false;
        boolean needsHeating = false;
        double totalCapacity = 0.0;
        double totalMaxCost = 0.0;

        for (MedDispatchRecDTO req : reqs) {
            MedDispatchRecDTO.Requirements requirements = req.getRequirements();
            if (requirements != null) {
                if (Boolean.TRUE.equals(requirements.getCooling())) {
                    needsCooling = true;
                }
                if (Boolean.TRUE.equals(requirements.getHeating())) {
                    needsHeating = true;
                }
                if (requirements.getCapacity() != null) {
                    totalCapacity += requirements.getCapacity();
                }
                if (requirements.getMaxCost() != null) {
                    totalMaxCost += requirements.getMaxCost();
                }
            }
        }

        return new AggregatedRequirements(needsCooling, needsHeating, totalCapacity, totalMaxCost);
    }

    public record AggregatedRequirements(boolean needsCooling, boolean needsHeating, double totalCapacity, double totalMaxCost) {
    }
}
