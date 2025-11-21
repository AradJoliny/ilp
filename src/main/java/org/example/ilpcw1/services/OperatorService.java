package org.example.ilpcw1.services;

import org.example.ilpcw1.dto.DroneDTO;
import org.example.ilpcw1.dto.QueryAttributeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OperatorService {

    private static final Logger log = LoggerFactory.getLogger(OperatorService.class);

    public List<String> matchAttributes(DroneDTO[] arr, String attribute, String attributeValue) {
        List<String> droneIds = new ArrayList<>();
        if (arr != null) {
            for (DroneDTO drone : arr) {
                if (drone != null && drone.getCapability() != null) {
                    Map<String, Double> attributes = drone.getCapability().getAttributes();
                    if (attributes != null && attributes.containsKey(attribute)) {
                        Double value = attributes.get(attribute);
                        if (value != null) {
                            try {
                                double attrVal = Double.parseDouble(attributeValue);
                                if (value >= attrVal) {
                                    droneIds.add(drone.getId());
                                }
                            } catch (NumberFormatException e) {
                                log.warn("Invalid attribute value format: {}", attributeValue, e);
                            }
                        }
                    }
                }
            }
        }
        return droneIds;
    }

    // New method to match a drone against a query attribute NOW CLEAN UP ILPCLIENT

    public boolean matchesQuery(DroneDTO drone, QueryAttributeDTO query) {
        Map<String, Double> attributes = drone.getCapability().getAttributes();
        if (attributes == null || !attributes.containsKey(query.getAttribute())) {
            return false;
        }

        Double attrValue = attributes.get(query.getAttribute());
        if (attrValue == null) {
            return false;
        }

        try {
            double queryValue = Double.parseDouble(query.getValue());
            return compareNumeric(attrValue, query.getOperator(), queryValue);
        } catch (NumberFormatException e) {
            return compareString(attrValue.toString(), query.getOperator(), query.getValue());
        }
    }

    private boolean compareNumeric(double attrValue, String operator, double queryValue) {
        switch (operator) {
            case "=": return attrValue == queryValue;
            case "!=": return attrValue != queryValue;
            case "<": return attrValue < queryValue;
            case ">": return attrValue > queryValue;
            default: return false;
        }
    }

    private boolean compareString(String attrValue, String operator, String queryValue) {
        if ("=".equals(operator)) {
            return attrValue.equals(queryValue);
        }
        return false;
    }
}
