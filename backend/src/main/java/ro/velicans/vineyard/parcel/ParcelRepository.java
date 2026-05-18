package ro.velicans.vineyard.parcel;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ParcelRepository extends JpaRepository<Parcel, UUID> {}
