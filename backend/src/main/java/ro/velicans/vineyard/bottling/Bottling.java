package ro.velicans.vineyard.bottling;

import jakarta.persistence.*;
import ro.velicans.vineyard.batch.ProductionBatch;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Bottling {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch batch;

    private LocalDate date;
    private Integer bottleCount;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "bottle_volume")
    private BottleVolume bottleVolume;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ProductionBatch getBatch() { return batch; }
    public void setBatch(ProductionBatch batch) { this.batch = batch; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Integer getBottleCount() { return bottleCount; }
    public void setBottleCount(Integer bottleCount) { this.bottleCount = bottleCount; }
    public BottleVolume getBottleVolume() { return bottleVolume; }
    public void setBottleVolume(BottleVolume bottleVolume) { this.bottleVolume = bottleVolume; }
}
