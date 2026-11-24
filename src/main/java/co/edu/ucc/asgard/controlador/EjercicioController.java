package co.edu.ucc.asgard.controlador;

import co.edu.ucc.asgard.dto.ejercicio.EjercicioRequest;
import co.edu.ucc.asgard.dto.ejercicio.EjercicioResponse;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.servicio.EjercicioService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ejercicios")
@Tag(name = "2. Catalogo de ejercicios", description = "Ejercicios y grafo de sustitutos (RF02, RF10)")
public class EjercicioController {

    private final EjercicioService ejercicioService;

    public EjercicioController(EjercicioService ejercicioService) {
        this.ejercicioService = ejercicioService;
    }

    @PostMapping
    @Operation(summary = "Crear un ejercicio con sus sustitutos (US003)")
    public ResponseEntity<EjercicioResponse> crear(@Valid @RequestBody EjercicioRequest solicitud) {
        EjercicioResponse ejercicio = ejercicioService.crear(solicitud);
        return ResponseEntity.created(URI.create("/api/ejercicios/" + ejercicio.id())).body(ejercicio);
    }

    @GetMapping
    @Operation(summary = "Listar el catalogo, opcionalmente por grupo muscular")
    public List<EjercicioResponse> listar(@RequestParam(required = false) GrupoMuscular grupo) {
        return ejercicioService.listar(grupo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un ejercicio")
    public EjercicioResponse consultar(@PathVariable String id) {
        return ejercicioService.consultar(id);
    }

    @GetMapping("/{id}/sustitutos")
    @Operation(summary = "Sustitutos directos del ejercicio en el grafo, del mas parecido al menos parecido")
    public List<EjercicioResponse.SustitutoResponse> sustitutos(@PathVariable String id) {
        return ejercicioService.sustitutos(id);
    }
}
