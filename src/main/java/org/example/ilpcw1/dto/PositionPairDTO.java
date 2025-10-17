package org.example.ilpcw1.dto;

public class PositionPairDTO {
    private PositionDTO position1;
    private PositionDTO position2;

    public PositionPairDTO() {}

    public PositionPairDTO(PositionDTO position1, PositionDTO position2) {
        this.position1 = position1;
        this.position2 = position2;
    }

    public PositionDTO getPosition1() { return position1; }
    public void setPosition1(PositionDTO position1) { this.position1 = position1; }
    public PositionDTO getPosition2() { return position2; }
    public void setPosition2(PositionDTO position2) { this.position2 = position2; }
}
