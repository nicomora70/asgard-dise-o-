package co.edu.ucc.asgard.controlador;

import co.edu.ucc.asgard.dto.serie.SerieResponse;
import co.edu.ucc.asgard.servicio.HistorialService;
import co.edu.ucc.asgard.servicio.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/socios/{socioId}")
@Tag(name = "6. Reportes", description = "Historial de series y reporte PDF de progreso (RF12)")
public class ReporteController {

    private final HistorialService historialService;
    private final ReporteService reporteService;

    public ReporteController(HistorialService historialService, ReporteService reporteService) {
        this.historialService = historialService;
        this.reporteService = reporteService;
    }

    @GetMapping("/historial")
    @Operation(summary = "Historial de series, filtrable por ejercicio y fechas ISO-8601 (US014)")
    public List<SerieResponse> historial(@PathVariable String socioId,
            @RequestParam(required = false) String ejercicioId, @RequestParam(required = false) Instant desde,
            @RequestParam(required = false) Instant hasta) {
        return historialService.historial(socioId, ejercicioId, desde, hasta);
    }

    @GetMapping(value = "/reporte/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Reporte PDF de progreso para el entrenador (US014)")
    public ResponseEntity<byte[]> reportePdf(@PathVariable String socioId,
            @RequestParam(required = false) Instant desde, @RequestParam(required = false) Instant hasta) {
        byte[] pdf = reporteService.generarPdf(socioId, desde, hasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("asgard-progreso-" + socioId + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
