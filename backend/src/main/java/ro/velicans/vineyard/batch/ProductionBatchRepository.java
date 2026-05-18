package ro.velicans.vineyard.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, UUID> {
    List<ProductionBatch> findByParcelId(UUID parcelId);
    List<ProductionBatch> findByYear(Integer year);
    List<ProductionBatch> findByParcelIdAndYear(UUID parcelId, Integer year);
}
