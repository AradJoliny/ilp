package org.example.ilpcw1.dto;

import java.util.List;

public class RegionDTO {
    private String name;
    private List<PositionDTO> vertices;

    public RegionDTO() {}

    public RegionDTO(String name, List<PositionDTO> vertices) {
        this.name = name;
        this.vertices = vertices;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<PositionDTO> getVertices() { return vertices; }
    public void setVertices(List<PositionDTO> vertices) { this.vertices = vertices; }
}
