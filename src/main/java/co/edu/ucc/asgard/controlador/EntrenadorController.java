package co.edu.ucc.asgard.controlador;

import co.edu.ucc.asgard.dto.entrenador.EntrenadorRequest;
import co.edu.ucc.asgard.dto.entrenador.EntrenadorResponse;
import co.edu.ucc.asgard.dto.socio.SocioResponse;
import co.edu.ucc.asgard.servicio.EntrenadorService;
import co.edu.ucc.asgard.servicio.SocioService;
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
@RequestMapping("/api/entrenadores")
@Tag(name = "1. Socios y entrenadores", description = "Registro de socios, entrenadores, datos fisicos y lesiones (RF01)")
public class EntrenadorController {

    private final EntrenadorService entrenadorService;
    private final SocioService socioService;

    public EntrenadorController(EntrenadorService entrenadorService, SocioService socioService) {
        this.entrenadorService = entrenadorService;
        this.socioService = socioService;
    }

    @PostMapping
    @Operation(summary = "Registrar un entrenador con su turno y su cupo de socios (US002)")
    public ResponseEntity<EntrenadorResponse> registrar(@Valid @RequestBody EntrenadorRequest solicitud) {
        EntrenadorResponse entrenador = entrenadorService.registrar(solicitud);
        return ResponseEntity.created(URI.create("/api/entrenadores/" + entrenador.id())).body(entrenador);
    }

    @GetMapping
    @Operation(summary = "Listar entrenadores con sus socios asignados")
    public List<EntrenadorResponse> listar() {
        return entrenadorService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un entrenador")
    public EntrenadorResponse consultar(@PathVariable String id) {
        return entrenadorService.consultar(id);
    }

    @GetMapping("/{id}/socios")
    @Operation(summary = "Socios asignados al entrenador")
    public List<SocioResponse> socios(@PathVariable String id) {
        entrenadorService.obtener(id);
        return socioService.listar(id);
    }
}
