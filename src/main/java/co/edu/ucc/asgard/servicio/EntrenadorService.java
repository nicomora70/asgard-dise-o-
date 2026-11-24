package co.edu.ucc.asgard.servicio;

import co.edu.ucc.asgard.dto.entrenador.EntrenadorRequest;
import co.edu.ucc.asgard.dto.entrenador.EntrenadorResponse;
import co.edu.ucc.asgard.excepcion.RecursoNoEncontradoException;
import co.edu.ucc.asgard.excepcion.ReglaNegocioException;
import co.edu.ucc.asgard.modelo.Entrenador;
import co.edu.ucc.asgard.repositorio.EntrenadorRepository;
import co.edu.ucc.asgard.repositorio.SocioRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

/** Registro y consulta de entrenadores (RF01, US002). */
@Service
public class EntrenadorService {

    private final EntrenadorRepository entrenadorRepository;
    private final SocioRepository socioRepository;
    private final Clock reloj;

    public EntrenadorService(EntrenadorRepository entrenadorRepository, SocioRepository socioRepository,
            Clock reloj) {
        this.entrenadorRepository = entrenadorRepository;
        this.socioRepository = socioRepository;
        this.reloj = reloj;
    }

    public EntrenadorResponse registrar(EntrenadorRequest solicitud) {
        String documento = solicitud.documentoIdentidad().trim();
        if (entrenadorRepository.existsByDocumentoIdentidad(documento)) {
            throw new ReglaNegocioException("Ya existe un entrenador con el documento " + documento);
        }
        Entrenador entrenador = new Entrenador();
        entrenador.setNombre(solicitud.nombre().trim());
        entrenador.setDocumentoIdentidad(documento);
        entrenador.setCorreo(solicitud.correo());
        entrenador.setTelefono(solicitud.telefono());
        entrenador.setTurno(solicitud.turno());
        entrenador.setMaximoSocios(solicitud.maximoSocios());
        entrenador.setFechaRegistro(Instant.now(reloj));
        return EntrenadorResponse.desde(entrenadorRepository.save(entrenador), 0);
    }

    public Entrenador obtener(String id) {
        return entrenadorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el entrenador " + id));
    }

    public EntrenadorResponse consultar(String id) {
        return EntrenadorResponse.desde(obtener(id), socioRepository.countByEntrenadorId(id));
    }

    public List<EntrenadorResponse> listar() {
        return entrenadorRepository.findAll().stream()
                .map(e -> EntrenadorResponse.desde(e, socioRepository.countByEntrenadorId(e.getId())))
                .toList();
    }

    /** Verifica que el entrenador exista y tenga cupo para un socio mas; si no, responde 409. */
    public Entrenador verificarCupo(String entrenadorId) {
        Entrenador entrenador = obtener(entrenadorId);
        long asignados = socioRepository.countByEntrenadorId(entrenadorId);
        if (!entrenador.tieneCupo((int) asignados)) {
            throw new ReglaNegocioException("El entrenador " + entrenador.getNombre() + " ya tiene su cupo completo ("
                    + entrenador.getMaximoSocios() + " socios)");
        }
        return entrenador;
    }
}
