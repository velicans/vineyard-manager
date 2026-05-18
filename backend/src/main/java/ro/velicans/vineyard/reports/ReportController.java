package ro.velicans.vineyard.reports;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) { this.service = service; }

    @GetMapping("/harvest")
    public List<HarvestReportDto> harvest(@RequestParam(required = false) Integer year) {
        return service.harvestReport(year);
    }

    @GetMapping("/pressing")
    public List<PressingReportDto> pressing(@RequestParam(required = false) Integer year) {
        return service.pressingReport(year);
    }

    @GetMapping("/bottling")
    public List<BottlingReportDto> bottling(@RequestParam(required = false) Integer year) {
        return service.bottlingReport(year);
    }
}
