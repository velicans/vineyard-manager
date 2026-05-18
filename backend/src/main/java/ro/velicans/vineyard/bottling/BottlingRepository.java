package ro.velicans.vineyard.bottling;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BottlingRepository extends JpaRepository<Bottling, UUID> {
    Optional<Bottling> findByBatchId(UUID batchId);
    List<Bottling> findByBottleVolume(BottleVolume volume);
}
