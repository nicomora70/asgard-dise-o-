package co.edu.ucc.asgard.servicio;

import co.edu.ucc.asgard.config.ParametrosSesiones;
import co.edu.ucc.asgard.dto.socio.SocioRequest;
import co.edu.ucc.asgard.dto.socio.SocioResponse;
import co.edu.ucc.asgard.excepcion.RecursoNoEncontradoException;
import co.edu.ucc.asgard.excepcion.ReglaNegocioException;
import co.edu.ucc.asgard.modelo.Socio;
import co.edu.ucc.asgard.modelo.enums.GrupoMuscular;
import co.edu.ucc.asgard.repositorio.RutinaRepository;
import co.edu.ucc.asgard.repositorio.SerieRepository;
import co.edu.ucc.asgard.repositorio.SesionRepository;
import co.edu.ucc.asgard.repositorio.SocioRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** Registro, consulta y eliminacion de socios (RF01, US001, RNF-006). */
@Service
public class SocioService {

    private final SocioRepository socioRepository;
    private final EntrenadorService entrenadorService;
    private final RutinaRepository rutinaRepository;
    private final SesionRepository sesionRepository;
    private final SerieRepository serieRepository;
    private final ParametrosSesiones parametrosSesiones;
    private final Clock reloj;

    public SocioService(SocioRepository socioRepository, EntrenadorService entrenadorService,
            RutinaRepository rutinaRepository, SesionRepository sesionRepository, SerieRepository serieRepository,
            ParametrosSesiones parametrosSesiones, Clock reloj) {
        this.socioRepository = socioRepository;
        this.entrenadorService = entrenadorService;
        this.rutinaRepository = rutinaRepository;
        this.sesionRepository = sesionRepository;
        this.serieRepository = serieRepository;
        this.parametrosSesiones = parametrosSesiones;
        this.reloj = reloj;
    }

    public SocioResponse registrar(SocioRequest solicitud) {
        String documento = solicitud.documentoIdentidad().trim();
        if (socioRepository.existsByDocumentoIdentidad(documento)) {
            throw new ReglaNegocioException("Ya existe un socio con el documento " + documento);
        }
        if (solicitud.entrenadorId() != null && !solicitud.entrenadorId().isBlank()) {
            entrenadorService.verificarCupo(solicitud.entrenadorId());
        }
        Socio socio = new Socio();
        socio.setNombre(solicitud.nombre().trim());
        socio.setDocumentoIdentidad(documento);
        socio.setCorreo(solicitud.correo());
        socio.setTelefono(solicitud.telefono());
        socio.setNivel(solicitud.nivel());
        socio.setObjetivo(solicitud.objetivo());
        socio.setFechaIngreso(solicitud.fechaIngreso() == null ? hoy() : solicitud.fechaIngreso());
        socio.setDatosFisicos(solicitud.datosFisicos() == null ? null : solicitud.datosFisicos().aModelo());
        socio.setLesiones(sinRepetidos(solicitud.lesiones()));
        socio.setEntrenadorId(solicitud.entrenadorId() == null || solicitud.entrenadorId().isBlank()
                ? null : solicitud.entrenadorId());
        socio.setFechaRegistro(Instant.now(reloj));
        return respuesta(socioRepository.save(socio));
    }

    public Socio obtener(String id) {
        return socioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el socio " + id));
    }

    public SocioResponse consultar(String id) {
        return respuesta(obtener(id));
    }

    public List<SocioResponse> listar(String entrenadorId) {
        List<Socio> socios = entrenadorId == null ? socioRepository.findAll()
                : socioRepository.findByEntrenadorId(entrenadorId);
        return socios.stream().map(this::respuesta).toList();
    }

    /** Actualiza las lesiones: el motor deja de sugerir ejercicios que carguen esos grupos (RF10). */
    public SocioResponse actualizarLesiones(String id, List<GrupoMuscular> lesiones) {
        Socio socio = obtener(id);
        socio.setLesiones(sinRepetidos(lesiones));
        return respuesta(socioRepository.save(socio));
    }

    /** Asigna (o cambia) el entrenador del socio, respetando el cupo del entrenador. */
    public SocioResponse asignarEntrenador(String id, String entrenadorId) {
        Socio socio = obtener(id);
        if (entrenadorId.equals(socio.getEntrenadorId())) {
            return respuesta(socio);
        }
        entrenadorService.verificarCupo(entrenadorId);
        socio.setEntrenadorId(entrenadorId);
        return respuesta(socioRepository.save(socio));
    }

    /** Elimina al socio y todos sus datos en cascada: series, sesiones y rutinas (RNF-006). */
    public void eliminar(String id) {
        Socio socio = obtener(id);
        serieRepository.deleteBySocioId(socio.getId());
        sesionRepository.deleteBySocioId(socio.getId());
        rutinaRepository.deleteBySocioId(socio.getId());
        socioRepository.delete(socio);
    }

    public Socio guardar(Socio socio) {
        return socioRepository.save(socio);
    }

    private SocioResponse respuesta(Socio socio) {
        return SocioResponse.desde(socio, hoy());
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj.withZone(parametrosSesiones.zona()));
    }

    private static List<GrupoMuscular> sinRepetidos(List<GrupoMuscular> lesiones) {
        return lesiones == null ? List.of() : lesiones.stream().filter(Objects::nonNull).distinct().toList();
    }
}
