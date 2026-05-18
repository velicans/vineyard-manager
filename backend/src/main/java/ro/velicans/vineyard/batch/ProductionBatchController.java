package ro.velicans.vineyard.batch;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches")
public class ProductionBatchController {

    private final ProductionBatchService service;

    public ProductionBatchController(ProductionBatchService service) { this.service = service; }

    @GetMapping
    public List<ProductionBatchDto> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ProductionBatchDto findById(@PathVariable UUID id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductionBatchDto create(@RequestBody ProductionBatchDto dto) { return service.create(dto); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
