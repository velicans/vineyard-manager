package ro.velicans.vineyard.bottling;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches/{batchId}/bottling")
public class BottlingController {

    private final BottlingService service;

    public BottlingController(BottlingService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BottlingDto record(@PathVariable UUID batchId, @RequestBody BottlingDto dto) {
        return service.record(batchId, dto);
    }

    @GetMapping
    public BottlingDto get(@PathVariable UUID batchId) {
        return service.findByBatchId(batchId);
    }
}
