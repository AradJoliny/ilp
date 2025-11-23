package org.example.ilpcw1.dto;

import org.example.ilpcw1.model.LngLat;

import java.util.List;

public class NoFlyZoneDTO {
    private String name;
    private long id;
    private LimitsDTO limits;
    private List<VertexDTO> vertices;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LimitsDTO getLimits() {
        return limits;
    }

    public void setLimits(LimitsDTO limits) {
        this.limits = limits;
    }

    public List<VertexDTO> getVertices() {
        return vertices;
    }

    public void setVertices(List<VertexDTO> vertices) {
        this.vertices = vertices;
    }
}
