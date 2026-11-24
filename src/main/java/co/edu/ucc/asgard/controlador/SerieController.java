package co.edu.ucc.asgard.controlador;

import co.edu.ucc.asgard.dto.serie.RegistrarSerieRequest;
import co.edu.ucc.asgard.dto.serie.RespuestasSerie.DeshacerResponse;
import co.edu.ucc.asgard.dto.serie.RespuestasSerie.RegistroSerieResponse;
import co.edu.ucc.asgard.dto.serie.RespuestasSerie.ValidacionSerieResponse;
import co.edu.ucc.asgard.servicio.SerieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/socios/{socioId}/series")
@Tag(name = "4. Entrenamiento", description = "Sesion del dia, series, advertencias y deshacer (RF04 - RF07)")
public class SerieController {

    private final SerieService serieService;

    public SerieController(SerieService serieService) {
        this.serieService = serieService;
    }

    @GetMapping("/validar")
    @Operation(summary = "Validar una serie antes de registrarla: devuelve la advertencia sin bloquear (RF06)")
    public ValidacionSerieResponse validar(@PathVariable String socioId, @RequestParam String ejercicioId,
            @RequestParam double peso, @RequestParam int repeticiones, @RequestParam(required = false) Double rpe) {
        return serieService.validar(socioId, ejercicioId, peso, repeticiones, rpe);
    }

    @PostMapping
    @Operation(summary = "Registrar una serie en un solo paso (RF05, US006, US013)")
    public ResponseEntity<RegistroSerieResponse> registrar(@PathVariable String socioId,
            @Valid @RequestBody RegistrarSerieRequest solicitud) {
        RegistroSerieResponse registro = serieService.registrar(socioId, solicitud);
        return ResponseEntity.created(URI.create("/api/socios/" + socioId + "/series/" + registro.serie().id()))
                .body(registro);
    }

    @PostMapping("/deshacer")
    @Operation(summary = "Deshacer la ultima serie registrada (RF07, US008)")
    public DeshacerResponse deshacer(@PathVariable String socioId) {
        return serieService.deshacerUltima(socioId);
    }
}
