package ro.velicans.vineyard.parcel;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parcels")
public class ParcelController {

    private final ParcelService service;

    public ParcelController(ParcelService service) { this.service = service; }

    @GetMapping
    public List<ParcelDto> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ParcelDto findById(@PathVariable UUID id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParcelDto create(@RequestBody ParcelDto dto) { return service.create(dto); }
}
