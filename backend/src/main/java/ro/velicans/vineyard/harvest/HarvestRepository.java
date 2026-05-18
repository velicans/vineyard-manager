package ro.velicans.vineyard.harvest;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface HarvestRepository extends JpaRepository<Harvest, UUID> {
    Optional<Harvest> findByBatchId(UUID batchId);
}
