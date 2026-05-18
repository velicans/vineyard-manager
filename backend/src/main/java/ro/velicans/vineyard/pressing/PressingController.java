package ro.velicans.vineyard.pressing;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches/{batchId}/pressing")
public class PressingController {

    private final PressingService service;

    public PressingController(PressingService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PressingDto record(@PathVariable UUID batchId, @RequestBody PressingDto dto) {
        return service.record(batchId, dto);
    }

    @GetMapping
    public PressingDto get(@PathVariable UUID batchId) {
        return service.findByBatchId(batchId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID batchId) {
        service.delete(batchId);
    }
}
