package co.edu.ucc.asgard.servicio;

import co.edu.ucc.asgard.config.ParametrosSesiones;
import co.edu.ucc.asgard.dto.rutina.RutinaRequest;
import co.edu.ucc.asgard.dto.rutina.RutinaResponse;
import co.edu.ucc.asgard.excepcion.ReglaNegocioException;
import co.edu.ucc.asgard.excepcion.SolicitudInvalidaException;
import co.edu.ucc.asgard.modelo.DiaRutina;
import co.edu.ucc.asgard.modelo.Prescripcion;
import co.edu.ucc.asgard.modelo.Rutina;
import co.edu.ucc.asgard.modelo.Socio;
import co.edu.ucc.asgard.repositorio.RutinaRepository;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Rutinas semanales: el socio guarda su historial y solo una rutina esta activa (RF03, US004). */
@Service
public class RutinaService {

    private final RutinaRepository rutinaRepository;
    private final SocioService socioService;
    private final EntrenadorService entrenadorService;
    private final EjercicioService ejercicioService;
    private final ParametrosSesiones parametrosSesiones;
    private final Clock reloj;

    public RutinaService(RutinaRepository rutinaRepository, SocioService socioService,
            EntrenadorService entrenadorService, EjercicioService ejercicioService,
            ParametrosSesiones parametrosSesiones, Clock reloj) {
        this.rutinaRepository = rutinaRepository;
        this.socioService = socioService;
        this.entrenadorService = entrenadorService;
        this.ejercicioService = ejercicioService;
        this.parametrosSesiones = parametrosSesiones;
        this.reloj = reloj;
    }

    /** Asigna una rutina nueva: la anterior pasa al historial y la cola de sesiones pendientes se reinicia. */
    public RutinaResponse asignar(String socioId, RutinaRequest solicitud) {
        Socio socio = socioService.obtener(socioId);
        String entrenadorId = solicitud.entrenadorId() == null || solicitud.entrenadorId().isBlank()
                ? socio.getEntrenadorId() : solicitud.entrenadorId();
        if (entrenadorId != null) {
            entrenadorService.obtener(entrenadorId);
        }

        Set<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
        List<DiaRutina> diasRutina = new ArrayList<>();
        for (RutinaRequest.DiaRequest dia : solicitud.dias()) {
            if (!dias.add(dia.dia())) {
                throw new SolicitudInvalidaException("El dia " + dia.dia() + " esta repetido en la rutina");
            }
            Set<String> ejerciciosDelDia = new HashSet<>();
            List<Prescripcion> prescripciones = new ArrayList<>();
            int orden = 1;
            for (RutinaRequest.PrescripcionRequest p : dia.ejercicios()) {
                if (p.repsMin() > p.repsMax()) {
                    throw new SolicitudInvalidaException("En " + dia.dia() + ", repsMin (" + p.repsMin()
                            + ") no puede ser mayor que repsMax (" + p.repsMax() + ")");
                }
                if (!ejerciciosDelDia.add(p.ejercicioId())) {
                    throw new SolicitudInvalidaException("El ejercicio " + p.ejercicioId() + " esta repetido el "
                            + dia.dia());
                }
                ejercicioService.obtener(p.ejercicioId());
                prescripciones.add(new Prescripcion(orden++, p.ejercicioId(), p.series(), p.repsMin(), p.repsMax(),
                        p.rpeObjetivo(), p.cargaInicial(), p.descansoSegundos()));
            }
            diasRutina.add(new DiaRutina(dia.dia(), dia.nombre(), prescripciones));
        }

        rutinaRepository.findFirstBySocioIdAndActivaTrue(socioId).ifPresent(anterior -> {
            anterior.desactivar();
            rutinaRepository.save(anterior);
        });

        Rutina rutina = new Rutina();
        rutina.setSocioId(socioId);
        rutina.setEntrenadorId(entrenadorId);
        rutina.setNombre(solicitud.nombre().trim());
        rutina.setFechaInicio(solicitud.fechaInicio() == null ? hoy() : solicitud.fechaInicio());
        rutina.setSemanas(solicitud.semanas());
        rutina.setActiva(true);
        rutina.setFechaCreacion(Instant.now(reloj));
        rutina.setDias(diasRutina);
        Rutina guardada = rutinaRepository.save(rutina);

        socio.setColaPendientes(List.of());
        socioService.guardar(socio);
        return RutinaResponse.desde(guardada, ejercicioService.nombres());
    }

    public Optional<Rutina> activa(String socioId) {
        return rutinaRepository.findFirstBySocioIdAndActivaTrue(socioId);
    }

    /** Rutina activa o 409 si el socio no tiene una. */
    public Rutina obtenerActiva(String socioId) {
        return activa(socioId).orElseThrow(() -> new ReglaNegocioException(
                "El socio no tiene una rutina activa; pidele a tu entrenador que te asigne una"));
    }

    public RutinaResponse consultarActiva(String socioId) {
        socioService.obtener(socioId);
        return RutinaResponse.desde(obtenerActiva(socioId), ejercicioService.nombres());
    }

    public List<RutinaResponse> historial(String socioId) {
        socioService.obtener(socioId);
        var nombres = ejercicioService.nombres();
        return rutinaRepository.findBySocioIdOrderByFechaCreacionDesc(socioId).stream()
                .map(r -> RutinaResponse.desde(r, nombres)).toList();
    }

    public List<Rutina> rutinasActivas() {
        return rutinaRepository.findByActivaTrue();
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj.withZone(parametrosSesiones.zona()));
    }
}
