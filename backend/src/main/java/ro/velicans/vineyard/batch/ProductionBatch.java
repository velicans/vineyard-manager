package ro.velicans.vineyard.batch;

import jakarta.persistence.*;
import ro.velicans.vineyard.parcel.Parcel;
import java.util.UUID;

@Entity
public class ProductionBatch {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcel_id")
    private Parcel parcel;

    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "batch_status")
    private BatchStatus status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Parcel getParcel() { return parcel; }
    public void setParcel(Parcel parcel) { this.parcel = parcel; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }
}
