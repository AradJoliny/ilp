package org.example.ilpcw1.dto;

public class RegionCheckDTO {
    private PositionDTO position;
    private RegionDTO region;

    public RegionCheckDTO() {}

    public RegionCheckDTO(PositionDTO position, RegionDTO region) {
        this.position = position;
        this.region = region;
    }

    public PositionDTO getPosition() {
        return position;
    }

    public void setPosition(PositionDTO position) {
        this.position = position;
    }

    public RegionDTO getRegion() {
        return region;
    }

    public void setRegion(RegionDTO region) {
        this.region = region;
    }
}
