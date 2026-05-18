package ro.velicans.vineyard.parcel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParcelServiceTest {

    @Mock ParcelRepository repo;
    @InjectMocks ParcelService service;

    @Test
    void findAll_returnsMappedDtos() {
        Parcel p = new Parcel();
        p.setId(UUID.randomUUID());
        p.setName("Nord");
        p.setGrapeVariety("Feteasca Alba");
        p.setAreaSqM(1200);
        when(repo.findAll()).thenReturn(List.of(p));

        List<ParcelDto> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Nord");
    }

    @Test
    void findById_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(id))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void create_savesAndReturnsDto() {
        ParcelDto req = new ParcelDto(null, "Sud", "Merlot", 800);
        Parcel saved = new Parcel();
        saved.setId(UUID.randomUUID());
        saved.setName("Sud");
        saved.setGrapeVariety("Merlot");
        saved.setAreaSqM(800);
        when(repo.save(any())).thenReturn(saved);

        ParcelDto result = service.create(req);

        assertThat(result.name()).isEqualTo("Sud");
        assertThat(result.id()).isNotNull();
    }
}
