package co.edu.ucc.asgard.controlador;

import co.edu.ucc.asgard.dto.progresion.ProgresionResponse;
import co.edu.ucc.asgard.servicio.ProgresionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/socios/{socioId}")
@Tag(name = "5. Progresion", description = "Carga sugerida, semaforo, estancamientos y prioridades (RF08 - RF11)")
public class ProgresionController {

    private final ProgresionService progresionService;

    public ProgresionController(ProgresionService progresionService) {
        this.progresionService = progresionService;
    }

    @GetMapping("/progresion")
    @Operation(summary = "Carga sugerida, semaforo y decision de cada ejercicio (US009, US010, US011)")
    public ProgresionResponse progresion(@PathVariable String socioId) {
        return progresionService.progresion(socioId);
    }

    @GetMapping("/prioridades")
    @Operation(summary = "Los k ejercicios que mas atencion necesitan, extraidos de un Max-Heap (US012)")
    public ProgresionResponse.PrioridadesResponse prioridades(@PathVariable String socioId,
            @RequestParam(defaultValue = "5") @Min(value = 1, message = "k debe ser al menos 1")
            @Max(value = 50, message = "k debe ser maximo 50") int k) {
        return progresionService.prioridades(socioId, k);
    }
}
