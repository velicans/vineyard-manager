package ro.velicans.vineyard.pressing;

import jakarta.persistence.*;
import ro.velicans.vineyard.batch.ProductionBatch;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class Pressing {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch batch;

    private LocalDate date;
    private BigDecimal mustLiters;
    private BigDecimal yieldRatio;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ProductionBatch getBatch() { return batch; }
    public void setBatch(ProductionBatch batch) { this.batch = batch; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public BigDecimal getMustLiters() { return mustLiters; }
    public void setMustLiters(BigDecimal mustLiters) { this.mustLiters = mustLiters; }
    public BigDecimal getYieldRatio() { return yieldRatio; }
    public void setYieldRatio(BigDecimal yieldRatio) { this.yieldRatio = yieldRatio; }
}
