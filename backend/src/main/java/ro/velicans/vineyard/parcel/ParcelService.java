package ro.velicans.vineyard.parcel;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Transactional
@Service
public class ParcelService {

    private final ParcelRepository repo;

    public ParcelService(ParcelRepository repo) { this.repo = repo; }

    public List<ParcelDto> findAll() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    public ParcelDto findById(UUID id) {
        return repo.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new NoSuchElementException("Parcel not found: " + id));
    }

    public ParcelDto create(ParcelDto dto) {
        Parcel p = new Parcel();
        p.setName(dto.name());
        p.setGrapeVariety(dto.grapeVariety());
        p.setAreaSqM(dto.areaSqM());
        return toDto(repo.save(p));
    }

    private ParcelDto toDto(Parcel p) {
        return new ParcelDto(p.getId(), p.getName(), p.getGrapeVariety(), p.getAreaSqM());
    }
}
