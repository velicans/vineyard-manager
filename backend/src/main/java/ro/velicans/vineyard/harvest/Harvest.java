package ro.velicans.vineyard.harvest;

import jakarta.persistence.*;
import ro.velicans.vineyard.batch.ProductionBatch;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Harvest {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch batch;

    private LocalDate date;
    private BigDecimal quantityKg;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ProductionBatch getBatch() { return batch; }
    public void setBatch(ProductionBatch batch) { this.batch = batch; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getQuantityKg() { return quantityKg; }
    public void setQuantityKg(BigDecimal quantityKg) { this.quantityKg = quantityKg; }
}
