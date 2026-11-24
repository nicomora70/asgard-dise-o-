package co.edu.ucc.asgard.controlador;

import co.edu.ucc.asgard.dto.sesion.SesionHoyResponse;
import co.edu.ucc.asgard.dto.sesion.SesionResponse;
import co.edu.ucc.asgard.servicio.SesionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/socios/{socioId}/sesiones")
@Tag(name = "4. Entrenamiento", description = "Sesion del dia, series, advertencias y deshacer (RF04 - RF07)")
public class SesionController {

    private final SesionService sesionService;

    public SesionController(SesionService sesionService) {
        this.sesionService = sesionService;
    }

    @GetMapping("/hoy")
    @Operation(summary = "Sesion que toca hoy: la pendiente mas antigua (cola FIFO) o la de la rutina (US005)")
    public SesionHoyResponse hoy(@PathVariable String socioId) {
        return sesionService.sesionDeHoy(socioId);
    }

    @GetMapping
    @Operation(summary = "Historial de sesiones del socio")
    public List<SesionResponse> historial(@PathVariable String socioId) {
        return sesionService.historial(socioId);
    }

    @PostMapping("/{sesionId}/completar")
    @Operation(summary = "Completar una sesion; si esta en la cola debe ser la del frente")
    public SesionResponse completar(@PathVariable String socioId, @PathVariable String sesionId) {
        return sesionService.completar(socioId, sesionId);
    }

    @PostMapping("/{sesionId}/omitir")
    @Operation(summary = "Omitir una sesion pendiente para seguir con la rutina")
    public SesionResponse omitir(@PathVariable String socioId, @PathVariable String sesionId) {
        return sesionService.omitir(socioId, sesionId);
    }
}
