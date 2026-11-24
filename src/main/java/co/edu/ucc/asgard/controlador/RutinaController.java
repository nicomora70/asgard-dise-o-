package co.edu.ucc.asgard.controlador;

import co.edu.ucc.asgard.dto.rutina.RutinaRequest;
import co.edu.ucc.asgard.dto.rutina.RutinaResponse;
import co.edu.ucc.asgard.servicio.RutinaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/socios/{socioId}/rutinas")
@Tag(name = "3. Rutinas", description = "Rutina semanal del socio con historial (RF03)")
public class RutinaController {

    private final RutinaService rutinaService;

    public RutinaController(RutinaService rutinaService) {
        this.rutinaService = rutinaService;
    }

    @PostMapping
    @Operation(summary = "Asignar una rutina semanal: queda activa y la anterior pasa al historial (US004)")
    public ResponseEntity<RutinaResponse> asignar(@PathVariable String socioId,
            @Valid @RequestBody RutinaRequest solicitud) {
        RutinaResponse rutina = rutinaService.asignar(socioId, solicitud);
        return ResponseEntity.created(URI.create("/api/socios/" + socioId + "/rutinas/activa")).body(rutina);
    }

    @GetMapping("/activa")
    @Operation(summary = "Rutina activa del socio (409 si no tiene)")
    public RutinaResponse activa(@PathVariable String socioId) {
        return rutinaService.consultarActiva(socioId);
    }

    @GetMapping
    @Operation(summary = "Historial de rutinas del socio, de la mas reciente a la mas antigua")
    public List<RutinaResponse> historial(@PathVariable String socioId) {
        return rutinaService.historial(socioId);
    }
}
