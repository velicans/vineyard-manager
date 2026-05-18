package ro.velicans.vineyard.parcel;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Parcel {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private String grapeVariety;
    private Integer areaSqM;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGrapeVariety() { return grapeVariety; }
    public void setGrapeVariety(String grapeVariety) { this.grapeVariety = grapeVariety; }
    public Integer getAreaSqM() { return areaSqM; }
    public void setAreaSqM(Integer areaSqM) { this.areaSqM = areaSqM; }
}
