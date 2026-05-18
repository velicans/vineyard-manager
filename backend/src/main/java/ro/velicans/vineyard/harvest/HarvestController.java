package ro.velicans.vineyard.harvest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/batches/{batchId}/harvest")
public class HarvestController {

    private final HarvestService service;

    public HarvestController(HarvestService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HarvestDto record(@PathVariable UUID batchId, @RequestBody HarvestDto dto) {
        return service.record(batchId, dto);
    }

    @GetMapping
    public HarvestDto get(@PathVariable UUID batchId) {
        return service.findByBatchId(batchId);
    }
}
