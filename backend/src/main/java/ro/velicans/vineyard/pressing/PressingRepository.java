package ro.velicans.vineyard.pressing;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PressingRepository extends JpaRepository<Pressing, UUID> {
    Optional<Pressing> findByBatchId(UUID batchId);
}
