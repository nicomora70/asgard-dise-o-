package co.edu.ucc.asgard.controlador;

import co.edu.ucc.asgard.dto.socio.LesionesRequest;
import co.edu.ucc.asgard.dto.socio.SocioRequest;
import co.edu.ucc.asgard.dto.socio.SocioResponse;
import co.edu.ucc.asgard.servicio.SocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/socios")
@Tag(name = "1. Socios y entrenadores", description = "Registro de socios, entrenadores, datos fisicos y lesiones (RF01)")
public class SocioController {

    private final SocioService socioService;

    public SocioController(SocioService socioService) {
        this.socioService = socioService;
    }

    @PostMapping
    @Operation(summary = "Registrar un socio con datos fisicos, lesiones y entrenador (US001)")
    public ResponseEntity<SocioResponse> registrar(@Valid @RequestBody SocioRequest solicitud) {
        SocioResponse socio = socioService.registrar(solicitud);
        return ResponseEntity.created(URI.create("/api/socios/" + socio.id())).body(socio);
    }

    @GetMapping
    @Operation(summary = "Listar socios")
    public List<SocioResponse> listar() {
        return socioService.listar(null);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un socio")
    public SocioResponse consultar(@PathVariable String id) {
        return socioService.consultar(id);
    }

    @PutMapping("/{id}/lesiones")
    @Operation(summary = "Actualizar las lesiones: el motor no sugiere ejercicios que carguen esos grupos (RF10)")
    public SocioResponse actualizarLesiones(@PathVariable String id, @Valid @RequestBody LesionesRequest solicitud) {
        return socioService.actualizarLesiones(id, solicitud.lesiones());
    }

    @PutMapping("/{id}/entrenador/{entrenadorId}")
    @Operation(summary = "Asignar o cambiar el entrenador del socio, respetando su cupo")
    public SocioResponse asignarEntrenador(@PathVariable String id, @PathVariable String entrenadorId) {
        return socioService.asignarEntrenador(id, entrenadorId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar al socio y todos sus datos en cascada (RNF-006)")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        socioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
